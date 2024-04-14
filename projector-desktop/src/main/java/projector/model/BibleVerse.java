package projector.model;

import com.j256.ormlite.dao.ForeignCollection;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

import static projector.utils.StringUtils.stripAccents;

public class BibleVerse extends BaseEntity {
    private static final Logger LOG = LoggerFactory.getLogger(BibleVerse.class);
    private final int MAX_WIDTH = 1000;
    @ForeignCollectionField
    private ForeignCollection<VerseIndex> verseIndexForeignCollection;
    @DatabaseField(width = MAX_WIDTH)
    private String text;
    @DatabaseField(width = MAX_WIDTH)
    private String strippedText;
    @DatabaseField(foreign = true, foreignAutoRefresh = true, index = true)
    private Chapter chapter;
    private List<VerseIndex> verseIndices;
    @DatabaseField
    private short number;

    public String getText() {
        return text;
    }

    public void setText(String text) {
        if (text.length() > MAX_WIDTH) {
            LOG.warn("\nText to longer then " + MAX_WIDTH + "!: " + text + "\n");
            text = text.substring(0, MAX_WIDTH);
        }
        this.text = text;
        strippedText = stripAccents(text.toLowerCase());
    }

    public Chapter getChapter() {
        return chapter;
    }

    public void setChapter(Chapter chapter) {
        this.chapter = chapter;
    }

    public String getStrippedText() {
        return strippedText;
    }

    public void setStrippedText(String strippedText) {
        this.strippedText = strippedText;
    }

    @Override
    public String toString() {
        return text;
    }

    public List<VerseIndex> getVerseIndices() {
        if (verseIndices == null && verseIndexForeignCollection != null) {
            verseIndices = new ArrayList<>(verseIndexForeignCollection.size());
            verseIndices.addAll(verseIndexForeignCollection);
        }
        return verseIndices;
    }

    public void setVerseIndices(List<VerseIndex> verseIndices) {
        for (VerseIndex verseIndex : verseIndices) {
            verseIndex.setBibleVerse(this);
        }
        this.verseIndices = verseIndices;
    }

    public short getNumber() {
        return number;
    }

    public void setNumber(short number) {
        this.number = number;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (!(obj instanceof BibleVerse bibleVerse)) {
            return false;
        }
        Long id = getId();
        if (id != null && bibleVerse.getId() != null && id.equals(bibleVerse.getId())) {
            return true;
        }
        if (getUuid() != null && bibleVerse.getUuid() != null && getUuid().equals(bibleVerse.getUuid())) {
            return true;
        }
        return super.equals(obj);
    }
}
