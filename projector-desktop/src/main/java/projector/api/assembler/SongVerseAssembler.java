package projector.api.assembler;

import com.bence.projector.common.dto.SongVerseDTO;
import projector.model.SongVerse;

import java.util.ArrayList;
import java.util.List;

public class SongVerseAssembler implements GeneralAssembler<SongVerse, SongVerseDTO> {

    private static SongVerseAssembler instance;

    private SongVerseAssembler() {
    }

    public static synchronized SongVerseAssembler getInstance() {
        if (instance == null) {
            instance = new SongVerseAssembler();
        }
        return instance;
    }

    @Override
    public SongVerseDTO createDto(SongVerse songVerse) {
        SongVerseDTO songVerseDTO = new SongVerseDTO();
        songVerseDTO.setText(songVerse.getText());
        songVerseDTO.setChorus(songVerse.isChorus());
        return songVerseDTO;
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
}
