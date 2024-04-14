package com.bence.projector.server.backend.model;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import java.util.List;

@Entity
public class Chapter extends BaseEntity {
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "chapter")
    private List<BibleVerse> verses;
    @ManyToOne(fetch = FetchType.LAZY)
    private Book book;

    public List<BibleVerse> getVerses() {
        return verses;
    }

    public void setVerses(List<BibleVerse> verses) {
        for (BibleVerse bibleVerse : verses) {
            bibleVerse.setChapter(this);
        }
        this.verses = verses;
    }

    public void setBook(Book book) {
        this.book = book;
    }

    public void linkBibleToVerseIndices(Bible bible) {
        for (BibleVerse bibleVerse : getVerses()) {
            bibleVerse.linkBibleToVerseIndices(bible);
        }
    }
}