package com.bence.projector.server.api.assembler;

import com.bence.projector.common.dto.SongTitleDTO;
import com.bence.projector.server.backend.model.Song;
import com.bence.projector.server.backend.model.SongVerse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class SongTitleAssembler implements GeneralAssembler<Song, SongTitleDTO> {

    private static final Logger log = LoggerFactory.getLogger(SongTitleAssembler.class);

    @Override
    public SongTitleDTO createDto(Song song) {
        SongTitleDTO songTitleDTO = new SongTitleDTO();
        songTitleDTO.setId(song.getUuid());
        songTitleDTO.setTitle(song.getTitle());
        songTitleDTO.setCreatedDate(song.getCreatedDate());
        songTitleDTO.setModifiedDate(song.getModifiedDate());
        songTitleDTO.setDeleted(song.isDeleted());
        songTitleDTO.setViews(song.getViews());
        songTitleDTO.setFavourites(song.getFavourites());
        songTitleDTO.setYoutubeUrl(song.getYoutubeUrl());
        List<SongVerse> verses = song.getVerses();
        int verseCount = verses == null ? 0 : verses.size();
        songTitleDTO.setVerseCount(verseCount);
        if (verseCount == 0) {
            log.warn("Song in title list has no verses: id={} title={}", song.getUuid(), song.getTitle());
        }
        return songTitleDTO;
    }

    @Override
    public Song createModel(SongTitleDTO songTitleDTO) {
        return updateModel(new Song(), songTitleDTO);
    }

    @Override
    public Song updateModel(Song song, SongTitleDTO songTitleDTO) {
        song.setTitle(songTitleDTO.getTitle());
        song.setModifiedDate(songTitleDTO.getModifiedDate());
        return song;
    }
}
