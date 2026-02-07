package com.bence.projector.common.dto;

import java.util.Date;

public class ReviewedWordDTO extends BaseDTO {

    private String word;
    private String normalizedWord;
    private LanguageDTO language;
    private String status;
    private String category;
    private String contextCategory;
    private String contextDescription;
    private String reviewedByEmail;
    private String reviewedByName;
    private Date reviewedDate;
    private String notes;
    private LanguageDTO sourceLanguage;
    private Integer foreignLanguageType;

    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
    }

    public String getNormalizedWord() {
        return normalizedWord;
    }

    public void setNormalizedWord(String normalizedWord) {
        this.normalizedWord = normalizedWord;
    }

    public LanguageDTO getLanguage() {
        return language;
    }

    public void setLanguage(LanguageDTO language) {
        this.language = language;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
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

    public String getReviewedByEmail() {
        return reviewedByEmail;
    }

    public void setReviewedByEmail(String reviewedByEmail) {
        this.reviewedByEmail = reviewedByEmail;
    }

    public String getReviewedByName() {
        return reviewedByName;
    }

    public void setReviewedByName(String reviewedByName) {
        this.reviewedByName = reviewedByName;
    }

    public Date getReviewedDate() {
        return reviewedDate;
    }

    public void setReviewedDate(Date reviewedDate) {
        this.reviewedDate = reviewedDate;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
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
}
