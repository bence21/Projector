package com.bence.songbook.repository.impl.ormLite;

import android.content.Context;
import android.util.Log;

import com.bence.songbook.models.SongList;
import com.bence.songbook.repository.DatabaseHelper;
import com.bence.songbook.repository.SongListRepository;
import com.bence.songbook.repository.dao.CustomDao;
import com.bence.songbook.repository.exception.RepositoryException;

import java.sql.SQLException;
import java.util.ArrayList;

public class SongListRepositoryImpl extends AbstractRepository<SongList> implements SongListRepository {
    private static final String TAG = SongListRepositoryImpl.class.getSimpleName();
    private final CustomDao<SongList, Long> songListDao;

    public SongListRepositoryImpl(Context context) {
        super(SongList.class);
        try {
            DatabaseHelper databaseHelper = DatabaseHelper.getInstance(context);
            songListDao = databaseHelper.getSongListDao();
            super.setDao(songListDao);
        } catch (SQLException e) {
            String msg = "Failed to initialize SongListRepository";
            Log.e(TAG, msg);
            throw new RepositoryException(msg, e);
        }
    }

    @Override
    public SongList findByUuid(String uuid) {
        String msg = "Could not find songList";
        try {
            ArrayList<SongList> songLists = (ArrayList<SongList>) songListDao.queryForEq("uuid", uuid);
            if (songLists != null && songLists.size() > 0) {
                return songLists.get(0);
            }
            return null;
        } catch (Exception e) {
            Log.e(TAG, msg);
            throw new RepositoryException(msg, e);
        }
    }
}
