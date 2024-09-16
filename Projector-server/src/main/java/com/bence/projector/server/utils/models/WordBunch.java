package com.bence.projector.server.utils.models;

import com.bence.projector.server.backend.model.Song;

import java.util.ArrayList;
import java.util.List;

public class WordBunch {

    private String word;
    private int count;
    private List<Song> songs;

    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
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
}
