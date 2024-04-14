package projector.model;

import java.util.LinkedList;
import java.util.List;

public class Reference {

    private final List<ReferenceBook> bookList;
    private Bible bible;

    public Reference() {
        bookList = new LinkedList<>();
    }

    public String getReference() {
        StringBuilder reference = new StringBuilder();
        for (ReferenceBook book : bookList) {
            if (!reference.toString().equals("")) {
                reference.append("\n");
            }
            reference.append(getBookTitle(book));
            ReferenceChapter c = book.getChapters().get(0);
            reference.append(" ").append(c.getChapterNumber()).append(":");
            reference.append(c.getVerses().get(0));
            int k;
            k = getK(c, reference);
            someFunction(c, k, reference);
            for (int j = 1; j < book.getChapters().size(); ++j) {
                ReferenceChapter c1 = book.getChapters().get(j);
                reference.append("\n").append(c1.getChapterNumber()).append(":");
                reference.append(c1.getVerses().get(0));
                k = getK(c1, reference);
                someFunction(c1, k, reference);
            }
        }
        return reference.toString();
    }

    private void someFunction(ReferenceChapter c, int k, StringBuilder reference) {
        if (k < c.getVerses().size()) {
            if (c.getVerses().get(k - 1) + 1 == c.getVerses().get(k)) {
                reference.append("-");
            } else {
                reference.append(", ");
            }
            reference.append(c.getVerses().get(k));
        }
    }

    private String getBookTitle(ReferenceBook referenceBook) {
        Book book = referenceBook.getBook();
        if (book != null) {
            return book.getShortOrTitle().trim();
        }
        if (bible == null) {
            return "";
        }
        List<Book> books = bible.getBooks();
        int bookNumber = referenceBook.getBookNumber();
        if (bookNumber >= books.size()) {
            return "";
        }
        return books.get(bookNumber).getShortOrTitle().trim();
    }

    private int getK(ReferenceChapter c, StringBuilder reference) {
        int k;
        for (k = 1; k < c.getVerses().size() - 1; ++k) {
            if (c.getVerses().get(k + 1) - 1 != c.getVerses().get(k)) {
                if (c.getVerses().get(k - 1) + 1 == c.getVerses().get(k)) {
                    reference.append("-");
                } else {
                    reference.append(", ");
                }
                reference.append(c.getVerses().get(k));
            } else if (c.getVerses().get(k - 1) + 1 != c.getVerses().get(k)) {
                reference.append(", ");
                reference.append(c.getVerses().get(k));
            }
        }
        return k;
    }

    public void setBible(Bible bible) {
        this.bible = bible;
    }

    public void addVerse(int book, int chapter, int vers) {
        for (ReferenceBook i : bookList) {
            if (i.getBookNumber() == book) {
                i.addVerse(chapter, vers);
                return;
            }
        }
        bookList.add(new ReferenceBook(book));
        addVerse(book, chapter, vers);
    }

    public void addVerse(Book book, int chapter, int vers) {
        for (ReferenceBook bookI : bookList) {
            if (bookI.getBook().equivalent(book)) {
                bookI.addVerse(chapter, vers);
                return;
            }
        }
        bookList.add(new ReferenceBook(book));
        addVerse(book, chapter, vers);
    }

    public void addVerse(String reference) {
        String[] split = reference.split(" ");
        int bookIndex = Integer.parseInt(split[0]) - 1;
        int partIndex = Integer.parseInt(split[1]);
        addingVerses(split[2], bookIndex, partIndex);
    }

    private void addingVerses(String s, int bookIndex, int partIndex) {
        if (s.contains(",")) {
            String[] split = s.split(",");
            for (String withoutComma : split) {
                addingVersesWithoutComma(withoutComma, bookIndex, partIndex);
            }
        } else {
            addingVersesWithoutComma(s, bookIndex, partIndex);
        }
    }

    private void addingVersesWithoutComma(String withoutComma, int bookIndex, int partIndex) {
        if (!withoutComma.contains("-")) {
            int verseIndex = Integer.parseInt(withoutComma);
            addVerse(bookIndex, partIndex, verseIndex);
        } else {
            String[] split2 = withoutComma.split("-");
            int fromVerse = Integer.parseInt(split2[0]);
            int toVerse = Integer.parseInt(split2[1]);
            for (int i = fromVerse; i <= toVerse; ++i) {
                addVerse(bookIndex, partIndex, i);
            }
        }
    }

    public List<ReferenceBook> getBookList() {
        return bookList;
    }

    public void removeVerse(int book, int chapter, int verse) {
        for (ReferenceBook i : bookList) {
            if (i.getBookNumber() == book) {
                i.removeVerse(chapter, verse);
                if (i.isEmpty()) {
                    bookList.remove(i);
                }
                return;
            }
        }
    }

    public String getReference(Bible bible) {
        this.bible = bible;
        return getReference();
    }

    public void clear() {
        for (ReferenceBook referenceBook : bookList) {
            referenceBook.clear();
        }
        bookList.clear();
    }
}
