package projector.model;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class ReferenceChapter {

    private List<Integer> verses;
    private int chapterNumber;

    ReferenceChapter(int chapterNumber) {
        this.chapterNumber = chapterNumber;
        verses = new LinkedList<>();
    }

    void addVers(int index) {
        if (!verses.contains(index)) {
            verses.add(index);
        }
    }

    public List<Integer> getVerses() {
        return verses;
    }

    public int getChapterNumber() {
        return chapterNumber;
    }

    void sort() {
        Collections.sort(verses);
    }

    void removeVers(int vers) {
        if (verses.contains(vers)) {
            verses.remove((Integer) vers);
        }
    }

    public boolean isEmpty() {
        return verses.isEmpty();
    }

    public void clear() {
        verses.clear();
    }
}
