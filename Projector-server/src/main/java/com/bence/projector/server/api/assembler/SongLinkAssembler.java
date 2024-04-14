package com.bence.projector.server.api.assembler;

import com.bence.projector.common.dto.SongLinkDTO;
import com.bence.projector.server.backend.model.Song;
import com.bence.projector.server.backend.model.SongLink;
import com.bence.projector.server.backend.repository.SongRepository;
import com.bence.projector.server.backend.service.SongService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class SongLinkAssembler implements GeneralAssembler<SongLink, SongLinkDTO> {

    @Autowired
    private SongService songService;
    @Autowired
    private SongRepository songRepository;

    @Override
    public SongLinkDTO createDto(SongLink songLink) {
        if (songLink == null) {
            return null;
        }
        SongLinkDTO songLinkDTO = new SongLinkDTO();
        songLinkDTO.setUuid(songLink.getUuid());
        songLinkDTO.setCreatedDate(songLink.getCreatedDate());
        songLinkDTO.setModifiedDate(songLink.getModifiedDate());
        songLinkDTO.setCreatedByEmail(songLink.getCreatedByEmail());
        songLinkDTO.setApplied(songLink.getApplied());
        Song song1 = songLink.getSong1(songRepository);
        if (song1 != null) {
            songLinkDTO.setSongId1(song1.getUuid());
            songLinkDTO.setTitle1(song1.getTitle());
        }
        Song song2 = songLink.getSong2(songRepository);
        if (song2 != null) {
            songLinkDTO.setSongId2(song2.getUuid());
            songLinkDTO.setTitle2(song2.getTitle());
        }
        return songLinkDTO;
    }

    @Override
    public SongLink createModel(SongLinkDTO songLinkDTO) {
        final SongLink songLink = new SongLink();
        songLink.setCreatedDate(new Date());
        return updateModel(songLink, songLinkDTO);
    }

    @Override
    public SongLink updateModel(SongLink songLink, SongLinkDTO songLinkDTO) {
        songLink.setCreatedByEmail(songLinkDTO.getCreatedByEmail());
        songLink.setApplied(songLinkDTO.getApplied());
        songLink.setSong1(songService.findOneByUuid(songLinkDTO.getSongId1()));
        songLink.setSong2(songService.findOneByUuid(songLinkDTO.getSongId2()));
        return songLink;
    }
}
