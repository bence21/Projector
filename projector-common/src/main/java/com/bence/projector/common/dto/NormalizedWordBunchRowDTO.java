package com.bence.projector.common.dto;

/**
 * DTO for a single row in the words spell checker table (one word occurrence).
 * Used by the paginated spell checker API.
 */
public class NormalizedWordBunchRowDTO {

    private int nr;
    private double confidencePercentage;
    private String word;
    private int count;
    private String correction;
    private SongTitleDTO song;
    private boolean problematic;
    private Boolean allOccurrencesAutoCapitalized;
    private ReviewedWordDTO reviewedWord;
    /** True when word and bestWord had different lengths; correction is shown as-is (no case applied). */
    private boolean correctionLengthMismatch;

    public int getNr() {
        return nr;
    }

    public void setNr(int nr) {
        this.nr = nr;
    }

    public double getConfidencePercentage() {
        return confidencePercentage;
    }

    public void setConfidencePercentage(double confidencePercentage) {
        this.confidencePercentage = confidencePercentage;
    }

    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public String getCorrection() {
        return correction;
    }

    public void setCorrection(String correction) {
        this.correction = correction;
    }

    public SongTitleDTO getSong() {
        return song;
    }

    public void setSong(SongTitleDTO song) {
        this.song = song;
    }

    public boolean isProblematic() {
        return problematic;
    }

    public void setProblematic(boolean problematic) {
        this.problematic = problematic;
    }

    public Boolean getAllOccurrencesAutoCapitalized() {
        return allOccurrencesAutoCapitalized;
    }

    public void setAllOccurrencesAutoCapitalized(Boolean allOccurrencesAutoCapitalized) {
        this.allOccurrencesAutoCapitalized = allOccurrencesAutoCapitalized;
    }

    public ReviewedWordDTO getReviewedWord() {
        return reviewedWord;
    }

    public void setReviewedWord(ReviewedWordDTO reviewedWord) {
        this.reviewedWord = reviewedWord;
    }

    public boolean isCorrectionLengthMismatch() {
        return correctionLengthMismatch;
    }

    public void setCorrectionLengthMismatch(boolean correctionLengthMismatch) {
        this.correctionLengthMismatch = correctionLengthMismatch;
    }
}
