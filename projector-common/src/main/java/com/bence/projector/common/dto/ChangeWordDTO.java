package com.bence.projector.common.dto;

public class ChangeWordDTO {

    private String word;
    private String correction;
    private Long occurrence;

    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
    }

    public String getCorrection() {
        return correction;
    }

    public void setCorrection(String correction) {
        this.correction = correction;
    }

    public Long getOccurrence() {
        return occurrence;
    }

    public void setOccurrence(Long occurrence) {
        this.occurrence = occurrence;
    }
}
