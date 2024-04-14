package com.bence.projector.server.api.assembler;

import com.bence.projector.common.dto.SongTitleDTO;
import com.bence.projector.server.backend.model.Song;
import org.springframework.stereotype.Component;

@Component
public class SongTitleAssembler implements GeneralAssembler<Song, SongTitleDTO> {

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
