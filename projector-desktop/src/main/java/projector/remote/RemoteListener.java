package projector.remote;

import projector.model.Bible;
import projector.model.BibleVerse;
import projector.model.Book;
import projector.model.Chapter;

public interface RemoteListener {
    void onBibleListViewSelected(Bible bible);

    void onBookListViewSelected(Book book);

    void onPartListViewSelected(Chapter chapter);

    void onVerseListViewSelected(BibleVerse bibleVerse);
}
