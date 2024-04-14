package com.bence.songbook.repository.impl.ormLite;

import android.content.Context;
import android.util.Log;

import com.bence.songbook.models.SongListElement;
import com.bence.songbook.repository.DatabaseHelper;
import com.bence.songbook.repository.SongListElementRepository;
import com.bence.songbook.repository.dao.CustomDao;
import com.bence.songbook.repository.exception.RepositoryException;

import java.sql.SQLException;

public class SongListElementRepositoryImpl extends BaseRepositoryImpl<SongListElement> implements SongListElementRepository {
    private static final String TAG = SongListElementRepositoryImpl.class.getSimpleName();

    public SongListElementRepositoryImpl(Context context) {
        super(SongListElement.class);
        try {
            DatabaseHelper databaseHelper = DatabaseHelper.getInstance(context);
            CustomDao<SongListElement, Long> songListElementDao = databaseHelper.getSongListElementDao();
            super.setDao(songListElementDao);
        } catch (SQLException e) {
            String msg = "Failed to initialize SongListElementRepository";
            Log.e(TAG, msg);
            throw new RepositoryException(msg, e);
        }
    }
}
