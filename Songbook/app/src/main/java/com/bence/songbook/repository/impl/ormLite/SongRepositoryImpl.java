package com.bence.songbook.repository.impl.ormLite;

import android.content.Context;
import android.util.Log;
import android.widget.ProgressBar;

import com.bence.projector.common.dto.SongViewsDTO;
import com.bence.songbook.models.Language;
import com.bence.songbook.models.Song;
import com.bence.songbook.repository.DatabaseHelper;
import com.bence.songbook.repository.SongRepository;
import com.bence.songbook.repository.SongVerseRepository;
import com.bence.songbook.repository.dao.CustomDao;
import com.bence.songbook.repository.exception.RepositoryException;
import com.j256.ormlite.misc.TransactionManager;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

public class SongRepositoryImpl extends AbstractRepository<Song> implements SongRepository {
    private static final String TAG = SongRepositoryImpl.class.getSimpleName();
    private final DatabaseHelper databaseHelper;

    private final CustomDao<Song, Long> songDao;
    private final SongVerseRepository songVerseRepository;

    public SongRepositoryImpl(final Context context) {
        super(Song.class);
        try {
            DatabaseHelper databaseHelper = DatabaseHelper.getInstance(context);
            this.databaseHelper = databaseHelper;
            songDao = databaseHelper.getSongDao();
            super.setDao(songDao);
            songVerseRepository = new SongVerseRepositoryImpl(context);
        } catch (SQLException e) {
            String msg = "Failed to initialize SongRepository";
            Log.e(TAG, msg);
            throw new RepositoryException(msg, e);
        }
    }

    @Override
    public Song findOne(final Long id) {
        String msg = "Could not find song";
        try {
            return songDao.queryForId(id);
        } catch (SQLException e) {
            Log.e(TAG, msg);
            throw new RepositoryException(msg, e);
        } catch (Exception e) {
            Log.e(TAG, msg, e);
            return null;
        }
    }

    @Override
    public List<Song> findAll() {
        try {
            return songDao.queryForAll();
        } catch (final SQLException e) {
            String msg = "Could not find all songs";
            Log.e(TAG, msg);
            throw new RepositoryException(msg, e);
        }
    }

    @Override
    public void save(final Song song) {
        try {
            if (!song.isDeleted()) {
                songDao.createOrUpdate(song);
                songVerseRepository.save(song.getVerses());
            }
        } catch (SQLException e) {
            String msg = "Could not save song";
            Log.e(TAG, msg);
            throw new RepositoryException(msg, e);
        }
    }

    @Override
    public void save(final List<Song> songs) {
        try {
            songDao.callBatchTasks(
                    (Callable<Void>) () -> {
                        for (final Song song : songs) {
                            save(song);
                        }
                        return null;
                    });
        } catch (Exception e) {
            String msg = "Could not save songs";
            Log.e(TAG, msg);
            throw new RepositoryException(msg, e);
        }
    }

    @Override
    public void save(final List<Song> newSongs, final ProgressBar progressBar) {
        try {
            songDao.callBatchTasks(
                    (Callable<Void>) () -> {
                        int i = 0;
                        for (final Song song : newSongs) {
                            save(song);
                            progressBar.setProgress(++i);
                        }
                        return null;
                    });
        } catch (Exception e) {
            String msg = "Could not save songs";
            Log.e(TAG, msg);
            throw new RepositoryException(msg, e);
        }
    }

    @Override
    public Song findByUUID(String uuid) {
        String msg = "Could not find song";
        try {
            ArrayList<Song> uuid1 = songDao.queryForEq("uuid", uuid);
            if (uuid1 != null && uuid1.size() > 0) {
                return uuid1.get(0);
            }
            return null;
        } catch (Exception e) {
            Log.e(TAG, msg);
            throw new RepositoryException(msg, e);
        }
    }

    @Override
    public List<Song> findAllByVersionGroup(String versionGroup) {
        String msg = "Could not find song versions";
        try {
            List<Song> songs = songDao.queryForEq("versionGroup", versionGroup);
            Song byUUID = findByUUID(versionGroup);
            if (byUUID != null) {
                songs.add(byUUID);
            }
            return songs;
        } catch (Exception e) {
            Log.e(TAG, msg);
            throw new RepositoryException(msg, e);
        }
    }

    @Override
    public void saveViews(final List<SongViewsDTO> songViewsDTOS) {
        try {
            if (songViewsDTOS == null || songViewsDTOS.isEmpty()) {
                return;
            }
            TransactionManager.callInTransaction(databaseHelper.getConnectionSource(),
                    () -> {
                        for (SongViewsDTO songViewsDTO : songViewsDTOS) {
                            if (songViewsDTO != null) {
                                String songViewsDTOUuid = songViewsDTO.getUuid();
                                if (songViewsDTOUuid != null) {
                                    String uuid = songViewsDTOUuid.replace("'", "''");
                                    songDao.executeRaw("UPDATE song SET views = "
                                            + songViewsDTO.getViews()
                                            + " WHERE uuid = '" + uuid + "'");
                                }
                            }
                        }
                        return null;
                    });
        } catch (SQLException e) {
            String msg = "Could not save verseIndices";
            Log.e(TAG, msg);
            throw new RepositoryException(msg, e);
        }
    }

    @Override
    public List<Song> findAllExceptAsDeleted() {
        List<Song> songs = findAll();
        List<Song> songList = new ArrayList<>();
        for (Song song : songs) {
            if (!song.isAsDeleted()) {
                songList.add(song);
            }
        }
        return songList;
    }

    @Override
    public void delete(final Song song) {
        try {
            if (song != null) {
                songDao.deleteById(song.getId());
            }
        } catch (SQLException e) {
            String msg = "Could not delete song";
            Log.e(TAG, msg);
            throw new RepositoryException(msg, e);
        }
    }

    @Override
    public long sumAccessedTimesByLanguage(Language language) {
        long x = 0L;
        try {
            x = songDao.queryRawValue("SELECT SUM(accessedTimes) FROM song WHERE language_id = " + language.getId());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return x;
    }

    @Override
    public long countByLanguage(Language language) {
        long x = 0L;
        try {
            x = songDao.queryRawValue("SELECT COUNT(id) FROM song WHERE language_id = " + language.getId());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return x;
    }

}
