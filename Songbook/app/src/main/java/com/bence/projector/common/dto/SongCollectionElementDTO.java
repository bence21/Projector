package com.bence.projector.common.dto;

public class SongCollectionElementDTO {
    private String ordinalNumber;
    private String songUuid;

    public String getOrdinalNumber() {
        return ordinalNumber;
    }

    public void setOrdinalNumber(String ordinalNumber) {
        this.ordinalNumber = ordinalNumber;
    }

    public String getSongUuid() {
        return songUuid;
    }

    public void setSongUuid(String songUuid) {
        this.songUuid = songUuid;
    }
}
