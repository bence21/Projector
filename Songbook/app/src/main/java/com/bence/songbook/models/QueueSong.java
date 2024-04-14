package com.bence.songbook.models;

import com.j256.ormlite.field.DatabaseField;

public class QueueSong extends Base {

    @DatabaseField(foreign = true, index = true)
    private Song song;
    @DatabaseField
    private int queueNumber;

    public Song getSong() {
        return song;
    }

    public void setSong(Song song) {
        this.song = song;
    }

    public int getQueueNumber() {
        return queueNumber;
    }

    public void setQueueNumber(int queueNumber) {
        this.queueNumber = queueNumber;
    }
}
