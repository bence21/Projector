package com.bence.projector.common.dto;

import java.util.Date;
import java.util.List;

public class SuggestionDTO extends BaseDTO {

    private String songId;
    private String title;
    private List<SongVerseDTO> verses;
    private Date createdDate;
    private Boolean applied;
    private String createdByEmail;
    private String description;
    private String youtubeUrl;
    private Boolean reviewed;
    private Date modifiedDate;
    private String lastModifiedByUserEmail;

    public SuggestionDTO() {
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public List<SongVerseDTO> getVerses() {
        return verses;
    }

    public void setVerses(List<SongVerseDTO> verses) {
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

    public String getSongId() {
        return songId;
    }

    public void setSongId(String songId) {
        this.songId = songId;
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

    public void setReviewed(Boolean reviewed) {
        this.reviewed = reviewed;
    }

    public Date getModifiedDate() {
        return modifiedDate;
    }

    public void setModifiedDate(Date modifiedDate) {
        this.modifiedDate = modifiedDate;
    }

    public String getLastModifiedByUserEmail() {
        return lastModifiedByUserEmail;
    }

    public void setLastModifiedByUserEmail(String lastModifiedByUserEmail) {
        this.lastModifiedByUserEmail = lastModifiedByUserEmail;
    }
}
