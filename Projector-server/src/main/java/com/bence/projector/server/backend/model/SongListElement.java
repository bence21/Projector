package com.bence.projector.server.backend.model;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;

@Entity
public class SongListElement extends BaseEntity {

    private int number;
    @ManyToOne(fetch = FetchType.LAZY)
    private Song song;
    @ManyToOne(fetch = FetchType.LAZY)
    private SongList songList;

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public String getSongUuid() {
        if (song == null) {
            return null;
        }
        return song.getUuid();
    }

    public Song getSong() {
        return song;
    }

    public void setSong(Song song) {
        this.song = song;
    }

    public void setSongList(SongList songList) {
        this.songList = songList;
    }
}
