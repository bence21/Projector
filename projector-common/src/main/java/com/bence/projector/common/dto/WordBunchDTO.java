package com.bence.projector.common.dto;

public class WordBunchDTO {

    private String word;
    private int count;
    private SongTitleDTO song;
    private boolean problematic;
    private ReviewedWordDTO reviewedWord;

    public void setWord(String word) {
        this.word = word;
    }

    public String getWord() {
        return word;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public int getCount() {
        return count;
    }

    public void setSong(SongTitleDTO song) {
        this.song = song;
    }

    public SongTitleDTO getSong() {
        return song;
    }

    public void setProblematic(boolean problematic) {
        this.problematic = problematic;
    }

    public boolean isProblematic() {
        return problematic;
    }

    public void setReviewedWord(ReviewedWordDTO reviewedWord) {
        this.reviewedWord = reviewedWord;
    }

    public ReviewedWordDTO getReviewedWord() {
        return reviewedWord;
    }
}
