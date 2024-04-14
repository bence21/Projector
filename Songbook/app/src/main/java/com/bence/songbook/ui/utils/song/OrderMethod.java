package com.bence.songbook.ui.utils.song;


import androidx.annotation.NonNull;

public enum OrderMethod {
    RELEVANCE("Relevance"),
    ASCENDING_BY_TITLE("Ascending by title"),
    BY_MODIFIED_DATE("By modified date"),
    BY_COLLECTION("By collection"),
    BY_CREATED_DATE("By created date"),
    BY_LAST_ACCESSED("By last accessed"),
    ;
    private String text;

    OrderMethod(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    @NonNull
    @Override
    public String toString() {
        return text;
    }
}
