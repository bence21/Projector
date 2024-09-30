package com.bence.projector.server.backend.model;

import com.bence.projector.common.model.SectionType;
import com.bence.projector.server.utils.interfaces.MatchesInterface;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.Transient;
import java.util.List;

@Entity
public class SongVerse extends BaseEntity implements MatchesInterface<SongVerse> {
    private static final int MAX_TEXT_LENGTH = 1000;
    @Column(length = MAX_TEXT_LENGTH)
    private String text;
    private String type;
    private SectionType sectionType = SectionType.VERSE;
    @ManyToOne(fetch = FetchType.LAZY)
    private Song song;
    @ManyToOne(fetch = FetchType.LAZY)
    private Suggestion suggestion;
    @Transient
    private List<String> lines;
    @Transient
    private Short index;

    public SongVerse() {
    }

    @SuppressWarnings("CopyConstructorMissesField")
    public SongVerse(SongVerse songVerse) {
        this.text = songVerse.text;
        this.type = songVerse.type;
        this.sectionType = songVerse.sectionType;
        this.song = songVerse.song;
        this.suggestion = songVerse.suggestion;
        this.index = songVerse.index;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text.substring(0, Math.min(text.length(), MAX_TEXT_LENGTH));
    }

    public boolean isChorus() {
        return sectionType != null && sectionType == SectionType.CHORUS;
    }

    public boolean matches(SongVerse songVerse) {
        return text.equals(songVerse.text) && isChorus() == songVerse.isChorus();
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public SectionType getSectionType() {
        if (isChorus()) {
            sectionType = SectionType.CHORUS;
        }
        return sectionType;
    }

    public void setSectionType(SectionType sectionType) {
        this.sectionType = sectionType;
    }

    public void setSong(Song song) {
        this.song = song;
    }

    public void setSuggestion(Suggestion suggestion) {
        this.suggestion = suggestion;
    }

    public void setTextLines() {
        lines = List.of(getText().split("\n"));
    }

    public List<String> getLines() {
        return lines;
    }

    public void setLines(List<String> lines) {
        this.lines = lines;
    }

    public void setIndex(Short index) {
        this.index = index;
    }

    public Short getIndex() {
        return index;
    }
}
