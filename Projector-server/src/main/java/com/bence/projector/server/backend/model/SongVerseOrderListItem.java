package com.bence.projector.server.backend.model;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;

@Entity
public class SongVerseOrderListItem extends BaseEntity {

    private Short position;
    @ManyToOne(fetch = FetchType.LAZY)
    private Song song;

    public SongVerseOrderListItem() {

    }

    public SongVerseOrderListItem(SongVerseOrderListItem item) {
        this.position = item.position;
        this.song = item.song;
    }

    @Override
    public String toString() {
        return position + "";
    }

    public void setSong(Song song) {
        this.song = song;
    }

    public short getPosition() {
        if (position == null) {
            return 0;
        }
        return position;
    }

    public void setPosition(Short position) {
        this.position = position;
    }
}
