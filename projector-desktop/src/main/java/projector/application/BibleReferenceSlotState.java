package projector.application;

import com.google.gson.annotations.Expose;

import java.util.ArrayList;
import java.util.List;

public class BibleReferenceSlotState {

    @Expose
    private List<ReferenceBookState> books = new ArrayList<>();

    public List<ReferenceBookState> getBooks() {
        return books;
    }

    public void setBooks(List<ReferenceBookState> books) {
        this.books = books != null ? books : new ArrayList<>();
    }
}
