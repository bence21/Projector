package com.bence.projector.server.backend.service;

import com.bence.projector.server.backend.model.Song;
import com.bence.projector.server.backend.model.SongVerse;

public interface SongVerseService extends BaseService<SongVerse> {
    void deleteBySong(Song song);
}
