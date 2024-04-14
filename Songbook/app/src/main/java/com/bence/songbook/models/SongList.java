package com.bence.songbook.models;

import com.j256.ormlite.dao.ForeignCollection;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class SongList extends BaseEntity {

    @DatabaseField
    private String title;
    @DatabaseField
    private String description;
    @DatabaseField
    private Date createdDate;
    @DatabaseField
    private Date modifiedDate;
    @DatabaseField
    private boolean owned = true;
    @DatabaseField
    private boolean publish = false;
    @ForeignCollectionField
    private ForeignCollection<SongListElement> songListElementForeignList;
    private List<SongListElement> songListElements;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Date getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
    }

    public Date getModifiedDate() {
        return modifiedDate;
    }

    public void setModifiedDate(Date modifiedDate) {
        this.modifiedDate = modifiedDate;
    }

    public boolean isOwned() {
        return owned;
    }

    public void setOwned(boolean owned) {
        this.owned = owned;
    }

    public boolean isPublish() {
        return publish;
    }

    public void setPublish(boolean publish) {
        this.publish = publish;
    }

    public List<SongListElement> getSongListElements() {
        if (songListElements == null) {
            //noinspection ConstantConditions
            if (songListElementForeignList == null) {
                songListElements = new ArrayList<>();
                return songListElements;
            }
            songListElements = new ArrayList<>(songListElementForeignList.size());
            songListElements.addAll(songListElementForeignList);
        }
        return songListElements;
    }

    public void setSongListElements(List<SongListElement> songListElements) {
        this.songListElements = songListElements;
        for (SongListElement element : songListElements) {
            element.setSongList(this);
        }
    }
}
