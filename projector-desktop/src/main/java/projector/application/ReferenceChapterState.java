package projector.application;

import com.google.gson.annotations.Expose;

import java.util.ArrayList;
import java.util.List;

public class ReferenceChapterState {

    @Expose
    private int chapterNumber;
    @Expose
    private List<Integer> verses = new ArrayList<>();

    public int getChapterNumber() {
        return chapterNumber;
    }

    public void setChapterNumber(int chapterNumber) {
        this.chapterNumber = chapterNumber;
    }

    public List<Integer> getVerses() {
        return verses;
    }

    public void setVerses(List<Integer> verses) {
        this.verses = verses != null ? new ArrayList<>(verses) : new ArrayList<>();
    }
}
