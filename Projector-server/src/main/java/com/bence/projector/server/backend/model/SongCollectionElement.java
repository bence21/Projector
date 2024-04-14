package com.bence.projector.server.backend.model;

import com.bence.projector.server.utils.interfaces.MatchesInterface;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;

@Entity
public class SongCollectionElement extends BaseEntity implements MatchesInterface<SongCollectionElement> {

    private String ordinalNumber;
    @ManyToOne(fetch = FetchType.LAZY)
    private Song song;
    @ManyToOne(fetch = FetchType.LAZY)
    private SongCollection songCollection;

    public String getOrdinalNumber() {
        return ordinalNumber;
    }

    public void setOrdinalNumber(String ordinalNumber) {
        this.ordinalNumber = ordinalNumber;
    }

    @Override
    public String toString() {
        return ordinalNumber;
    }

    public String getSongUuid() {
        if (song != null) {
            return song.getUuid();
        }
        return null;
    }

    public boolean matches(SongCollectionElement songCollectionElement) {
        if (songCollectionElement == null) {
            return false;
        }
        if (!ordinalNumber.equals(songCollectionElement.ordinalNumber)) {
            return false;
        }
        return isEquals(songCollectionElement.song, this.song);
    }

    private boolean isEquals(Song song, Song other) {
        if (song == null) {
            return false;
        }
        if (other == null) {
            return false;
        }
        return other.getId().equals(song.getId());
    }

    public void setSong(Song song) {
        this.song = song;
    }

    public SongCollection getSongCollection() {
        return songCollection;
    }

    public void setSongCollection(SongCollection songCollection) {
        this.songCollection = songCollection;
    }
}
