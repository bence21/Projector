package projector.api.assembler;

import com.bence.projector.common.dto.SongDTO;
import projector.model.Song;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class SongAssembler implements GeneralAssembler<Song, SongDTO> {

    private static SongAssembler instance;
    private static SongVerseAssembler songVerseAssembler = SongVerseAssembler.getInstance();
    private static LanguageAssembler languageAssembler = LanguageAssembler.getInstance();

    private SongAssembler() {
    }

    public static synchronized SongAssembler getInstance() {
        if (instance == null) {
            instance = new SongAssembler();
        }
        return instance;
    }

    @Override
    public SongDTO createDto(Song song) {
        SongDTO songDTO = new SongDTO();
        songDTO.setUuid(song.getUuid());
        songDTO.setTitle(song.getTitle());
        songDTO.setCreatedDate(song.getCreatedDate());
        songDTO.setModifiedDate(song.getModifiedDate());
        songDTO.setSongVerseDTOS(songVerseAssembler.createDtoList(song.getVerses()));
        songDTO.setLanguageDTO(languageAssembler.createDto(song.getLanguage()));
        songDTO.setVersionGroup(song.getVersionGroup());
        songDTO.setVerseOrderList(song.getVerseOrderList());
        return songDTO;
    }

    @Override
    public synchronized Song createModel(SongDTO songDTO) {
        if (songDTO == null) {
            return null;
        }
        final Song song = new Song();
        song.setPublished(true);
        return updateModel(song, songDTO);
    }

    @Override
    public synchronized Song updateModel(Song song, SongDTO songDTO) {
        if (songDTO == null) {
            return null;
        }
        if (song != null) {
            song.setUuid(songDTO.getUuid());
            song.setTitle(songDTO.getTitle());
            song.setCreatedDate(songDTO.getCreatedDate());
            Date modifiedDate = songDTO.getModifiedDate();
            song.setModifiedDate(modifiedDate);
            song.setServerModifiedDate(modifiedDate);
            song.setVerses(songVerseAssembler.createModelList(songDTO.getSongVerseDTOS()));
            song.setDeleted(songDTO.isDeleted());
            song.setVersionGroup(songDTO.getVersionGroup());
            song.setViews(songDTO.getViews());
            song.setFavouriteCount(songDTO.getFavourites());
            song.setAuthor(songDTO.getAuthor());
            song.setVerseOrderList(songDTO.getVerseOrderList());
        }
        return song;
    }

    @Override
    public synchronized List<Song> createModelList(List<SongDTO> ds) {
        if (ds == null) {
            return null;
        }
        List<Song> models = new ArrayList<>();
        for (SongDTO songDTO : ds) {
            models.add(createModel(songDTO));
        }
        return models;
    }
}
