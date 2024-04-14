package com.bence.projector.server.backend.service.impl;

import com.bence.projector.server.backend.model.Song;
import com.bence.projector.server.backend.model.SongVerse;
import com.bence.projector.server.backend.model.Suggestion;
import com.bence.projector.server.backend.service.LanguageService;
import com.bence.projector.server.backend.service.SongService;
import com.bence.projector.server.backend.service.SuggestionService;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;
import java.util.List;

import static com.bence.projector.server.backend.service.impl.SongServiceImplTest.getASong;
import static com.bence.projector.server.backend.service.impl.SongServiceImplTest.getSongVerses;

public class SuggestionServiceImplTest extends BaseServiceTest {

    @Autowired
    private SuggestionService suggestionService;
    @Autowired
    private LanguageService languageService;
    @Autowired
    private SongService songService;

    @Test
    public void testSaveSuggestion() {
        Suggestion suggestion = getASuggestion();
        suggestionService.save(suggestion);
        Suggestion suggestion1 = suggestionService.findOne(suggestion.getId());
        Assert.assertTrue(suggestion1.getVerses().size() > 0);
    }

    private Suggestion getASuggestion() {
        Suggestion suggestion = new Suggestion();
        suggestion.setCreatedDate(new Date());
        suggestion.setModifiedDate(suggestion.getCreatedDate());
        suggestion.setCreatedByEmail("test@email");
        suggestion.setDescription("Test description");
        Song aSong = getASavedSong();
        suggestion.setSong(aSong);
        suggestion.setTitle(aSong.getTitle());
        List<SongVerse> songVerses = getSongVerses();
        SongVerse songVerse = songVerses.get(0);
        songVerse.setText(songVerse.getText() + " suggestion addition");
        suggestion.setVerses(songVerses);
        return suggestion;
    }

    private Song getASavedSong() {
        Song aSong = getASong(languageService);
        songService.save(aSong);
        return aSong;
    }
}