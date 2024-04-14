package com.bence.projector.server.backend.model;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Index;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.util.Date;
import java.util.List;

@Entity
@Table(
        indexes = {@Index(name = "uuid_index", columnList = "uuid", unique = true)}
)
public class SongList extends AbstractModel {

    private String title;
    private String description;
    private Date createdDate;
    private Date modifiedDate;
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "songList")
    private List<SongListElement> songListElements;
    private String createdByEmail;

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
        int DESCRIPTION_LENGTH = 255;
        this.description = description.substring(0, Math.min(description.length(), DESCRIPTION_LENGTH));
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

    public List<SongListElement> getSongListElements() {
        return songListElements;
    }

    public void setSongListElements(List<SongListElement> songListElements) {
        if (songListElements != null) {
            for (SongListElement songListElement : songListElements) {
                songListElement.setSongList(this);
            }
        }
        this.songListElements = songListElements;
    }

    public String getCreatedByEmail() {
        return createdByEmail;
    }

    public void setCreatedByEmail(String createdByEmail) {
        this.createdByEmail = createdByEmail;
    }
}
