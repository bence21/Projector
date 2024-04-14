package com.bence.projector.server.api.assembler;

import com.bence.projector.common.dto.SongListDTO;
import com.bence.projector.common.dto.SongListElementDTO;
import com.bence.projector.server.backend.model.SongList;
import com.bence.projector.server.backend.model.SongListElement;
import com.bence.projector.server.backend.service.SongService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Date;

@Component
public class SongListAssembler implements GeneralAssembler<SongList, SongListDTO> {
    @Autowired
    private SongService songService;

    @Override
    public SongListDTO createDto(SongList songList) {
        if (songList == null) {
            return null;
        }
        SongListDTO songListDTO = new SongListDTO();
        songListDTO.setUuid(songList.getUuid());
        songListDTO.setCreatedDate(songList.getCreatedDate());
        songListDTO.setModifiedDate(songList.getModifiedDate());
        songListDTO.setCreatedByEmail(songList.getCreatedByEmail());
        ArrayList<SongListElementDTO> songListElements = new ArrayList<>();
        for (SongListElement songListElement : songList.getSongListElements()) {
            SongListElementDTO dto = new SongListElementDTO();
            dto.setNumber(songListElement.getNumber());
            dto.setSongUuid(songListElement.getSongUuid());
            songListElements.add(dto);
        }
        songListDTO.setSongListElements(songListElements);
        songListDTO.setDescription(songList.getDescription());
        songListDTO.setTitle(songList.getTitle());
        return songListDTO;
    }

    @Override
    public SongList createModel(SongListDTO songListDTO) {
        final SongList songList = new SongList();
        songList.setUuid(songListDTO.getUuid());
        Date createdDate = songListDTO.getCreatedDate();
        if (createdDate == null || createdDate.getTime() < 1000) {
            songList.setCreatedDate(new Date());
        } else {
            songList.setCreatedDate(createdDate);
        }
        songList.setModifiedDate(songListDTO.getModifiedDate());
        return updateModel(songList, songListDTO);
    }

    @Override
    public SongList updateModel(SongList songList, SongListDTO songListDTO) {
        songList.setCreatedByEmail(songListDTO.getCreatedByEmail());
        ArrayList<SongListElement> songListElements = new ArrayList<>();
        for (SongListElementDTO dto : songListDTO.getSongListElements()) {
            SongListElement element = new SongListElement();
            element.setNumber(dto.getNumber());
            element.setSong(songService.findOneByUuid(dto.getSongUuid()));
            songListElements.add(element);
        }
        songList.setSongListElements(songListElements);
        songList.setDescription(songListDTO.getDescription());
        songList.setTitle(songListDTO.getTitle());
        return songList;
    }
}
