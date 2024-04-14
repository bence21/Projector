package com.bence.projector.server.backend.model;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Index;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
@Table(
        indexes = {@Index(name = "uuid_index", columnList = "uuid", unique = true)}
)
public class SongCollection extends AbstractModel {
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "songCollection")
    private List<SongCollectionElement> songCollectionElements;
    private Date createdDate;
    private Date modifiedDate;
    private String name;
    @ManyToOne(fetch = FetchType.LAZY)
    private Language language;
    private String originalId;
    private Boolean deleted;
    private Boolean uploaded;

    public SongCollection() {
    }

    public SongCollection(SongCollection songCollection) {
        super(songCollection);
        this.createdDate = songCollection.createdDate;
        this.modifiedDate = songCollection.modifiedDate;
        this.name = songCollection.name;
        this.language = songCollection.language;
        this.originalId = songCollection.originalId;
        this.deleted = songCollection.deleted;
        this.uploaded = songCollection.uploaded;
        this.songCollectionElements = songCollection.songCollectionElements;
    }

    public List<SongCollectionElement> getSongCollectionElements() {
        if (songCollectionElements == null) {
            songCollectionElements = new ArrayList<>();
        }
        return songCollectionElements;
    }

    public void setSongCollectionElements(List<SongCollectionElement> songCollectionElements) {
        if (songCollectionElements != null) {
            for (SongCollectionElement songCollectionElement : songCollectionElements) {
                songCollectionElement.setSongCollection(this);
            }
        }
        this.songCollectionElements = songCollectionElements;
    }

    public Date getCreatedDate() {
        return createdDate == null ? null : (Date) createdDate.clone();
    }

    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate == null ? null : (Date) createdDate.clone();
    }

    public Date getModifiedDate() {
        return modifiedDate == null ? null : (Date) modifiedDate.clone();
    }

    public void setModifiedDate(Date modifiedDate) {
        this.modifiedDate = modifiedDate == null ? null : (Date) modifiedDate.clone();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Language getLanguage() {
        return language;
    }

    public void setLanguage(Language language) {
        this.language = language;
    }

    public String getOriginalId() {
        return originalId;
    }

    public void setOriginalId(String originalId) {
        this.originalId = originalId;
    }

    public boolean isDeleted() {
        return deleted != null && deleted;
    }

    public void setDeleted(Boolean deleted) {
        this.deleted = deleted;
    }

    public boolean isUploaded() {
        return uploaded != null && uploaded;
    }

    public void setUploaded(Boolean uploaded) {
        this.uploaded = uploaded;
    }

    @Override
    public String toString() {
        return name;
    }
}
