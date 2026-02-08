package com.bence.projector.common.dto;

import java.util.List;

public class WordWithStatus {

    private String word;
    private ReviewedWordStatusDTO status;
    private List<String> suggestions;
    private Integer countInSong;
    private Integer countInAllSongs;
    private String category;
    private String notes;
    private String contextCategory;
    private String contextDescription;
    private LanguageDTO sourceLanguage;
    private Integer foreignLanguageType;

    public WordWithStatus() {
    }

    public WordWithStatus(String word, ReviewedWordStatusDTO status, List<String> suggestions) {
        this.word = word;
        this.status = status;
        this.suggestions = suggestions;
    }

    public WordWithStatus(String word, ReviewedWordStatusDTO status, List<String> suggestions, Integer countInSong, Integer countInAllSongs) {
        this.word = word;
        this.status = status;
        this.suggestions = suggestions;
        this.countInSong = countInSong;
        this.countInAllSongs = countInAllSongs;
    }

    public WordWithStatus(String word, ReviewedWordStatusDTO status, List<String> suggestions, Integer countInSong, Integer countInAllSongs, String category, String notes) {
        this.word = word;
        this.status = status;
        this.suggestions = suggestions;
        this.countInSong = countInSong;
        this.countInAllSongs = countInAllSongs;
        this.category = category;
        this.notes = notes;
    }

    public WordWithStatus(String word, ReviewedWordStatusDTO status, List<String> suggestions, Integer countInSong, Integer countInAllSongs, String category, String notes, String contextCategory, String contextDescription) {
        this.word = word;
        this.status = status;
        this.suggestions = suggestions;
        this.countInSong = countInSong;
        this.countInAllSongs = countInAllSongs;
        this.category = category;
        this.notes = notes;
        this.contextCategory = contextCategory;
        this.contextDescription = contextDescription;
    }

    public WordWithStatus(String word, ReviewedWordStatusDTO status, List<String> suggestions, Integer countInSong, Integer countInAllSongs, String category, String notes, String contextCategory, String contextDescription, LanguageDTO sourceLanguage, Integer foreignLanguageType) {
        this.word = word;
        this.status = status;
        this.suggestions = suggestions;
        this.countInSong = countInSong;
        this.countInAllSongs = countInAllSongs;
        this.category = category;
        this.notes = notes;
        this.contextCategory = contextCategory;
        this.contextDescription = contextDescription;
        this.sourceLanguage = sourceLanguage;
        this.foreignLanguageType = foreignLanguageType;
    }

    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
    }

    public ReviewedWordStatusDTO getStatus() {
        return status;
    }

    public void setStatus(ReviewedWordStatusDTO status) {
        this.status = status;
    }

    public List<String> getSuggestions() {
        return suggestions;
    }

    public void setSuggestions(List<String> suggestions) {
        this.suggestions = suggestions;
    }

    public Integer getCountInSong() {
        return countInSong;
    }

    public void setCountInSong(Integer countInSong) {
        this.countInSong = countInSong;
    }

    public Integer getCountInAllSongs() {
        return countInAllSongs;
    }

    public void setCountInAllSongs(Integer countInAllSongs) {
        this.countInAllSongs = countInAllSongs;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getContextCategory() {
        return contextCategory;
    }

    public void setContextCategory(String contextCategory) {
        this.contextCategory = contextCategory;
    }

    public String getContextDescription() {
        return contextDescription;
    }

    public void setContextDescription(String contextDescription) {
        this.contextDescription = contextDescription;
    }

    public LanguageDTO getSourceLanguage() {
        return sourceLanguage;
    }

    public void setSourceLanguage(LanguageDTO sourceLanguage) {
        this.sourceLanguage = sourceLanguage;
    }

    public Integer getForeignLanguageType() {
        return foreignLanguageType;
    }

    public void setForeignLanguageType(Integer foreignLanguageType) {
        this.foreignLanguageType = foreignLanguageType;
    }

    private String statusSymbol() {
        if (status == null) return "?";
        switch (status) {
            case REVIEWED_GOOD:
            case CONTEXT_SPECIFIC:
            case ACCEPTED:
            case AUTO_ACCEPTED_FROM_PUBLIC:
                return "✓";
            case UNREVIEWED:
                return "?";
            case BANNED:
            case REJECTED:
                return "✗";
            default:
                return status.getValue();
        }
    }

    private String formattedSuggestions() {
        if (suggestions == null || suggestions.isEmpty()) return "";
        return " → " + String.join(", ", suggestions);
    }

    private String formattedCounts() {
        if (countInSong == null && countInAllSongs == null) return "";
        int inSong = countInSong != null ? countInSong : 0;
        int inAll = countInAllSongs != null ? countInAllSongs : 0;
        return String.format(" [%d in song, %d total]", inSong, inAll);
    }

    @Override
    public String toString() {
        String sb = (word != null ? word : "") +
                " " + statusSymbol() +
                formattedSuggestions() +
                formattedCounts();
        return sb.trim();
    }
}
