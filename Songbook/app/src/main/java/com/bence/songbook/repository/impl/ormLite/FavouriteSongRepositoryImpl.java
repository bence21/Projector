package com.bence.songbook.repository.impl.ormLite;

import android.content.Context;
import android.util.Log;

import com.bence.songbook.models.FavouriteSong;
import com.bence.songbook.models.Song;
import com.bence.songbook.repository.DatabaseHelper;
import com.bence.songbook.repository.FavouriteSongRepository;
import com.bence.songbook.repository.SongRepository;
import com.bence.songbook.repository.dao.CustomDao;
import com.bence.songbook.repository.exception.RepositoryException;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class FavouriteSongRepositoryImpl extends BaseRepositoryImpl<FavouriteSong> implements FavouriteSongRepository {
    private static final String TAG = FavouriteSongRepositoryImpl.class.getSimpleName();
    private final CustomDao<FavouriteSong, Long> favouriteSongDao;
    private final Context context;
    private SongRepository songRepository;

    public FavouriteSongRepositoryImpl(Context context) {
        super(FavouriteSong.class);
        this.context = context;
        try {
            DatabaseHelper databaseHelper = DatabaseHelper.getInstance(context);
            favouriteSongDao = databaseHelper.getFavouriteSongDao();
            super.setDao(favouriteSongDao);
        } catch (SQLException e) {
            String msg = "Failed to initialize FavouriteSongRepository";
            Log.e(TAG, msg);
            throw new RepositoryException(msg, e);
        }
    }

    @Override
    public FavouriteSong findFavouriteSongBySongUuid(String uuid) {
        if (uuid == null) {
            return null;
        }
        String msg = "Could not find favouriteSong";
        try {
            songRepository = getSongRepository();
            Song byUUID = songRepository.findByUUID(uuid);
            if (byUUID != null) {
                ArrayList<FavouriteSong> favouriteSongs = favouriteSongDao.queryForEq("song_id", byUUID.getId());
                if (favouriteSongs != null && favouriteSongs.size() > 0) {
                    return favouriteSongs.get(0);
                }
            }
            return null;
        } catch (SQLException e) {
            Log.e(TAG, msg);
            throw new RepositoryException(msg, e);
        } catch (Exception e) {
            Log.e(TAG, msg);
            throw new RepositoryException(msg, e);
        }
    }

    private SongRepository getSongRepository() {
        if (songRepository == null) {
            songRepository = new SongRepositoryImpl(context);
        }
        return songRepository;
    }

    @Override
    public void save(FavouriteSong favourite) {
        if (favourite == null) {
            return;
        }
        Song song = favourite.getSong();
        if (song == null) {
            return;
        }
        FavouriteSong favouriteSongBySongUuid = findFavouriteSongBySongUuid(song.getUuid());
        if (song.getId() == null) {
            if (favouriteSongBySongUuid != null) {
                favourite.setSong(favouriteSongBySongUuid.getSong());
                favourite.setId(favouriteSongBySongUuid.getId());
                super.save(favourite);
            }
        } else {
            if (favouriteSongBySongUuid != null) {
                favourite.setId(favouriteSongBySongUuid.getId());
            }
            super.save(favourite);
        }
    }

    @Override
    public void save(List<FavouriteSong> models) {
        for (FavouriteSong favourite : models) {
            save(favourite);
        }
    }
}
