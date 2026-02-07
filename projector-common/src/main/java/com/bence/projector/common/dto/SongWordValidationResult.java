package com.bence.projector.common.dto;

import java.util.List;

public class SongWordValidationResult {

    private List<String> unreviewedWords;
    private List<String> bannedWords;
    private List<RejectedWordSuggestion> rejectedWords;
    private boolean hasIssues;
    private List<WordWithStatus> wordsWithStatus;

    public SongWordValidationResult() {
    }

    public SongWordValidationResult(List<String> unreviewedWords, List<String> bannedWords,
                                    List<RejectedWordSuggestion> rejectedWords, boolean hasIssues) {
        this.unreviewedWords = unreviewedWords;
        this.bannedWords = bannedWords;
        this.rejectedWords = rejectedWords;
        this.hasIssues = hasIssues;
    }

    public SongWordValidationResult(List<String> unreviewedWords, List<String> bannedWords,
                                    List<RejectedWordSuggestion> rejectedWords, boolean hasIssues,
                                    List<WordWithStatus> wordsWithStatus) {
        this.unreviewedWords = unreviewedWords;
        this.bannedWords = bannedWords;
        this.rejectedWords = rejectedWords;
        this.hasIssues = hasIssues;
        this.wordsWithStatus = wordsWithStatus;
    }

    public List<String> getUnreviewedWords() {
        return unreviewedWords;
    }

    public void setUnreviewedWords(List<String> unreviewedWords) {
        this.unreviewedWords = unreviewedWords;
    }

    public List<String> getBannedWords() {
        return bannedWords;
    }

    public void setBannedWords(List<String> bannedWords) {
        this.bannedWords = bannedWords;
    }

    public List<RejectedWordSuggestion> getRejectedWords() {
        return rejectedWords;
    }

    public void setRejectedWords(List<RejectedWordSuggestion> rejectedWords) {
        this.rejectedWords = rejectedWords;
    }

    public boolean isHasIssues() {
        return hasIssues;
    }

    public void setHasIssues(boolean hasIssues) {
        this.hasIssues = hasIssues;
    }

    public List<WordWithStatus> getWordsWithStatus() {
        return wordsWithStatus;
    }

    public void setWordsWithStatus(List<WordWithStatus> wordsWithStatus) {
        this.wordsWithStatus = wordsWithStatus;
    }
}
