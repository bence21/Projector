package com.bence.psbremote.util;

import com.bence.psbremote.SongSenderRemoteListener;
import com.bence.psbremote.model.Song;

import java.util.ArrayList;
import java.util.List;

public class Memory {

    private static Memory instance;
    private ObservableList<String> songVerses = new ObservableList<>();
    private ObservableList<Song> songs = new ObservableList<>();
    private String projectionText;
    private List<OnChangeListener> projectionTextOnChangeListeners;
    private SongSenderRemoteListener songSenderRemoteListener;

    private Memory() {
    }

    public static Memory getInstance() {
        if (instance == null) {
            instance = new Memory();
        }
        return instance;
    }

    public ObservableList<String> getSongVerses() {
        return songVerses;
    }

    public void setSongVerses(List<String> songVerses) {
        this.songVerses.clear();
        this.songVerses.addAll(songVerses);
    }

    public ObservableList<Song> getSongs() {
        return songs;
    }

    public void setSongs(List<Song> songs) {
        this.songs.clear();
        this.songs.addAll(songs);
    }

    public void addProjectionTextOnChangeListener(OnChangeListener onChangeListener) {
        if (projectionTextOnChangeListeners == null) {
            projectionTextOnChangeListeners = new ArrayList<>();
        }
        projectionTextOnChangeListeners.add(onChangeListener);
    }

    public String getProjectionText() {
        return projectionText;
    }

    public void setProjectionText(String projectionText) {
        this.projectionText = projectionText;
        for (OnChangeListener onChangeListener : projectionTextOnChangeListeners) {
            onChangeListener.onChange();
        }
    }

    public SongSenderRemoteListener getSongSenderRemoteListener() {
        return songSenderRemoteListener;
    }

    public void setSongSenderRemoteListener(SongSenderRemoteListener songSenderRemoteListener) {
        this.songSenderRemoteListener = songSenderRemoteListener;
    }
}
