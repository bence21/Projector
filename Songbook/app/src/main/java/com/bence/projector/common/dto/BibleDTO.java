package com.bence.projector.common.dto;

import java.util.Date;
import java.util.List;

public class BibleDTO extends BaseDTO {

    private String name;
    private String shortName;
    private List<BookDTO> books;
    private String languageUuid;
    private Date createdDate;
    private Date modifiedDate;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getShortName() {
        return shortName;
    }

    public void setShortName(String shortName) {
        this.shortName = shortName;
    }

    public List<BookDTO> getBooks() {
        return books;
    }

    public void setBooks(List<BookDTO> books) {
        this.books = books;
    }

    public String getLanguageUuid() {
        return languageUuid;
    }

    public void setLanguageUuid(String languageUuid) {
        this.languageUuid = languageUuid;
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
}
