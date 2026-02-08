package com.bence.projector.common.dto;

import java.util.List;

public class RejectedWordSuggestion {

    private String word;
    private String primarySuggestion;
    private List<String> alternativeSuggestions;

    public RejectedWordSuggestion() {
    }

    public RejectedWordSuggestion(String word, String primarySuggestion, List<String> alternativeSuggestions) {
        this.word = word;
        this.primarySuggestion = primarySuggestion;
        this.alternativeSuggestions = alternativeSuggestions;
    }

    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
    }

    public String getPrimarySuggestion() {
        return primarySuggestion;
    }

    public void setPrimarySuggestion(String primarySuggestion) {
        this.primarySuggestion = primarySuggestion;
    }

    public List<String> getAlternativeSuggestions() {
        return alternativeSuggestions;
    }

    public void setAlternativeSuggestions(List<String> alternativeSuggestions) {
        this.alternativeSuggestions = alternativeSuggestions;
    }
}
