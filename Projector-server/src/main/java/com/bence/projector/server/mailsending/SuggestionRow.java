package com.bence.projector.server.mailsending;

import com.bence.projector.server.backend.model.Song;
import com.bence.projector.server.backend.model.Suggestion;

public class SuggestionRow {
    private Suggestion suggestion;
    private Song song;
    private String suggestionType;

    public Suggestion getSuggestion() {
        return suggestion;
    }

    public void setSuggestion(Suggestion suggestion) {
        this.suggestion = suggestion;
    }

    public Song getSong() {
        return song;
    }

    public void setSong(Song song) {
        this.song = song;
    }

    public String getSuggestionType() {
        return suggestionType;
    }

    public void setSuggestionType(String suggestionType) {
        this.suggestionType = suggestionType;
    }
}
