package com.bence.projector.common.dto;

public class SongListElementDTO {

    private int number;
    private String songUuid;

    public String getSongUuid() {
        return songUuid;
    }

    public void setSongUuid(String songUuid) {
        this.songUuid = songUuid;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }
}
