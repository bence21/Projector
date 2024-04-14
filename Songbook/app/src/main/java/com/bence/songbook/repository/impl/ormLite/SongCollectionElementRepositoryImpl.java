package com.bence.songbook.repository.impl.ormLite;

import android.content.Context;
import android.util.Log;
import android.widget.ProgressBar;

import com.bence.songbook.models.SongCollectionElement;
import com.bence.songbook.repository.DatabaseHelper;
import com.bence.songbook.repository.SongCollectionElementRepository;
import com.bence.songbook.repository.dao.CustomDao;
import com.bence.songbook.repository.exception.RepositoryException;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class SongCollectionElementRepositoryImpl extends AbstractRepository<SongCollectionElement> implements SongCollectionElementRepository {
    private static final String TAG = SongCollectionElementRepositoryImpl.class.getSimpleName();
    private final CustomDao<SongCollectionElement, Long> songCollectionDao;

    public SongCollectionElementRepositoryImpl(Context context) {
        super(SongCollectionElement.class);
        try {
            DatabaseHelper databaseHelper = DatabaseHelper.getInstance(context);
            songCollectionDao = databaseHelper.getSongCollectionElementDao();
            super.setDao(songCollectionDao);
        } catch (SQLException e) {
            String msg = "Failed to initialize SongCollectionRepository";
            Log.e(TAG, msg);
            throw new RepositoryException(msg, e);
        }
    }

    @Override
    public void save(List<SongCollectionElement> songCollectionElements, ProgressBar progressBar) {
        progressBar.setMax(songCollectionElements.size());
        int i = 0;
        for (SongCollectionElement songCollectionElement : songCollectionElements) {
            save(songCollectionElement);
            progressBar.setProgress(++i);
        }
    }

    @Override
    public SongCollectionElement findSongCollectionElementBySongUuid(String uuid) {
        String msg = "Could not find songCollectionElement";
        try {
            ArrayList<SongCollectionElement> songCollectionElements = (ArrayList<SongCollectionElement>) songCollectionDao.queryForEq("songUuid", uuid);
            if (songCollectionElements != null && songCollectionElements.size() > 0) {
                return songCollectionElements.get(0);
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
}
