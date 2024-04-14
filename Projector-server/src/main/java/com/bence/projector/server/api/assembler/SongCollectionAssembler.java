package com.bence.projector.server.api.assembler;

import com.bence.projector.common.dto.SongCollectionDTO;
import com.bence.projector.common.dto.SongCollectionElementDTO;
import com.bence.projector.server.backend.model.Language;
import com.bence.projector.server.backend.model.SongCollection;
import com.bence.projector.server.backend.model.SongCollectionElement;
import com.bence.projector.server.backend.service.LanguageService;
import com.bence.projector.server.backend.service.SongService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Component
public class SongCollectionAssembler implements GeneralAssembler<SongCollection, SongCollectionDTO> {

    @Autowired
    private SongService songService;
    @Autowired
    private LanguageService languageService;

    private SongCollectionDTO createMinimalDto(SongCollection songCollection) {
        if (songCollection == null) {
            return null;
        }
        SongCollectionDTO songCollectionDTO = new SongCollectionDTO();
        songCollectionDTO.setUuid(songCollection.getUuid());
        songCollectionDTO.setCreatedDate(songCollection.getCreatedDate());
        songCollectionDTO.setModifiedDate(songCollection.getModifiedDate());
        songCollectionDTO.setName(songCollection.getName());
        Language language = songCollection.getLanguage();
        if (language != null) {
            songCollectionDTO.setLanguageUuid(language.getUuid());
        }
        return songCollectionDTO;
    }

    @Override
    public SongCollectionDTO createDto(SongCollection songCollection) {
        SongCollectionDTO songCollectionDTO = createMinimalDto(songCollection);
        if (songCollectionDTO == null) {
            return null;
        }
        ArrayList<SongCollectionElementDTO> songCollectionElements = new ArrayList<>();
        for (SongCollectionElement songCollectionElement : songCollection.getSongCollectionElements()) {
            songCollectionElements.add(createElementModelDTO(songCollectionElement));
        }
        songCollectionDTO.setSongCollectionElements(songCollectionElements);
        return songCollectionDTO;
    }

    private SongCollectionElementDTO createElementModelDTO(SongCollectionElement songCollectionElement) {
        SongCollectionElementDTO songCollectionElementDTO = new SongCollectionElementDTO();
        songCollectionElementDTO.setOrdinalNumber(songCollectionElement.getOrdinalNumber());
        songCollectionElementDTO.setSongUuid(songCollectionElement.getSongUuid());
        return songCollectionElementDTO;
    }

    public SongCollectionElement createElementModel(SongCollectionElementDTO songCollectionElementDTO) {
        SongCollectionElement songCollectionElement = new SongCollectionElement();
        songCollectionElement.setOrdinalNumber(songCollectionElementDTO.getOrdinalNumber());
        songCollectionElement.setSong(songService.findOneByUuid(songCollectionElementDTO.getSongUuid()));
        return songCollectionElement;
    }

    @Override
    public SongCollection createModel(SongCollectionDTO songCollectionDTO) {
        final SongCollection songCollection = new SongCollection();
        return updateModel(songCollection, songCollectionDTO);
    }

    @Override
    public SongCollection updateModel(SongCollection songCollection, SongCollectionDTO songCollectionDTO) {
        Date createdDate = songCollectionDTO.getCreatedDate();
        if (createdDate == null) {
            createdDate = new Date();
        }
        songCollection.setCreatedDate(createdDate);
        songCollection.setModifiedDate(new Date());
        songCollection.setName(songCollectionDTO.getName());
        ArrayList<SongCollectionElement> songCollectionElements = new ArrayList<>();
        List<SongCollectionElementDTO> songCollectionElementDTOS = songCollectionDTO.getSongCollectionElements();
        if (songCollectionElementDTOS != null) {
            for (SongCollectionElementDTO dto : songCollectionElementDTOS) {
                SongCollectionElement songCollectionElement = new SongCollectionElement();
                songCollectionElement.setOrdinalNumber(dto.getOrdinalNumber());
                songCollectionElement.setSong(songService.findOneByUuid(dto.getSongUuid()));
                songCollectionElements.add(songCollectionElement);
            }
        }
        songCollection.setSongCollectionElements(songCollectionElements);
        String languageUuid = songCollectionDTO.getLanguageUuid();
        if (languageUuid != null) {
            songCollection.setLanguage(languageService.findOneByUuid(languageUuid));
        }
        return songCollection;
    }

    public List<SongCollectionDTO> createMinimalDtoList(List<SongCollection> songCollections) {
        ArrayList<SongCollectionDTO> songCollectionDTOS = new ArrayList<>(songCollections.size());
        for (SongCollection songCollection : songCollections) {
            songCollectionDTOS.add(createMinimalDto(songCollection));
        }
        return songCollectionDTOS;
    }
}
