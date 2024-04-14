package com.bence.songbook.models;

import com.google.gson.annotations.Expose;
import com.j256.ormlite.field.DatabaseField;

import java.util.Date;

public class FavouriteSong extends Base {

    @Expose
    @DatabaseField(columnName = "song_id", foreign = true, foreignAutoRefresh = true, index = true, unique = true, canBeNull = false)
    private Song song;
    @Expose
    @DatabaseField
    private boolean favourite;
    @Expose
    @DatabaseField
    private boolean favouritePublished = true;
    @Expose
    @DatabaseField
    private Date modifiedDate;
    @Expose
    @DatabaseField
    private boolean uploadedToServer = false;

    public Song getSong() {
        return song;
    }

    public void setSong(Song song) {
        this.song = song;
    }

    public boolean isFavourite() {
        return favourite;
    }

    public void setFavourite(boolean favourite) {
        this.favourite = favourite;
    }

    public boolean isFavouriteNotPublished() {
        return !favouritePublished;
    }

    public void setFavouritePublished(boolean favouritePublished) {
        this.favouritePublished = favouritePublished;
    }

    public Date getModifiedDate() {
        return modifiedDate;
    }

    public void setModifiedDate(Date modifiedDate) {
        this.modifiedDate = modifiedDate;
    }

    public boolean isUploadedToServer() {
        return uploadedToServer;
    }

    public void setUploadedToServer(boolean uploadedToServer) {
        this.uploadedToServer = uploadedToServer;
    }

    @Override
    public String toString() {
        return "FavouriteSong{" +
                "song= " + song +
                ", favourite= " + favourite +
                '}';
    }
}
