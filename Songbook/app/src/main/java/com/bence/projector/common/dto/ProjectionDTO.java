package com.bence.projector.common.dto;

import com.google.gson.annotations.Expose;

import java.util.List;

public class ProjectionDTO {

    @Expose
    private List<Long> verseIndices;
    @Expose
    private String selectedBibleUuid;
    @Expose
    private String selectedBibleName;
    @Expose
    private int selectedBook;
    @Expose
    private int selectedPart;
    @Expose
    private List<Integer> verseIndicesByPart;

    public List<Long> getVerseIndices() {
        return verseIndices;
    }

    public void setVerseIndices(List<Long> verseIndices) {
        this.verseIndices = verseIndices;
    }

    public String getSelectedBibleUuid() {
        return selectedBibleUuid;
    }

    public void setSelectedBibleUuid(String selectedBibleUuid) {
        this.selectedBibleUuid = selectedBibleUuid;
    }

    public String getSelectedBibleName() {
        return selectedBibleName;
    }

    public void setSelectedBibleName(String selectedBibleName) {
        this.selectedBibleName = selectedBibleName;
    }

    public int getSelectedBook() {
        return selectedBook;
    }

    public void setSelectedBook(int selectedBook) {
        this.selectedBook = selectedBook;
    }

    public int getSelectedPart() {
        return selectedPart;
    }

    public void setSelectedPart(int selectedPart) {
        this.selectedPart = selectedPart;
    }

    public List<Integer> getVerseIndicesByPart() {
        return verseIndicesByPart;
    }

    public void setVerseIndicesByPart(List<Integer> verseIndicesByPart) {
        this.verseIndicesByPart = verseIndicesByPart;
    }
}
