package com.bence.projector.server.backend.model;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Index;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.util.Date;
import java.util.List;

@Entity
@Table(
        indexes = {@Index(name = "uuid_index", columnList = "uuid", unique = true)}
)
public class Bible extends AbstractModel {

    private String name;
    private String shortName;
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "bible")
    private List<Book> books;
    @ManyToOne(fetch = FetchType.LAZY)
    private Language language;
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

    public List<Book> getBooks() {
        return books;
    }

    public void setBooks(List<Book> books) {
        setBibleToBooks(books);
        this.books = books;
    }

    private void setBibleToBooks(List<Book> books) {
        if (books == null) {
            return;
        }
        for (Book book : books) {
            book.setBible(this);
        }
    }

    public Language getLanguage() {
        return language;
    }

    public void setLanguage(Language language) {
        this.language = language;
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

    @SuppressWarnings("unused")
    public void linkToVerseIndices() {
        for (Book book : getBooks()) {
            book.linkBibleToVerseIndices(this);
        }
    }

    @Override
    public String toString() {
        return name;
    }
}
