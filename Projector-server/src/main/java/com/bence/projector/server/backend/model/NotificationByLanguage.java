package com.bence.projector.server.backend.model;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
public class NotificationByLanguage extends BaseEntity {

    private static final int INITIAL_DELAY = 24 * 60 * 60 * 1000;
    @ManyToOne(fetch = FetchType.LAZY)
    private Language language;
    private Boolean suggestions;
    private Boolean newSongs;
    private Integer suggestionsDelay;
    private Integer newSongsDelay;
    private Date suggestionsLastSentDate;
    private Date newSongsLastSentDate;
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "SUGGESTION_STACK")
    private List<Suggestion> suggestionStack;
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "NEW_SONG_STACK")
    private List<Song> newSongStack;
    @ManyToOne(fetch = FetchType.LAZY)
    private UserProperties userProperties;

    public Language getLanguage() {
        return language;
    }

    public void setLanguage(Language language) {
        this.language = language;
    }

    public Boolean isSuggestions() {
        return suggestions != null && suggestions;
    }

    public void setSuggestions(Boolean suggestions) {
        this.suggestions = suggestions;
    }

    public Boolean isNewSongs() {
        return newSongs != null && newSongs;
    }

    public void setNewSongs(Boolean newSongs) {
        this.newSongs = newSongs;
    }

    public Integer getSuggestionsDelay() {
        if (suggestionsDelay == null) {
            suggestionsDelay = INITIAL_DELAY;
        }
        return suggestionsDelay;
    }

    public void setSuggestionsDelay(Integer suggestionsDelay) {
        this.suggestionsDelay = suggestionsDelay;
    }

    public Integer getNewSongsDelay() {
        if (newSongsDelay == null) {
            newSongsDelay = INITIAL_DELAY;
        }
        return newSongsDelay;
    }

    public void setNewSongsDelay(Integer newSongsDelay) {
        this.newSongsDelay = newSongsDelay;
    }

    public Date getSuggestionsLastSentDate() {
        if (suggestionsLastSentDate == null) {
            return new Date(0);
        }
        return suggestionsLastSentDate;
    }

    public void setSuggestionsLastSentDate(Date suggestionsLastSentDate) {
        this.suggestionsLastSentDate = suggestionsLastSentDate;
    }

    public Date getNewSongsLastSentDate() {
        if (newSongsLastSentDate == null) {
            return new Date(0);
        }
        return newSongsLastSentDate;
    }

    public void setNewSongsLastSentDate(Date newSongsLastSentDate) {
        this.newSongsLastSentDate = newSongsLastSentDate;
    }

    public List<Suggestion> getSuggestionStack() {
        if (suggestionStack == null) {
            suggestionStack = new ArrayList<>();
        }
        return suggestionStack;
    }

    public void setSuggestionStack(List<Suggestion> suggestionStack) {
        this.suggestionStack = suggestionStack;
    }

    public List<Song> getNewSongStack() {
        if (newSongStack == null) {
            newSongStack = new ArrayList<>();
        }
        List<Song> songList = new ArrayList<>(newSongStack.size());
        for (Song song : newSongStack) {
            if (song != null) {
                songList.add(song);
            }
        }
        if (songList.size() != newSongStack.size()) {
            newSongStack.clear();
            newSongStack.addAll(songList);
        }
        return newSongStack;
    }

    public void setNewSongStack(List<Song> newSongStack) {
        this.newSongStack = newSongStack;
    }

    public void setUserProperties(UserProperties userProperties) {
        this.userProperties = userProperties;
    }
}
