package projector.api.assembler;

import com.bence.projector.common.dto.SongCollectionDTO;
import com.bence.projector.common.dto.SongCollectionElementDTO;
import projector.model.SongCollection;
import projector.model.SongCollectionElement;

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
    public SongCollectionDTO createDto(SongCollection songCollection) {
        SongCollectionDTO songCollectionDTO = new SongCollectionDTO();
        songCollectionDTO.setId(songCollection.getUuid());
        songCollectionDTO.setUuid(songCollection.getUuid());
        songCollectionDTO.setCreatedDate(songCollection.getCreatedDate());
        songCollectionDTO.setModifiedDate(songCollection.getModifiedDate());
        songCollectionDTO.setLanguageUuid(songCollection.getLanguage().getUuid());
        songCollectionDTO.setName(songCollection.getName());
        ArrayList<SongCollectionElementDTO> songCollectionElementDTOS = new ArrayList<>();
        for (SongCollectionElement songCollectionElement : songCollection.getSongCollectionElements()) {
            SongCollectionElementDTO songCollectionElementDTO = new SongCollectionElementDTO();
            songCollectionElementDTO.setOrdinalNumber(songCollectionElement.getOrdinalNumber());
            songCollectionElementDTO.setSongUuid(songCollectionElement.getSongUuid());
            songCollectionElementDTOS.add(songCollectionElementDTO);
        }
        songCollectionDTO.setSongCollectionElements(songCollectionElementDTOS);
        return songCollectionDTO;
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

    @Override
    public List<SongCollectionDTO> createDtoList(List<SongCollection> songCollections) {
        return null;
    }
}
