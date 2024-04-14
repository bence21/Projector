package projector.model.sqlite;

import com.j256.ormlite.field.DatabaseField;

public class Verses {

    @DatabaseField(uniqueCombo = true, uniqueIndexName = "bookNumberChapterVerse")
    private Long book_number;
    @DatabaseField(uniqueCombo = true, uniqueIndexName = "bookNumberChapterVerse")
    private Long chapter;
    @DatabaseField(uniqueCombo = true, uniqueIndexName = "bookNumberChapterVerse")
    private Long verse;
    @DatabaseField
    private String text;

    private static long getSimpleLong(Long aLong) {
        if (aLong == null) {
            return 0;
        }
        return aLong;
    }

    public Long getBook_number() {
        return book_number;
    }

    public void setBook_number(Long book_number) {
        this.book_number = book_number;
    }

    public Long getChapter() {
        return chapter;
    }

    public void setChapter(Long chapter) {
        this.chapter = chapter;
    }

    public Long getVerse() {
        return verse;
    }

    public void setVerse(Long verse) {
        this.verse = verse;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public long getChapterL() {
        return getSimpleLong(getChapter());
    }

    public long getVerseL() {
        return getSimpleLong(getVerse());
    }
}
