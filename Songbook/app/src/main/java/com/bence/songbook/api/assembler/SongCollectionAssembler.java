package com.bence.songbook.api.assembler;

import com.bence.projector.common.dto.SongCollectionDTO;
import com.bence.projector.common.dto.SongCollectionElementDTO;
import com.bence.songbook.ProgressMessage;
import com.bence.songbook.models.SongCollection;
import com.bence.songbook.models.SongCollectionElement;

import java.util.ArrayList;
import java.util.List;

public class SongCollectionAssembler implements GeneralAssembler<SongCollection, SongCollectionDTO> {
    private static SongCollectionAssembler instance;

    private SongCollectionAssembler() {
    }

    public static SongCollectionAssembler getInstance() {
        if (instance == null) {
            instance = new SongCollectionAssembler();
        }
        return instance;
    }

    @Override
    public synchronized SongCollection createModel(SongCollectionDTO songCollectionDTO) {
        return updateModel(new SongCollection(), songCollectionDTO);
    }

    @Override
    public synchronized SongCollection updateModel(SongCollection songCollection, SongCollectionDTO songCollectionDTO) {
        if (songCollection != null) {
            songCollection.setUuid(songCollectionDTO.getUuid());
            songCollection.setCreatedDate(songCollectionDTO.getCreatedDate());
            songCollection.setModifiedDate(songCollectionDTO.getModifiedDate());
            songCollection.setName(songCollectionDTO.getName());
            List<SongCollectionElement> songCollectionElements = new ArrayList<>();
            for (SongCollectionElementDTO songCollectionElementDTO : songCollectionDTO.getSongCollectionElements()) {
                SongCollectionElement songCollectionElement = new SongCollectionElement();
                songCollectionElement.setOrdinalNumber(songCollectionElementDTO.getOrdinalNumber());
                songCollectionElement.setSongUuid(songCollectionElementDTO.getSongUuid());
                songCollectionElement.setSongCollection(songCollection);
                songCollectionElements.add(songCollectionElement);
            }
            songCollection.setSongCollectionElements(songCollectionElements);
        }
        return songCollection;
    }

    @Override
    public synchronized List<SongCollection> createModelList(List<SongCollectionDTO> ds) {
        if (ds == null) {
            return null;
        }
        List<SongCollection> models = new ArrayList<>();
        for (SongCollectionDTO songCollectionDTO : ds) {
            models.add(createModel(songCollectionDTO));
        }
        return models;
    }

    public synchronized List<SongCollection> createModelList(List<SongCollectionDTO> ds, ProgressMessage progressMessage) {
        if (ds == null) {
            return null;
        }
        List<SongCollection> models = new ArrayList<>();
        int i = 0;
        for (SongCollectionDTO songCollectionDTO : ds) {
            models.add(createModel(songCollectionDTO));
            progressMessage.onProgress(++i);
        }
        return models;
    }
}
