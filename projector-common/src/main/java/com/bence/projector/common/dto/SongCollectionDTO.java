package com.bence.projector.common.dto;

import java.util.Date;
import java.util.List;

public class SongCollectionDTO extends BaseDTO {
    private Date createdDate;
    private Date modifiedDate;
    private List<SongCollectionElementDTO> songCollectionElements;
    private String name;
    private String languageUuid;

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

    public List<SongCollectionElementDTO> getSongCollectionElements() {
        return songCollectionElements;
    }

    public void setSongCollectionElements(List<SongCollectionElementDTO> songCollectionElements) {
        this.songCollectionElements = songCollectionElements;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLanguageUuid() {
        return languageUuid;
    }

    public void setLanguageUuid(String languageUuid) {
        this.languageUuid = languageUuid;
    }
}
