package com.bence.projector.common.dto;

import java.util.List;

public class ChapterDTO {
    private List<BibleVerseDTO> verses;

    public List<BibleVerseDTO> getVerses() {
        return verses;
    }

    public void setVerses(List<BibleVerseDTO> verses) {
        this.verses = verses;
    }

}