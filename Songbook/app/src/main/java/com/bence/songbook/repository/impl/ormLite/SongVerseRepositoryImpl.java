package com.bence.songbook.repository.impl.ormLite;

import android.content.Context;
import android.util.Log;

import com.bence.songbook.models.SongVerse;
import com.bence.songbook.repository.DatabaseHelper;
import com.bence.songbook.repository.SongVerseRepository;
import com.bence.songbook.repository.dao.CustomDao;
import com.bence.songbook.repository.exception.RepositoryException;

import java.sql.SQLException;
import java.util.List;

public class SongVerseRepositoryImpl extends AbstractRepository<SongVerse> implements SongVerseRepository {
    private static final String TAG = SongVerseRepositoryImpl.class.getSimpleName();

    private final CustomDao<SongVerse, Long> songVerseDao;

    SongVerseRepositoryImpl(final Context context) {
        super(SongVerse.class);
        try {
            DatabaseHelper databaseHelper = DatabaseHelper.getInstance(context);
            songVerseDao = databaseHelper.getSongVerseDao();
            super.setDao(songVerseDao);
        } catch (SQLException e) {
            String msg = "Failed to initialize SongVerseRepository";
            Log.e(TAG, msg);
            throw new RepositoryException(msg, e);
        }
    }

    @Override
    public SongVerse findOne(final Long id) {
        try {
            return songVerseDao.queryForId(id);
        } catch (SQLException e) {
            String msg = "Could not find songVerse";
            Log.e(TAG, msg);
            throw new RepositoryException(msg, e);
        }
    }

    @Override
    public List<SongVerse> findAll() {
        String msg = "Could not find all songVerses";
        try {
            return songVerseDao.callBatchTasks(
                    songVerseDao::queryForAll);
        } catch (final SQLException e) {
            Log.e(TAG, msg);
            throw new RepositoryException(msg, e);
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, msg);
            throw new RepositoryException(msg, e);
        }
    }

    @Override
    public void save(final SongVerse songVerse) {
        try {
            songVerseDao.createOrUpdate(songVerse);
        } catch (SQLException e) {
            String msg = "Could not save songVerse";
            Log.e(TAG, msg);
            throw new RepositoryException(msg, e);
        }
    }

    @Override
    public void save(final List<SongVerse> songVerses) {
        try {
            for (final SongVerse songVerse : songVerses) {
                songVerseDao.createOrUpdate(songVerse);
            }
        } catch (SQLException e) {
            String msg = "Could not save songVerses";
            Log.e(TAG, msg);
            throw new RepositoryException(msg, e);
        }
    }

    @Override
    public void delete(final SongVerse songVerse) {
        try {
            if (songVerse != null) {
                songVerseDao.deleteById(songVerse.getId());
            }
        } catch (SQLException e) {
            String msg = "Could not delete songVerse";
            Log.e(TAG, msg);
            throw new RepositoryException(msg, e);
        }
    }
}
