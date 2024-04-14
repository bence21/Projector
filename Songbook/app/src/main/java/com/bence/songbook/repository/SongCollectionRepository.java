package com.bence.songbook.repository;

import android.widget.ProgressBar;

import com.bence.songbook.ProgressMessage;
import com.bence.songbook.models.Language;
import com.bence.songbook.models.SongCollection;

import java.util.List;

public interface SongCollectionRepository extends BaseRepository<SongCollection> {
    List<SongCollection> findAllByLanguage(Language language);

    void save(List<SongCollection> songCollections, ProgressBar progressBar, ProgressMessage progressMessage);
}
