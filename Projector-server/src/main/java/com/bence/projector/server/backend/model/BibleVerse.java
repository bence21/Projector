package com.bence.projector.server.backend.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import java.util.List;

@Entity
public class BibleVerse extends BaseEntity {

    @Column(length = 1000)
    private String text;
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "bibleVerse")
    private List<VerseIndex> verseIndices;
    @ManyToOne(fetch = FetchType.LAZY)
    private Chapter chapter;

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    @Override
    public String toString() {
        return text;
    }

    public List<VerseIndex> getVerseIndices() {
        return verseIndices;
    }

    public void setVerseIndices(List<VerseIndex> verseIndices) {
        for (VerseIndex verseIndex : verseIndices) {
            verseIndex.setBibleVerse(this);
        }
        this.verseIndices = verseIndices;
    }

    public Chapter getChapter() {
        return chapter;
    }

    public void setChapter(Chapter chapter) {
        this.chapter = chapter;
    }

    public void linkBibleToVerseIndices(Bible bible) {
        for (VerseIndex verseIndex : getVerseIndices()) {
            verseIndex.setBible(bible);
        }
    }
}
