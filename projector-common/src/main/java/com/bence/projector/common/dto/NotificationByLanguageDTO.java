package com.bence.projector.common.dto;

public class NotificationByLanguageDTO {
    private Boolean suggestions;
    private Boolean newSongs;
    private LanguageDTO language;
    private Integer suggestionsDelay;
    private Integer newSongsDelay;

    public Boolean getSuggestions() {
        return suggestions;
    }

    public void setSuggestions(Boolean suggestions) {
        this.suggestions = suggestions;
    }

    public Boolean getNewSongs() {
        return newSongs;
    }

    public void setNewSongs(Boolean newSongs) {
        this.newSongs = newSongs;
    }

    public LanguageDTO getLanguage() {
        return language;
    }

    public void setLanguage(LanguageDTO language) {
        this.language = language;
    }

    public Integer getSuggestionsDelay() {
        return suggestionsDelay;
    }

    public void setSuggestionsDelay(Integer suggestionsDelay) {
        this.suggestionsDelay = suggestionsDelay;
    }

    public Integer getNewSongsDelay() {
        return newSongsDelay;
    }

    public void setNewSongsDelay(Integer newSongsDelay) {
        this.newSongsDelay = newSongsDelay;
    }
}
