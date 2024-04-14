package com.bence.projector.server.backend.model;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Table(
        indexes = {
                @Index(name = "indexNrBibleVerse", columnList = "indexNumber, bible_verse_id", unique = true)
        }
)
@Entity
public class VerseIndex {
    @Id
    @GeneratedValue
    private Long id;
    private Long indexNumber;
    @ManyToOne(fetch = FetchType.LAZY)
    private BibleVerse bibleVerse;
    @ManyToOne(fetch = FetchType.LAZY)
    private Bible bible;

    public BibleVerse getBibleVerse() {
        return bibleVerse;
    }

    void setBibleVerse(BibleVerse bibleVerse) {
        this.bibleVerse = bibleVerse;
    }

    public Long getIndexNumber() {
        return indexNumber;
    }

    public void setIndexNumber(Long indexNumber) {
        this.indexNumber = indexNumber;
    }

    public Bible getBible() {
        return bible;
    }

    public void setBible(Bible bible) {
        this.bible = bible;
    }
}
