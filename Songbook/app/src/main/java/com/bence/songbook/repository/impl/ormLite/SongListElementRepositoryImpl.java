package com.bence.songbook.repository.impl.ormLite;

import android.content.Context;
import android.util.Log;

import com.bence.songbook.models.SongListElement;
import com.bence.songbook.repository.DatabaseHelper;
import com.bence.songbook.repository.SongListElementRepository;
import com.bence.songbook.repository.dao.CustomDao;
import com.bence.songbook.repository.exception.RepositoryException;

import java.sql.SQLException;
import java.util.concurrent.Callable;

public class SongListElementRepositoryImpl extends BaseRepositoryImpl<SongListElement> implements SongListElementRepository {
    private static final String TAG = SongListElementRepositoryImpl.class.getSimpleName();
    /**
     * Sentinel value for swap persistence; avoids unique constraint during reorder.
     */
    private static final int SWAP_SENTINEL_NUMBER = -1;

    private final CustomDao<SongListElement, Long> songListElementDao;

    public SongListElementRepositoryImpl(Context context) {
        super(SongListElement.class);
        try {
            DatabaseHelper databaseHelper = DatabaseHelper.getInstance(context);
            songListElementDao = databaseHelper.getSongListElementDao();
            super.setDao(songListElementDao);
        } catch (SQLException e) {
            String msg = "Failed to initialize SongListElementRepository";
            Log.e(TAG, msg);
            throw new RepositoryException(msg, e);
        }
    }

    @Override
    public void saveSwap(SongListElement first, SongListElement second, int firstNumber, int secondNumber) {
        try {
            songListElementDao.callBatchTasks((Callable<Void>) () -> {
                first.setNumber(SWAP_SENTINEL_NUMBER);
                songListElementDao.createOrUpdate(first);
                second.setNumber(firstNumber);
                songListElementDao.createOrUpdate(second);
                first.setNumber(secondNumber);
                songListElementDao.createOrUpdate(first);
                return null;
            });
        } catch (Exception e) {
            String msg = "Could not save swap of song list elements";
            Log.e(TAG, msg);
            throw new RepositoryException(msg, e);
        }
    }
}
