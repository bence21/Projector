package projector.model;

import com.j256.ormlite.field.DatabaseField;

public class VerseIndex {
    @DatabaseField(uniqueCombo = true, uniqueIndexName = "indexNrBibleVerse")
    private Long indexNumber;
    @DatabaseField(foreign = true, index = true, uniqueCombo = true, uniqueIndexName = "indexNrBibleVerse")
    private BibleVerse bibleVerse;
    @DatabaseField
    private Long bibleId;

    public VerseIndex() {

    }

    @SuppressWarnings("CopyConstructorMissesField")
    public VerseIndex(VerseIndex verseIndex) {
        this.indexNumber = verseIndex.indexNumber;
    }

    public BibleVerse getBibleVerse() {
        return bibleVerse;
    }

    void setBibleVerse(BibleVerse bibleVerse) {
        this.bibleVerse = bibleVerse;
    }

    public Long getIndexNumber() {
        return indexNumber;
    }

    public void setIndexNumber(Long indexNumber) {
        this.indexNumber = indexNumber;
    }

    public Long getBibleId() {
        return bibleId;
    }

    public void setBibleId(Long bibleId) {
        this.bibleId = bibleId;
    }
}
