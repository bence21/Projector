package projector.controller.biblesearch;

import projector.model.Bible;

public final class SearchHit {

    private final Bible bible;
    private final int bookIndex;
    private final int chapterIndex;
    private final int verseIndex;
    private final String bookLabel;
    private final String verseText;

    public SearchHit(Bible bible, int bookIndex, int chapterIndex, int verseIndex, String bookLabel, String verseText) {
        this.bible = bible;
        this.bookIndex = bookIndex;
        this.chapterIndex = chapterIndex;
        this.verseIndex = verseIndex;
        this.bookLabel = bookLabel;
        this.verseText = verseText;
    }

    public Bible getBible() {
        return bible;
    }

    public int getBookIndex() {
        return bookIndex;
    }

    public int getChapterIndex() {
        return chapterIndex;
    }

    public int getVerseIndex() {
        return verseIndex;
    }

    public String getBookLabel() {
        return bookLabel;
    }

    public String getVerseText() {
        return verseText;
    }
}
