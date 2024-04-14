package com.bence.projector.server.api.assembler;

import com.bence.projector.common.dto.SongDTO;
import com.bence.projector.server.backend.model.Song;
import com.bence.projector.server.backend.model.SongVerse;
import com.bence.projector.server.backend.model.User;
import com.bence.projector.server.backend.service.LanguageService;
import com.bence.projector.server.backend.service.SongService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Component
public class SongAssembler implements GeneralAssembler<Song, SongDTO> {
    private final SongVerseAssembler songVerseAssembler;
    private final LanguageAssembler languageAssembler;
    private final LanguageService languageService;
    private final SongService songService;

    @Autowired
    public SongAssembler(SongVerseAssembler songVerseAssembler, LanguageAssembler languageAssembler, LanguageService languageService, SongService songService) {
        this.songVerseAssembler = songVerseAssembler;
        this.languageAssembler = languageAssembler;
        this.languageService = languageService;
        this.songService = songService;
    }

    @Override
    public SongDTO createDto(Song song) {
        if (song == null) {
            return null;
        }
        SongDTO songDTO = new SongDTO();
        songDTO.setUuid(song.getUuid());
        songDTO.setOriginalId(song.getOriginalId());
        songDTO.setTitle(song.getTitle());
        songDTO.setCreatedDate(song.getCreatedDate());
        songDTO.setModifiedDate(song.getModifiedDate());
        songDTO.setSongVerseDTOS(songVerseAssembler.createDtoList(song.getVerses()));
        songDTO.setDeleted(song.isDeleted());
        songDTO.setLanguageDTO(languageAssembler.createDto(song.getLanguage()));
        songDTO.setUploaded(song.getUploaded());
        songDTO.setViews(song.getViews());
        songDTO.setFavourites(song.getFavourites());
        songDTO.setCreatedByEmail(song.getCreatedByEmail());
        songDTO.setVersionGroup(song.getVersionGroupUuid());
        songDTO.setYoutubeUrl(song.getYoutubeUrl());
        String verseOrder = song.getVerseOrder();
        songDTO.setVerseOrder(verseOrder);
        songDTO.setVerseOrderList(song.getVerseOrderList());
        if (verseOrder != null && song.verseOrderWasNotSaved()) {
            try {
                String[] split = verseOrder.split(" ");
                List<Short> verseOrderList = new ArrayList<>(split.length);
                for (String s : split) {
                    short index = 0;
                    for (SongVerse songVerse : song.getVerses()) {
                        String type = songVerse.getType();
                        if (type != null && type.equalsIgnoreCase(s)) {
                            verseOrderList.add(index);
                            break;
                        }
                        ++index;
                    }
                    if (index == song.getVerses().size()) {
                        String substring = s.substring(1);
                        short count = 1;
                        if (substring.contains("X")) {
                            String x = substring.substring(substring.indexOf("X") + 1);
                            count = Short.parseShort(x);
                            substring = substring.substring(0, substring.indexOf("X"));
                        }
                        index = Short.parseShort(substring);
                        --index;
                        for (int i = 0; i < count; ++i) {
                            verseOrderList.add(index);
                        }
                    }
                }
                songDTO.setVerseOrderList(verseOrderList);
            } catch (Exception ignored) {
            }
        }
        songDTO.setAuthor(song.getAuthor());
        songDTO.setReviewerErased(song.isReviewerErased());
        Song backUp = song.getBackUp();
        if (backUp != null) {
            songDTO.setBackUpSongId(backUp.getUuid());
        } else {
            songDTO.setBackUpSongId(null);
        }
        User lastModifiedBy = song.getLastModifiedBy();
        if (lastModifiedBy != null) {
            songDTO.setLastModifiedByUserEmail(lastModifiedBy.getEmail());
        }
        return songDTO;
    }

    @Override
    public Song createModel(SongDTO songDTO) {
        final Song song = new Song();
        Date createdDate = songDTO.getCreatedDate();
        if (createdDate == null || createdDate.getTime() < 1000) {
            song.setCreatedDate(new Date());
        } else {
            song.setCreatedDate(createdDate);
        }
        return updateModel(song, songDTO);
    }

    @Override
    public Song updateModel(Song song, SongDTO songDTO) {
        if (song.getUuid() != null && song.getUuid().equals(songDTO.getOriginalId())) {
            song.setOriginalId(null);
        } else {
            song.setOriginalId(songDTO.getOriginalId());
        }
        song.setTitle(songDTO.getTitle());
        Date modifiedDate = songDTO.getModifiedDate();
        if (modifiedDate == null || modifiedDate.getTime() < 1000) {
            song.setModifiedDate(new Date());
        } else {
            song.setModifiedDate(modifiedDate);
        }
        final List<SongVerse> songVerses = songVerseAssembler.createModelList(songDTO.getSongVerseDTOS());
        song.setVerses(songVerses);
        song.setDeleted(songDTO.isDeleted());
        if (songDTO.getLanguageDTO() != null) {
            song.setLanguage(languageService.findOneByUuid(songDTO.getLanguageDTO().getUuid()));
        }
        song.setCreatedByEmail(songDTO.getCreatedByEmail());
        song.setVersionGroup(songService.findOneByUuid(songDTO.getVersionGroup()));
        song.setYoutubeUrl(songDTO.getYoutubeUrl());
        song.setVerseOrder(null);
        song.setVerseOrderList(songDTO.getVerseOrderList());
        if (songDTO.getAuthor() != null && !songDTO.getAuthor().isEmpty()) {
            song.setAuthor(songDTO.getAuthor());
        } else {
            song.setAuthor(null);
        }
        return song;
    }
}
