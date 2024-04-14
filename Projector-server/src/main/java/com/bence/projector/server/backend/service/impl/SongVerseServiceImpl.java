package com.bence.projector.server.backend.service.impl;

import com.bence.projector.server.backend.model.Song;
import com.bence.projector.server.backend.model.SongVerse;
import com.bence.projector.server.backend.repository.SongVerseRepository;
import com.bence.projector.server.backend.service.SongVerseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SongVerseServiceImpl extends BaseServiceImpl<SongVerse> implements SongVerseService {

    @Autowired
    private SongVerseRepository songVerseRepository;

    @Override
    public void deleteBySong(Song song) {
        if (song == null) {
            return;
        }
        Long id = song.getId();
        if (id == null) {
            return;
        }
        songVerseRepository.deleteAllBySongId(id);
    }
}
