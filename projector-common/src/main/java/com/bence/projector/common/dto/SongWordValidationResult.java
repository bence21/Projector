package com.bence.projector.common.dto;

import java.util.ArrayList;
import java.util.List;

public class SongWordValidationResult {

    private List<String> unreviewedWords;
    private List<String> bannedWords;
    private List<RejectedWordSuggestion> rejectedWords;
    private boolean hasIssues;
    /**
     * True when banned or rejected words are present (blocks publication).
     */
    private boolean hasBlockingIssues;
    /**
     * Sum of tier-weighted word scores; higher means better reviewed quality.
     */
    private int wordQualityScore;
    private List<WordWithStatus> wordsWithStatus;
    private boolean hasMixedLanguageWarning;
    private int foreignWordCount;
    private int totalReviewedWordCount;
    private double foreignWordRatio;
    private List<String> foreignLanguages;

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
        this.foreignLanguages = new ArrayList<>();
    }

    public SongWordValidationResult(List<String> unreviewedWords, List<String> bannedWords,
                                    List<RejectedWordSuggestion> rejectedWords, boolean hasIssues,
                                    List<WordWithStatus> wordsWithStatus, boolean hasMixedLanguageWarning,
                                    int foreignWordCount, int totalReviewedWordCount, double foreignWordRatio,
                                    List<String> foreignLanguages) {
        this(unreviewedWords, bannedWords, rejectedWords, hasIssues, false, 0,
                wordsWithStatus, hasMixedLanguageWarning, foreignWordCount, totalReviewedWordCount, foreignWordRatio,
                foreignLanguages);
    }

    public SongWordValidationResult(List<String> unreviewedWords, List<String> bannedWords,
                                    List<RejectedWordSuggestion> rejectedWords, boolean hasIssues,
                                    boolean hasBlockingIssues, int wordQualityScore,
                                    List<WordWithStatus> wordsWithStatus, boolean hasMixedLanguageWarning,
                                    int foreignWordCount, int totalReviewedWordCount, double foreignWordRatio,
                                    List<String> foreignLanguages) {
        this.unreviewedWords = unreviewedWords;
        this.bannedWords = bannedWords;
        this.rejectedWords = rejectedWords;
        this.hasIssues = hasIssues;
        this.hasBlockingIssues = hasBlockingIssues;
        this.wordQualityScore = wordQualityScore;
        this.wordsWithStatus = wordsWithStatus;
        this.hasMixedLanguageWarning = hasMixedLanguageWarning;
        this.foreignWordCount = foreignWordCount;
        this.totalReviewedWordCount = totalReviewedWordCount;
        this.foreignWordRatio = foreignWordRatio;
        this.foreignLanguages = foreignLanguages;
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

    public boolean isHasBlockingIssues() {
        return hasBlockingIssues;
    }

    public void setHasBlockingIssues(boolean hasBlockingIssues) {
        this.hasBlockingIssues = hasBlockingIssues;
    }

    public int getWordQualityScore() {
        return wordQualityScore;
    }

    public void setWordQualityScore(int wordQualityScore) {
        this.wordQualityScore = wordQualityScore;
    }

    public List<WordWithStatus> getWordsWithStatus() {
        return wordsWithStatus;
    }

    public void setWordsWithStatus(List<WordWithStatus> wordsWithStatus) {
        this.wordsWithStatus = wordsWithStatus;
    }

    public boolean isHasMixedLanguageWarning() {
        return hasMixedLanguageWarning;
    }

    public void setHasMixedLanguageWarning(boolean hasMixedLanguageWarning) {
        this.hasMixedLanguageWarning = hasMixedLanguageWarning;
    }

    public int getForeignWordCount() {
        return foreignWordCount;
    }

    public void setForeignWordCount(int foreignWordCount) {
        this.foreignWordCount = foreignWordCount;
    }

    public int getTotalReviewedWordCount() {
        return totalReviewedWordCount;
    }

    public void setTotalReviewedWordCount(int totalReviewedWordCount) {
        this.totalReviewedWordCount = totalReviewedWordCount;
    }

    public double getForeignWordRatio() {
        return foreignWordRatio;
    }

    public void setForeignWordRatio(double foreignWordRatio) {
        this.foreignWordRatio = foreignWordRatio;
    }

    public List<String> getForeignLanguages() {
        return foreignLanguages;
    }

    public void setForeignLanguages(List<String> foreignLanguages) {
        this.foreignLanguages = foreignLanguages;
    }
}
