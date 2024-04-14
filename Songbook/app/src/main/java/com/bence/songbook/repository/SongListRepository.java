package com.bence.songbook.repository;

import com.bence.songbook.models.SongList;

public interface SongListRepository extends BaseRepository<SongList> {
    SongList findByUuid(String id);
}
