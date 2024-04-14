package com.bence.projector.common.dto;

import java.util.Date;
import java.util.List;

public class SongListDTO extends BaseDTO {

    private String title;
    private String description;
    private Date createdDate;
    private Date modifiedDate;
    private List<SongListElementDTO> songListElements;
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

    public List<SongListElementDTO> getSongListElements() {
        return songListElements;
    }

    public void setSongListElements(List<SongListElementDTO> songListElements) {
        this.songListElements = songListElements;
    }

    public String getCreatedByEmail() {
        return createdByEmail;
    }

    public void setCreatedByEmail(String createdByEmail) {
        this.createdByEmail = createdByEmail;
    }
}
