package com.bence.projector.server.utils.models;

import com.bence.projector.server.backend.model.Song;

import java.util.ArrayList;
import java.util.List;

import static com.bence.projector.server.utils.StringUtils.normalizeAccents;
import static com.bence.projector.server.utils.StringUtils.stripAccents;

public class WordBunch {

    private String word;
    private String normalizedWord;
    private String stripWord;
    private int count;
    private List<Song> songs;
    private boolean problematic;

    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
    }

    public String getNormalizedWord() {
        if (normalizedWord == null) {
            normalizedWord = normalizeAccents(word);
        }
        return normalizedWord;
    }

    public String getStripWordWord() {
        if (stripWord == null) {
            stripWord = stripAccents(word.toLowerCase());
        }
        return stripWord;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public List<Song> getSongs() {
        if (songs == null) {
            songs = new ArrayList<>();
        }
        return songs;
    }

    public void setSongs(List<Song> songs) {
        this.songs = songs;
    }

    public void incCount() {
        ++this.count;
    }

    public void addSong(Song song) {
        getSongs().add(song);
    }

    public void setProblematic(boolean problematic) {
        this.problematic = problematic;
    }

    public boolean isProblematic() {
        return problematic;
    }
}
