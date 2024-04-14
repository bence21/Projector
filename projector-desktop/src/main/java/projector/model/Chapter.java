package projector.model;

import com.j256.ormlite.dao.ForeignCollection;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;

import java.util.ArrayList;
import java.util.List;

public class Chapter extends BaseEntity {

    @ForeignCollectionField
    private ForeignCollection<BibleVerse> bibleVerseForeignCollection;
    private List<BibleVerse> verses;
    private int length;
    @DatabaseField(foreign = true, foreignAutoRefresh = true, index = true)
    private Book book;
    @DatabaseField
    private short number;

    public List<BibleVerse> getVerses() {
        if (verses == null && bibleVerseForeignCollection != null) {
            int initialCapacity = bibleVerseForeignCollection.size();
            verses = new ArrayList<>(initialCapacity);
            verses.addAll(bibleVerseForeignCollection);
        }
        return verses;
    }

    public void setVerses(List<BibleVerse> verses) {
        for (BibleVerse bibleVerse : verses) {
            bibleVerse.setChapter(this);
        }
        this.verses = verses;
        length = verses.size();
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public Book getBook() {
        return book;
    }

    public void setBook(Book book) {
        this.book = book;
    }

    public short getNumber() {
        return number;
    }

    public void setNumber(short number) {
        this.number = number;
    }

    @Override
    public String toString() {
        return number + "";
    }

    public BibleVerse getVerse(int index) {
        List<BibleVerse> bibleVerses = getVerses();
        if (bibleVerses == null || index < 0 || index >= bibleVerses.size()) {
            return null;
        }
        return bibleVerses.get(index);
    }
}