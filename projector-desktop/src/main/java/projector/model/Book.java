package projector.model;

import com.j256.ormlite.dao.ForeignCollection;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;

import java.util.ArrayList;
import java.util.List;

public class Book extends BaseEntity {

    @ForeignCollectionField
    private ForeignCollection<Chapter> chapterForeignCollection;
    private List<Chapter> chapters;
    @DatabaseField
    private String title;
    @DatabaseField
    private String shortName;
    @DatabaseField(foreign = true, foreignAutoRefresh = true, index = true)
    private Bible bible;

    public List<Chapter> getChapters() {
        if (chapters == null && chapterForeignCollection != null) {
            chapters = new ArrayList<>(chapterForeignCollection.size());
            chapters.addAll(chapterForeignCollection);
        }
        return chapters;
    }

    public void setChapters(List<Chapter> chapters) {
        for (Chapter chapter : chapters) {
            chapter.setBook(this);
        }
        this.chapters = chapters;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getShortName() {
        return shortName;
    }

    public void setShortName(String shortName) {
        this.shortName = shortName;
    }

    public Bible getBible() {
        return bible;
    }

    public void setBible(Bible bible) {
        this.bible = bible;
    }

    @Override
    public String toString() {
        if (shortName == null) {
            return title;
        }
        return shortName;
    }

    public boolean equals(Book other) {
        if (other != null) {
            Long id = getId();
            if (id != null) {
                return id.equals(other.getId());
            } else if (other.getId() != null) {
                return false;
            }
        }
        return super.equals(other);
    }

    public String getShortOrTitle() {
        if (shortName != null && !shortName.trim().equals("")) {
            return shortName;
        }
        return title;
    }

    public Chapter getChapter(int index) {
        List<Chapter> chapterList = getChapters();
        if (chapterList == null || index < 0 || index >= chapterList.size()) {
            return null;
        }
        return chapterList.get(index);
    }
}
