package projector.application;

import com.google.gson.annotations.Expose;

import java.util.ArrayList;
import java.util.List;

public class ReferenceBookState {

    @Expose
    private int bookNumber;
    @Expose
    private List<ReferenceChapterState> chapters = new ArrayList<>();

    public int getBookNumber() {
        return bookNumber;
    }

    public void setBookNumber(int bookNumber) {
        this.bookNumber = bookNumber;
    }

    public List<ReferenceChapterState> getChapters() {
        return chapters;
    }

    public void setChapters(List<ReferenceChapterState> chapters) {
        this.chapters = chapters != null ? chapters : new ArrayList<>();
    }
}
