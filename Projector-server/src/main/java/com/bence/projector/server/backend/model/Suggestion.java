package com.bence.projector.server.backend.model;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Index;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;
import java.util.Date;
import java.util.List;

@Entity
@Table(
        indexes = {@Index(name = "uuid_index", columnList = "uuid", unique = true)}
)
public class Suggestion extends AbstractModel {

    @ManyToOne(fetch = FetchType.LAZY)
    private Song song;
    private String title;
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "suggestion")
    private List<SongVerse> verses;
    private Date createdDate;
    private Boolean applied;
    private String createdByEmail;
    private String description;
    private String youtubeUrl;
    private Boolean reviewed;
    private Date modifiedDate;
    @ManyToOne(fetch = FetchType.LAZY)
    private User lastModifiedBy;
    @ManyToMany(fetch = FetchType.LAZY, mappedBy = "suggestionStack")
    private List<NotificationByLanguage> notificationByLanguages;
    @Transient
    private String songUuid;

    public Suggestion() {
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public List<SongVerse> getVerses() {
        return verses;
    }

    public void setVerses(List<SongVerse> verses) {
        if (verses != null) {
            for (SongVerse songVerse : verses) {
                songVerse.setSuggestion(this);
            }
        }
        this.verses = verses;
    }

    public Date getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
    }

    public Boolean getApplied() {
        return applied;
    }

    public void setApplied(Boolean applied) {
        this.applied = applied;
    }

    public String getCreatedByEmail() {
        return createdByEmail;
    }

    public void setCreatedByEmail(String createdByEmail) {
        this.createdByEmail = createdByEmail;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Song getSong() {
        return song;
    }

    public void setSong(Song song) {
        this.song = song;
    }

    public String getYoutubeUrl() {
        return youtubeUrl;
    }

    public void setYoutubeUrl(String youtubeUrl) {
        this.youtubeUrl = youtubeUrl;
    }

    public Boolean getReviewed() {
        return reviewed;
    }

    public boolean isReviewed() {
        return reviewed != null && reviewed;
    }

    public void setReviewed(Boolean reviewed) {
        this.reviewed = reviewed;
    }

    public Date getModifiedDate() {
        if (modifiedDate == null) {
            modifiedDate = createdDate;
        }
        return modifiedDate;
    }

    public void setModifiedDate(Date modifiedDate) {
        this.modifiedDate = modifiedDate;
    }

    public User getLastModifiedBy() {
        return lastModifiedBy;
    }

    public void setLastModifiedBy(User lastModifiedBy) {
        this.lastModifiedBy = lastModifiedBy;
    }

    public String getSongUuid() {
        if (songUuid != null) {
            return songUuid;
        }
        Song song = getSong();
        if (song == null) {
            return null;
        }
        return song.getUuid();
    }

    public void setSongUuid(String songUuid) {
        this.songUuid = songUuid;
    }
}
