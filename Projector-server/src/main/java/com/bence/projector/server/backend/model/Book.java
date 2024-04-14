package com.bence.projector.server.backend.model;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import java.util.List;

@Entity
public class Book extends BaseEntity {

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "book")
    private List<Chapter> chapters;
    private String title;
    private String shortName;
    @ManyToOne(fetch = FetchType.LAZY)
    private Bible bible;

    public List<Chapter> getChapters() {
        return chapters;
    }

    public void setChapters(List<Chapter> chapters) {
        for (Chapter chapter : chapters) {
            chapter.setBook(this);
        }
        this.chapters = chapters;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getShortName() {
        return shortName;
    }

    public void setShortName(String shortName) {
        this.shortName = shortName;
    }

    public void setBible(Bible bible) {
        this.bible = bible;
    }

    public void linkBibleToVerseIndices(Bible bible) {
        for (Chapter chapter : getChapters()) {
            chapter.linkBibleToVerseIndices(bible);
        }
    }
}
