package projector.model;

import projector.application.Settings;

import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

public class ReferenceBook {

    private Book book;
    private List<ReferenceChapter> chapters;
    private int bookNumber;

    ReferenceBook(int bookNumber) {
        this.bookNumber = bookNumber;
        chapters = new LinkedList<>();
    }

    ReferenceBook(Book book) {
        this.book = book;
        chapters = new LinkedList<>();
    }

    private void addChapter(int index) {
        chapters.add(new ReferenceChapter(index));
    }

    public List<ReferenceChapter> getChapters() {
        if (Settings.getInstance().isReferenceChapterSorting()) {
            chapters.sort(Comparator.comparingInt(ReferenceChapter::getChapterNumber));
        }
        if (Settings.getInstance().isReferenceVerseSorting()) {
            for (ReferenceChapter i : chapters) {
                i.sort();
            }
        }
        return chapters;
    }

    void addVerse(int chapter, int verse) {
        for (ReferenceChapter chapter1 : chapters) {
            if (chapter1.getChapterNumber() == chapter) {
                chapter1.addVers(verse);
                return;
            }
        }
        addChapter(chapter);
        addVerse(chapter, verse);
    }

    public int getBookNumber() {
        return bookNumber;
    }

    void removeVerse(int chapter, int verse) {
        for (int i = 0; i < chapters.size(); ++i) {
            if (chapters.get(i).getChapterNumber() == chapter) {
                chapters.get(i).removeVers(verse);
                if (chapters.get(i).isEmpty()) {
                    chapters.remove(i);
                }
                return;
            }
        }
    }

    public boolean isEmpty() {
        return chapters.isEmpty();
    }

    public void clear() {
        for (ReferenceChapter referenceChapter : chapters) {
            referenceChapter.clear();
        }
        chapters.clear();
    }

    public Book getBook() {
        return book;
    }

    public void setBook(Book book) {
        this.book = book;
    }
}
