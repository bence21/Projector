package com.bence.songbook.models;

import com.j256.ormlite.field.DatabaseField;

public class SongCollectionElement extends BaseEntity {

    @DatabaseField
    private String ordinalNumber;
    @DatabaseField
    private String songUuid;
    @DatabaseField(foreign = true, index = true)
    private SongCollection songCollection;

    public int getOrdinalNumberInt() {
        try {
            return Integer.parseInt(ordinalNumber.replaceAll("[^0-9]*", ""));
        } catch (NumberFormatException e) {
            return Integer.MAX_VALUE;
        }
    }

    public String getOrdinalNumber() {
        return ordinalNumber.replaceAll("^0+", "");
    }

    public void setOrdinalNumber(String ordinalNumber) {
        this.ordinalNumber = ordinalNumber;
    }

    @Override
    public String toString() {
        return ordinalNumber;
    }

    public String getSongUuid() {
        return songUuid;
    }

    public void setSongUuid(String songUuid) {
        this.songUuid = songUuid;
    }

    public SongCollection getSongCollection() {
        return songCollection;
    }

    public void setSongCollection(SongCollection songCollection) {
        this.songCollection = songCollection;
    }
}
