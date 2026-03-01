package com.bence.songbook.repository.impl.ormLite;

import android.content.Context;
import android.util.Log;

import com.bence.songbook.models.SongListElement;
import com.bence.songbook.repository.DatabaseHelper;
import com.bence.songbook.repository.SongListElementRepository;
import com.bence.songbook.repository.dao.CustomDao;
import com.bence.songbook.repository.exception.RepositoryException;

import java.sql.SQLException;
import java.util.Locale;
import java.util.concurrent.Callable;

public class SongListElementRepositoryImpl extends BaseRepositoryImpl<SongListElement> implements SongListElementRepository {
    private static final String TAG = SongListElementRepositoryImpl.class.getSimpleName();
    /**
     * ORMLite default table name for SongListElement (lowercase class name).
     */
    private static final String TABLE_NAME = "songlistelement";

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
                // Atomic swap: update both rows in one statement to avoid UNIQUE constraint violations.
                // IDs and numbers are from our model, not user input — safe to format.
                String sql = String.format(Locale.US,
                        "UPDATE %s SET number = CASE WHEN id = %d THEN %d WHEN id = %d THEN %d ELSE number END WHERE id IN (%d, %d)",
                        TABLE_NAME,
                        first.getId(), secondNumber,
                        second.getId(), firstNumber,
                        first.getId(), second.getId()
                );
                songListElementDao.executeRaw(sql);
                return null;
            });
        } catch (Exception e) {
            String msg = "Could not save swap of song list elements";
            Log.e(TAG, msg);
            throw new RepositoryException(msg, e);
        }
    }
}
