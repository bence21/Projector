package projector.model;

import com.bence.projector.common.model.SectionType;
import com.google.gson.annotations.Expose;
import com.j256.ormlite.dao.ForeignCollection;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import projector.utils.CloneUtil;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.bence.projector.common.util.StringUtils.trimLongString100;
import static projector.utils.StringUtils.stripAccents;

public class Song extends BaseEntity {

    private static final long currentDate = new Date().getTime();
    @Expose
    @DatabaseField
    private String title;
    @DatabaseField
    private String strippedTitle;
    @ForeignCollectionField
    private ForeignCollection<SongVerse> songVerseForeignCollection;
    @Expose
    private List<SongVerse> verses;
    @Expose
    @DatabaseField
    private Date createdDate;
    @Expose
    @DatabaseField
    private Date modifiedDate;
    @DatabaseField
    private Date serverModifiedDate;
    private transient boolean deleted;
    //	@Transient
    private String fileText;
    //	@Transient
    //	@ManyToMany(fetch = FetchType.LAZY, mappedBy = "songs")
    private List<SongBook> songBooks;
    @Expose
    @DatabaseField
    private boolean publish = true;
    @Expose
    @DatabaseField
    private boolean published = false;
    @DatabaseField(foreign = true, index = true)
    private Language language;
    private transient List<SongCollection> songCollections;
    private transient List<SongCollectionElement> songCollectionElements;
    @Expose
    @DatabaseField(width = 36)
    private String versionGroup;
    @DatabaseField
    private long views;
    @DatabaseField
    private long favouriteCount;
    @DatabaseField(width = 100)
    private String author;
    @Expose
    @DatabaseField(width = 100)
    private String verseOrder;
    private List<Short> verseOrderList;
    @DatabaseField
    private Boolean downloadedSeparately;
    private transient FavouriteSong favourite;
    private Long savedScore;

    public Song() {
    }

    public Song(String title, List<SongVerse> verses, String fileText, List<SongBook> songBooks) {
        this.title = title;
        this.verses = SongVerse.cloneList(verses);
        this.fileText = fileText;
        this.songBooks = songBooks;
    }

    @SuppressWarnings("CopyConstructorMissesField")
    public Song(Song song) {
        super(song);
        this.title = song.title;
        this.strippedTitle = song.strippedTitle;
        this.verses = SongVerse.cloneList(song.verses);
        this.createdDate = song.createdDate;
        this.modifiedDate = song.modifiedDate;
        this.serverModifiedDate = song.serverModifiedDate;
        this.deleted = song.deleted;
        this.fileText = song.fileText;
        this.songBooks = song.songBooks;
        this.publish = song.publish;
        this.published = song.published;
        this.language = song.language;
        this.songCollections = song.songCollections;
        this.songCollectionElements = song.songCollectionElements;
        this.versionGroup = song.versionGroup;
        this.views = song.views;
        this.favouriteCount = song.favouriteCount;
        this.author = song.author;
        this.verseOrder = song.verseOrder;
        this.verseOrderList = CloneUtil.cloneList(song.verseOrderList);
        this.downloadedSeparately = song.downloadedSeparately;
    }

    private static long getCurrentDate() {
        return currentDate;
    }

    public List<Short> getVerseOrderList() {
        if (verseOrderList == null) {
            verseOrderList = new ArrayList<>();
            if (verseOrder != null) {
                String[] split = verseOrder.split(",");
                for (String s : split) {
                    verseOrderList.add(Short.parseShort(s));
                }
            }
            if (verseOrderList.isEmpty()) {
                short i = 0;
                short chorusIndex = 0;
                boolean wasChorus = false;
                List<SongVerse> verses = getVerses();
                int size = verses.size();
                for (SongVerse verse : verses) {
                    verseOrderList.add(i);
                    if (wasChorus) {
                        SectionType sectionType = verse.getSectionType();
                        boolean nextNotChorus = i >= size - 1 || !verses.get(i + 1).isChorus();
                        if (!sectionType.equals(SectionType.CHORUS) && !sectionType.equals(SectionType.CODA) && nextNotChorus) {
                            verseOrderList.add(chorusIndex);
                        }
                    }
                    if (verse.getSectionType().equals(SectionType.CHORUS)) {
                        chorusIndex = i;
                        wasChorus = true;
                    }
                    ++i;
                }

            }
        }
        return verseOrderList;
    }

    public void setVerseOrderList(List<Short> verseOrderList) {
        this.verseOrderList = verseOrderList;
        if (verseOrderList == null) {
            return;
        }
        StringBuilder s = new StringBuilder();
        boolean first = true;
        for (Short index : verseOrderList) {
            if (!first) {
                s.append(",");
            }
            s.append(index);
            first = false;
        }
        verseOrder = trimLongString100(s.toString());
    }

    public List<SongVerse> getSongVersesByVerseOrder() {
        List<Short> verseOrderList = getVerseOrderList();
        List<SongVerse> songVerses = new ArrayList<>(verseOrderList.size());
        List<SongVerse> verses = getVerses();
        int size = verses.size();
        for (Short index : verseOrderList) {
            if (size > index) {
                songVerses.add(verses.get(index));
            }
        }
        return songVerses;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = trimLongString100(title);
        strippedTitle = stripAccents(this.title.toLowerCase());
    }

    public void setFileText(String fileText) {
        this.fileText = fileText;
    }

    public Date getCreatedDate() {
        return createdDate == null ? null : (Date) createdDate.clone();
    }

    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate == null ? null : (Date) createdDate.clone();
    }

    public Date getModifiedDate() {
        return modifiedDate == null ? new Date(0) : (Date) modifiedDate.clone();
    }

    public void setModifiedDate(Date modifiedDate) {
        this.modifiedDate = modifiedDate == null ? null : (Date) modifiedDate.clone();
    }

    public List<SongVerse> getVerses() {
        if (verses == null) {
            if (songVerseForeignCollection != null) {
                List<SongVerse> songVerses = new ArrayList<>(songVerseForeignCollection.size());
                songVerses.addAll(songVerseForeignCollection);
                verses = songVerses;
            } else {
                verses = new ArrayList<>();
            }
        }
        return verses;
    }

    public void setVerses(List<SongVerse> verseList) {
        for (SongVerse songVerse : verseList) {
            songVerse.setMainSong(this);
        }
        this.verses = verseList;
    }

    public boolean isPublish() {
        return publish;
    }

    public void setPublish(boolean publish) {
        this.publish = publish;
    }

    public boolean isPublished() {
        return published;
    }

    public void setPublished(boolean published) {
        this.published = published;
    }

    public String getStrippedTitle() {
        return strippedTitle;
    }

    public String getVersesText() {
        StringBuilder stringBuilder = new StringBuilder();
        for (SongVerse songVerse : getVerses()) {
            if (!stringBuilder.isEmpty()) {
                stringBuilder.append("\n\n");
            }
            stringBuilder.append(songVerse.getText());
        }
        return stringBuilder.toString();
    }

    public List<SongCollection> getSongCollections() {
        if (songCollections == null) {
            songCollections = new ArrayList<>();
        }
        return songCollections;
    }

    public void setSongCollections(List<SongCollection> songCollections) {
        this.songCollections = songCollections;
    }

    public void addToSongCollections(SongCollection songCollection) {
        if (songCollection == null) {
            return;
        }
        List<SongCollection> songCollections = getSongCollections();
        if (!containsSongCollection(songCollections, songCollection)) {
            songCollections.add(songCollection);
        }
    }

    private boolean containsSongCollection(List<SongCollection> songCollections, SongCollection songCollection) {
        if (songCollection == null) {
            return false;
        }
        for (SongCollection collection : songCollections) {
            if (collection.getId().equals(songCollection.getId())) {
                return true;
            }
        }
        return false;
    }

    public List<SongCollectionElement> getSongCollectionElements() {
        if (songCollectionElements == null) {
            songCollectionElements = new ArrayList<>();
        }
        if (checkNull(songCollectionElements)) {
            return withoutNullSongCollection(songCollectionElements);
        }
        return songCollectionElements;
    }

    public void setSongCollectionElements(List<SongCollectionElement> songCollectionElements) {
        if (songCollectionElements != null) {
            for (SongCollectionElement element : songCollectionElements) {
                element.setSong(this);
            }
        }
        this.songCollectionElements = songCollectionElements;
    }

    private List<SongCollectionElement> withoutNullSongCollection(List<SongCollectionElement> songCollectionElements) {
        List<SongCollectionElement> collectionElements = new ArrayList<>();
        for (SongCollectionElement songCollectionElement : songCollectionElements) {
            if (songCollectionElement.getSongCollection() != null) {
                collectionElements.add(songCollectionElement);
            }
        }
        return collectionElements;
    }

    private boolean checkNull(List<SongCollectionElement> songCollectionElements) {
        for (SongCollectionElement songCollectionElement : songCollectionElements) {
            if (songCollectionElement.getSongCollection() == null) {
                return true;
            }
        }
        return false;
    }

    public Language getLanguage() {
        return language;
    }

    public void setLanguage(Language language) {
        this.language = language;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    public Date getServerModifiedDate() {
        return serverModifiedDate;
    }

    public void setServerModifiedDate(Date serverModifiedDate) {
        this.serverModifiedDate = serverModifiedDate;
    }

    public void stripTitle() {
        strippedTitle = stripAccents(title.toLowerCase());
    }

    public String getVersionGroup() {
        return versionGroup;
    }

    public void setVersionGroup(String versionGroup) {
        this.versionGroup = versionGroup;
    }

    public long getViews() {
        return views;
    }

    public void setViews(long views) {
        this.views = views;
    }

    public long getFavouriteCount() {
        return favouriteCount;
    }

    public void setFavouriteCount(long favouriteCount) {
        this.favouriteCount = favouriteCount;
    }

    public Long getScore() {
        if (savedScore != null) {
            return savedScore;
        }
        long score = 0;
        score += getViews();
        score += getFavouriteCount() * 2;
        if (createdDate == null || modifiedDate == null) {
            return score;
        }
        long l = Song.getCurrentDate() - createdDate.getTime();
        if (l < 2592000000L) {
            score += (long) (14 * ((1 - (double) l / 2592000000L)));
        }
        l = Song.getCurrentDate() - modifiedDate.getTime();
        if (l < 2592000000L) {
            score += (long) (4 * ((1 - (double) l / 2592000000L)));
        }
        if (isFavourite()) {
            score = (int) Math.max(score + 10, score * 1.1);
        }
        savedScore = score;
        return score;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = trimLongString100(author);
    }

    public void addToSongCollectionElements(SongCollectionElement songCollectionElement) {
        List<SongCollectionElement> songCollectionElements = getSongCollectionElements();
        if (!containsSongCollectionElement(songCollectionElements, songCollectionElement)) {
            songCollectionElements.add(songCollectionElement);
        }
    }

    private boolean containsSongCollectionElement(List<SongCollectionElement> songCollectionElements, SongCollectionElement songCollectionElement) {
        for (SongCollectionElement collectionElement : songCollectionElements) {
            if (collectionElement.getId().equals(songCollectionElement.getId())) {
                return true;
            }
        }
        return false;
    }

    public boolean isFavourite() {
        return favourite != null && favourite.isFavourite();
    }

    public FavouriteSong getFavourite() {
        return favourite;
    }

    public void setFavourite(boolean favourite) {
        if (this.favourite == null) {
            this.favourite = new FavouriteSong();
            this.favourite.setSong(this);
        }
        this.favourite.setFavourite(favourite);
    }

    public void setFavourite(FavouriteSong favouriteSong) {
        this.favourite = favouriteSong;
        if (favouriteSong != null) {
            favouriteSong.setSong(this);
        }
    }

    @Override
    public String toString() {
        StringBuilder text = new StringBuilder(this.title);
        for (SongCollectionElement songCollectionElement : getSongCollectionElements()) {
            SongCollection songCollection = songCollectionElement.getSongCollection();
            text.append(" ").append(songCollection.getName());
            text.append(" ").append(songCollectionElement.getOrdinalNumber());
        }
        return text.toString();
    }

    public boolean hasSongCollection() {
        List<SongCollectionElement> songCollectionElements = getSongCollectionElements();
        return songCollectionElements != null && !songCollectionElements.isEmpty();
    }

    public boolean isDownloadedSeparately() {
        return downloadedSeparately != null && downloadedSeparately;
    }

    public void setDownloadedSeparately(boolean downloadedSeparately) {
        this.downloadedSeparately = downloadedSeparately;
    }

    public boolean equivalent(Song other) {
        boolean equivalent = super.equivalent(other);
        if (!equivalent && other != null) {
            String uuid = getUuid();
            if (uuid != null && uuid.equals(other.getUuid())) {
                return true;
            }
        }
        return equivalent;
    }

    public void clearSongCollectionLists() {
        if (songCollections != null) {
            songCollections.clear();
        }
        if (songCollectionElements != null) {
            songCollectionElements.clear();
        }
    }
}
