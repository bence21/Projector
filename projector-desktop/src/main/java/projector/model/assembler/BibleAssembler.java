package projector.model.assembler;

import projector.model.Bible;
import projector.model.BibleVerse;
import projector.model.Book;
import projector.model.Chapter;
import projector.model.sqlite.Books;
import projector.model.sqlite.Verses;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BibleAssembler {

    private static BibleAssembler instance;

    private BibleAssembler() {

    }

    public static BibleAssembler getInstance() {
        if (instance == null) {
            instance = new BibleAssembler();
        }
        return instance;
    }

    private static String getParsedText(String text) {
        String s = text
                .replaceAll("<pb/>", "\n")
                .replaceAll("</pb>", "\n")
                .replaceAll("^<pb/>", "\t")
                .replaceAll("<t>", "\n")
                .replaceAll("</t>", "")
                .replaceAll("<e>", "[")
                .replaceAll("</e>", "]")
                .replaceAll("<i>(.*)</i>", "[$1]")
                .replaceAll("<i>", "[")
                .replaceAll("</i>", "]")
                .replaceAll("<f>.*</f>", "")
                .replaceAll("<J>", "")
                .replaceAll("</J>", "")
                .replaceAll("<n>", "[")
                .replaceAll("</n>", "]")
                .replaceAll("<n>(\\[.*])</n>", "$1")
                .replaceAll("<n>(.*)</n>", "[$1]")
                .replaceAll("<!--end of footnotes-->", "")
                .replaceAll("<br/>", "\n")
                .replaceAll("<!--end of crossrefs-->", "");
        s = s.trim();
//        s = text;
        if (s.contains("<")) {
            System.out.println(s);
        }
        return s;
    }

    public Bible createBible(List<Books> pBooks, List<Verses> pVerses, String bibleName, String bibleShortName) {
        Bible bible = new Bible();
        bible.setCreatedDate(new Date());
        bible.setModifiedDate(bible.getCreatedDate());
        bible.setName(bibleName);
        bible.setShortName(bibleShortName);
        List<Book> books = new ArrayList<>();
        Map<Long, Book> bookMap = new HashMap<>();
        for (Books sBook : pBooks) {
            Book book = new Book();
            book.setTitle(sBook.getLong_name());
            book.setShortName(sBook.getShort_name());
            book.setChapters(new ArrayList<>());
            bookMap.put(sBook.getBook_number(), book);
            books.add(book);
        }
        Map<String, Chapter> chaptersMap = new HashMap<>();
        for (Verses sVerse : pVerses) {
            Long bookNumber = sVerse.getBook_number();
            Book book = bookMap.get(bookNumber);
            if (book != null) {
                long chapterNumber = sVerse.getChapterL();
                String key = bookNumber + "_" + chapterNumber;
                Chapter chapter = chaptersMap.get(key);
                if (chapter == null) {
                    chapter = new Chapter();
                    chapter.setNumber((short) chapterNumber);
                    chapter.setVerses(new ArrayList<>());
                    chapter.setBook(book);
                    book.getChapters().add(chapter);
                    chaptersMap.put(key, chapter);
                }
                BibleVerse bibleVerse = new BibleVerse();
                bibleVerse.setNumber((short) sVerse.getVerseL());
                bibleVerse.setChapter(chapter);
                bibleVerse.setText(getParsedText(sVerse.getText()));
                chapter.getVerses().add(bibleVerse);
            }
        }
        bible.setBooks(books);
        return bible;
    }
}
