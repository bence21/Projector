package com.bence.songbook.models;

import com.j256.ormlite.field.DatabaseField;

public class SongListElement extends Base {
    @DatabaseField(columnName = "song_id", foreign = true, foreignAutoRefresh = true, index = true, canBeNull = false)
    private Song song;
    @DatabaseField
    private int number;
    @DatabaseField(foreign = true, index = true)
    private SongList songList;

    public Song getSong() {
        return song;
    }

    public void setSong(Song song) {
        this.song = song;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public SongList getSongList() {
        return songList;
    }

    public void setSongList(SongList songList) {
        this.songList = songList;
    }
}
