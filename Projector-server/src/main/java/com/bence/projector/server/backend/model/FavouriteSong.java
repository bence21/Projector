package com.bence.projector.server.backend.model;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import java.util.Date;

@Entity
public class FavouriteSong extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    private User user;
    @ManyToOne(fetch = FetchType.LAZY)
    private Song song;
    private Date modifiedDate;
    private Date serverModifiedDate;
    private boolean favourite;

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Song getSong() {
        return song;
    }

    public void setSong(Song song) {
        this.song = song;
    }

    public String getSongUuid() {
        if (song == null) {
            return null;
        }
        return song.getUuid();
    }

    public Date getModifiedDate() {
        return modifiedDate;
    }

    public void setModifiedDate(Date modifiedDate) {
        this.modifiedDate = modifiedDate;
    }

    public boolean isFavourite() {
        return favourite;
    }

    public void setFavourite(boolean favourite) {
        this.favourite = favourite;
    }

    public Date getServerModifiedDate() {
        return serverModifiedDate;
    }

    public void setServerModifiedDate(Date serverModifiedDate) {
        this.serverModifiedDate = serverModifiedDate;
    }
}
