package com.bence.projector.server.mailsending;

import com.bence.projector.server.backend.model.Song;

public class NewSongRow {
    private Song song;
    private boolean hasUnreviewedWords;
    private int unreviewedWordCount;
    private int totalUniqueWordCount;
    private String unreviewedWordRatio;

    public Song getSong() {
        return song;
    }

    public void setSong(Song song) {
        this.song = song;
    }

    public boolean isHasUnreviewedWords() {
        return hasUnreviewedWords;
    }

    public void setHasUnreviewedWords(boolean hasUnreviewedWords) {
        this.hasUnreviewedWords = hasUnreviewedWords;
    }

    public int getUnreviewedWordCount() {
        return unreviewedWordCount;
    }

    public void setUnreviewedWordCount(int unreviewedWordCount) {
        this.unreviewedWordCount = unreviewedWordCount;
    }

    public int getTotalUniqueWordCount() {
        return totalUniqueWordCount;
    }

    public void setTotalUniqueWordCount(int totalUniqueWordCount) {
        this.totalUniqueWordCount = totalUniqueWordCount;
    }

    public String getUnreviewedWordRatio() {
        return unreviewedWordRatio;
    }

    public void setUnreviewedWordRatio(String unreviewedWordRatio) {
        this.unreviewedWordRatio = unreviewedWordRatio;
    }

    public String getTitle() {
        return song != null ? song.getTitle() : null;
    }

    public String getUuid() {
        return song != null ? song.getUuid() : null;
    }

    public String getCreatedByEmail() {
        return song != null ? song.getCreatedByEmail() : null;
    }

    public boolean isDeleted() {
        return song != null && song.isDeleted();
    }

    public boolean isReviewerErased() {
        return song != null && song.isReviewerErased();
    }
}
