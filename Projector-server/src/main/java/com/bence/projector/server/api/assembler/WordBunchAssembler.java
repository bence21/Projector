package com.bence.projector.server.api.assembler;

import com.bence.projector.common.dto.WordBunchDTO;
import com.bence.projector.server.backend.model.Song;
import com.bence.projector.server.utils.models.WordBunch;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class WordBunchAssembler implements GeneralAssembler<WordBunch, WordBunchDTO> {

    private final SongTitleAssembler songTitleAssembler;

    @Autowired
    public WordBunchAssembler(SongTitleAssembler songTitleAssembler) {
        this.songTitleAssembler = songTitleAssembler;
    }

    @Override
    public WordBunchDTO createDto(WordBunch wordBunch) {
        if (wordBunch == null) {
            return null;
        }
        WordBunchDTO wordBunchDTO = new WordBunchDTO();
        wordBunchDTO.setWord(wordBunch.getWord());
        wordBunchDTO.setCount(wordBunch.getCount());
        List<Song> songs = wordBunch.getSongs();
        if (songs != null && !songs.isEmpty()) {
            wordBunchDTO.setSong(songTitleAssembler.createDto(songs.get(0)));
        }
        wordBunchDTO.setProblematic(wordBunch.isProblematic());
        return wordBunchDTO;
    }

    @Override
    public WordBunch createModel(WordBunchDTO wordBunchDTO) {
        return null;
        // final WordBunch wordBunch = new WordBunch();
        // return updateModel(wordBunch, wordBunchDTO);
    }

    @Override
    public WordBunch updateModel(WordBunch wordBunch, WordBunchDTO wordBunchDTO) {
        return wordBunch;
    }
}
