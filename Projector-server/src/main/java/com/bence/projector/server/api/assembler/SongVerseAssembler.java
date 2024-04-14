package com.bence.projector.server.api.assembler;

import com.bence.projector.common.dto.SongVerseDTO;
import com.bence.projector.common.model.SectionType;
import com.bence.projector.server.backend.model.SongVerse;
import org.springframework.stereotype.Component;

@Component
public class SongVerseAssembler implements GeneralAssembler<SongVerse, SongVerseDTO> {
    @Override
    public SongVerseDTO createDto(SongVerse songVerse) {
        SongVerseDTO songVerseDTO = new SongVerseDTO();
        songVerseDTO.setText(songVerse.getText());
        songVerseDTO.setChorus(songVerse.isChorus());
        SectionType sectionType = songVerse.getSectionType();
        if (sectionType != null) {
            songVerseDTO.setType(sectionType.getValue());
        } else {
            if (songVerse.isChorus()) {
                songVerseDTO.setType(SectionType.CHORUS.getValue());
            } else {
                String type = songVerse.getType();
                if (type != null) {
                    String s = type.toUpperCase();
                    if (s.startsWith("V")) {
                        songVerseDTO.setType(SectionType.VERSE.getValue());
                    } else if (s.startsWith("C")) {
                        songVerseDTO.setType(SectionType.CHORUS.getValue());
                    } else if (s.startsWith("T") || s.startsWith("S")) {
                        songVerseDTO.setType(SectionType.CODA.getValue());
                    } else if (s.startsWith("I")) {
                        songVerseDTO.setType(SectionType.INTRO.getValue());
                    } else if (s.startsWith("P")) {
                        songVerseDTO.setType(SectionType.PRE_CHORUS.getValue());
                    } else if (s.startsWith("B")) {
                        songVerseDTO.setType(SectionType.BRIDGE.getValue());
                    } else {
                        songVerseDTO.setType(SectionType.VERSE.getValue());
                    }
                } else {
                    songVerseDTO.setType(SectionType.VERSE.getValue());
                }
            }
        }
        return songVerseDTO;
    }

    @Override
    public SongVerse createModel(SongVerseDTO songVerseDTO) {
        return updateModel(new SongVerse(), songVerseDTO);
    }

    @Override
    public SongVerse updateModel(SongVerse songVerse, SongVerseDTO songVerseDTO) {
        songVerse.setText(songVerseDTO.getText());
        Integer type = songVerseDTO.getType();
        if (type != null) {
            songVerse.setSectionType(SectionType.getInstance(type));
        } else {
            if (songVerseDTO.isChorus()) {
                songVerse.setSectionType(SectionType.CHORUS);
            } else {
                songVerse.setSectionType(SectionType.VERSE);
            }
        }
        return songVerse;
    }
}
