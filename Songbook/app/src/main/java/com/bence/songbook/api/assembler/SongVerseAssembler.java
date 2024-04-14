package com.bence.songbook.api.assembler;

import com.bence.projector.common.dto.SongVerseDTO;
import com.bence.projector.common.model.SectionType;
import com.bence.songbook.models.SongVerse;

import java.util.ArrayList;
import java.util.List;

public class SongVerseAssembler implements GeneralAssembler<SongVerse, SongVerseDTO> {

    private static SongVerseAssembler instance;

    private SongVerseAssembler() {
    }

    public static SongVerseAssembler getInstance() {
        if (instance == null) {
            instance = new SongVerseAssembler();
        }
        return instance;
    }

    @Override
    public synchronized SongVerse createModel(SongVerseDTO songVerseDTO) {
        return updateModel(new SongVerse(), songVerseDTO);
    }

    @Override
    public synchronized SongVerse updateModel(SongVerse songVerse, SongVerseDTO songVerseDTO) {
        if (songVerse != null) {
            songVerse.setText(songVerseDTO.getText());
            songVerse.setChorus(songVerseDTO.isChorus());
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
        }
        return songVerse;
    }

    @Override
    public synchronized List<SongVerse> createModelList(List<SongVerseDTO> ds) {
        List<SongVerse> models = new ArrayList<>();
        for (SongVerseDTO songVerseDTO : ds) {
            models.add(createModel(songVerseDTO));
        }
        return models;
    }

    List<SongVerseDTO> createDTOS(List<SongVerse> verses) {
        List<SongVerseDTO> songVerseDTOS = new ArrayList<>(verses.size());
        for (SongVerse songVerse : verses) {
            songVerseDTOS.add(createDTO(songVerse));
        }
        return songVerseDTOS;
    }

    private SongVerseDTO createDTO(SongVerse songVerse) {
        SongVerseDTO songVerseDTO = new SongVerseDTO();
        songVerseDTO.setText(songVerse.getText());
        songVerseDTO.setChorus(songVerse.isChorus());
        return songVerseDTO;
    }
}
