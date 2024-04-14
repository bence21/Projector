package com.bence.songbook.repository.impl.ormLite;

import android.content.Context;
import android.util.Log;
import android.widget.ProgressBar;

import com.bence.songbook.ProgressMessage;
import com.bence.songbook.models.Language;
import com.bence.songbook.models.SongCollection;
import com.bence.songbook.repository.DatabaseHelper;
import com.bence.songbook.repository.SongCollectionElementRepository;
import com.bence.songbook.repository.SongCollectionRepository;
import com.bence.songbook.repository.dao.CustomDao;
import com.bence.songbook.repository.exception.RepositoryException;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class SongCollectionRepositoryImpl extends AbstractRepository<SongCollection> implements SongCollectionRepository {
    private static final String TAG = SongCollectionRepositoryImpl.class.getSimpleName();
    private final SongCollectionElementRepository songCollectionElementRepository;

    public SongCollectionRepositoryImpl(Context context) {
        super(SongCollection.class);
        try {
            DatabaseHelper databaseHelper = DatabaseHelper.getInstance(context);
            CustomDao<SongCollection, Long> songCollectionDao = databaseHelper.getSongCollectionDao();
            super.setDao(songCollectionDao);
            songCollectionElementRepository = new SongCollectionElementRepositoryImpl(context);
        } catch (SQLException e) {
            String msg = "Failed to initialize SongCollectionRepository";
            Log.e(TAG, msg);
            throw new RepositoryException(msg, e);
        }
    }

    @Override
    public List<SongCollection> findAllByLanguage(Language language) {
        List<SongCollection> allSongCollections = findAll();
        List<SongCollection> songCollections = new ArrayList<>();
        Long uuid = language.getId();
        for (SongCollection songCollection : allSongCollections) {
            if (songCollection.getLanguage().getId().equals(uuid)) {
                songCollections.add(songCollection);
            }
        }
        return songCollections;
    }

    @Override
    public void save(List<SongCollection> songCollections, ProgressBar progressBar, ProgressMessage progressMessage) {
        int i = 0;
        for (SongCollection songCollection : songCollections) {
            progressMessage.onSongCollectionProgress(i++);
            save(songCollection, progressBar);
        }

    }

    public void save(SongCollection songCollection, ProgressBar progressBar) {
        super.save(songCollection);
        songCollectionElementRepository.save(songCollection.getSongCollectionElements(), progressBar);
    }

    @Override
    public void save(SongCollection songCollection) {
        super.save(songCollection);
        songCollectionElementRepository.save(songCollection.getSongCollectionElements());
    }

    @Override
    public void save(List<SongCollection> songCollections) {
        for (SongCollection songCollection : songCollections) {
            save(songCollection);
        }
    }
}
