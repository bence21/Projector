package com.bence.projector.common.dto;

import java.util.Date;

public class SongLinkDTO extends BaseDTO {

    private String songId1;
    private String songId2;
    private String title1;
    private String title2;
    private Date createdDate;
    private Date modifiedDate;
    private Boolean applied;
    private String createdByEmail;

    public String getSongId1() {
        return songId1;
    }

    public void setSongId1(String songId1) {
        this.songId1 = songId1;
    }

    public String getSongId2() {
        return songId2;
    }

    public void setSongId2(String songId2) {
        this.songId2 = songId2;
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

    public Date getModifiedDate() {
        return modifiedDate;
    }

    public void setModifiedDate(Date modifiedDate) {
        this.modifiedDate = modifiedDate;
    }

    public String getTitle1() {
        return title1;
    }

    public void setTitle1(String title1) {
        this.title1 = title1;
    }

    public String getTitle2() {
        return title2;
    }

    public void setTitle2(String title2) {
        this.title2 = title2;
    }
}
