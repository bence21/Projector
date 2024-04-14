package com.bence.songbook.repository;

import android.widget.ProgressBar;

import com.bence.songbook.models.SongCollectionElement;

import java.util.List;

public interface SongCollectionElementRepository extends BaseRepository<SongCollectionElement> {
    void save(List<SongCollectionElement> songCollectionElements, ProgressBar progressBar);

    SongCollectionElement findSongCollectionElementBySongUuid(String uuid);
}
