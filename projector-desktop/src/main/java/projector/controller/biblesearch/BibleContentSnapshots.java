package projector.controller.biblesearch;

import projector.model.Bible;
import projector.model.BibleVerse;
import projector.model.Book;
import projector.model.Chapter;

import java.util.ArrayList;
import java.util.List;

public final class BibleContentSnapshots {

    private BibleContentSnapshots() {
    }

    public static List<Book> books(Bible bible) {
        return snapshot(bible != null ? bible.getBooks() : null);
    }

    public static List<Chapter> chapters(Book book) {
        return snapshot(book != null ? book.getChapters() : null);
    }

    public static List<BibleVerse> verses(Chapter chapter) {
        try {
            return snapshot(chapter != null ? chapter.getVerses() : null);
        } catch (RuntimeException e) {
            return List.of();
        }
    }

    private static <T> List<T> snapshot(List<T> source) {
        if (source == null || source.isEmpty()) {
            return List.of();
        }
        List<T> copy = new ArrayList<>(source.size());
        for (int i = 0; i < source.size(); i++) {
            try {
                copy.add(source.get(i));
            } catch (IndexOutOfBoundsException ignored) {
                break;
            }
        }
        return copy;
    }
}
