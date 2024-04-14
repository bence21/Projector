package com.bence.songbook.models;

import static com.bence.songbook.ui.utils.StringUtils.stripAccents;

import androidx.annotation.NonNull;

import com.bence.projector.common.model.SectionType;
import com.j256.ormlite.dao.ForeignCollection;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Song extends BaseEntity {

    private static final long currentDate = new Date().getTime();
    @DatabaseField
    private String title;
    @DatabaseField
    private String strippedTitle;
    @ForeignCollectionField
    private ForeignCollection<SongVerse> songVerseForeignCollection;
    private List<SongVerse> verses;
    @DatabaseField
    private Date createdDate;
    @DatabaseField
    private Date modifiedDate;
    private boolean deleted = false;
    @DatabaseField(foreign = true, index = true)
    private Language language;
    @DatabaseField
    private Date lastAccessed;
    @DatabaseField
    private Long accessedTimes;
    @DatabaseField
    private Long accessedTimeAverage;
    private List<SongCollection> songCollections;
    private List<SongCollectionElement> songCollectionElements;
    private Date nullDate;
    private String createdByEmail;
    @DatabaseField
    private String versionGroup;
    @DatabaseField
    private String youtubeUrl;
    private FavouriteSong favourite;
    @DatabaseField
    private long views;
    @DatabaseField
    private String verseOrder;
    @DatabaseField
    private long favourites;
    private List<Short> verseOrderList;
    private boolean newSong;
    @DatabaseField
    private Boolean asDeleted;
    @DatabaseField
    private Boolean savedOnlyToDevice;
    private Integer savedScore;

    public Song() {
    }

    public static void copyLocallySet(Song song, Song modifiedSong) {
        song.setFavourite(modifiedSong.getFavourite());
        song.setAccessedTimeAverage(modifiedSong.getAccessedTimeAverage());
        song.setAccessedTimes(modifiedSong.getAccessedTimes());
        song.setLastAccessed(modifiedSong.getLastAccessed());
    }

    private static long getCurrentDate() {
        return currentDate;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
        strippedTitle = stripAccents(title.toLowerCase());
    }

    public Date getCreatedDate() {
        return createdDate == null ? null : (Date) createdDate.clone();
    }

    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate == null ? null : (Date) createdDate.clone();
    }

    public Date getModifiedDate() {
        return modifiedDate == null ? null : (Date) modifiedDate.clone();
    }

    public void setModifiedDate(Date modifiedDate) {
        this.modifiedDate = modifiedDate == null ? null : (Date) modifiedDate.clone();
    }

    public List<SongVerse> getVerses() {
        if (verses == null) {
            if (songVerseForeignCollection == null) {
                return new ArrayList<>();
            }
            List<SongVerse> songVerses = new ArrayList<>(songVerseForeignCollection.size());
            songVerses.addAll(songVerseForeignCollection);
            verses = songVerses;
            return songVerses;
        }
        return verses;
    }

    public void setVerses(List<SongVerse> verseList) {
        for (SongVerse songVerse : verseList) {
            songVerse.setSong(this);
        }
        this.verses = verseList;
    }

    public void fetchVerses() {
        if (verses == null) {
            if (songVerseForeignCollection == null) {
                return;
            }
            List<SongVerse> songVerses = new ArrayList<>(songVerseForeignCollection.size());
            songVerses.addAll(songVerseForeignCollection);
            verses = songVerses;
        }
    }

    public String getStrippedTitle() {
        return strippedTitle;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    public Language getLanguage() {
        return language;
    }

    public void setLanguage(Language language) {
        this.language = language;
    }

    private Date getNullDate() {
        if (nullDate == null) {
            nullDate = new Date(0);
        }
        return nullDate;
    }

    public Date getLastAccessed() {
        if (lastAccessed == null) {
            return getNullDate();
        }
        return lastAccessed;
    }

    public void setLastAccessed(Date lastAccessed) {
        this.lastAccessed = lastAccessed;
    }

    public Long getAccessedTimes() {
        if (accessedTimes == null) {
            accessedTimes = 0L;
        }
        return accessedTimes;
    }

    public void setAccessedTimes(Long accessedTimes) {
        this.accessedTimes = accessedTimes;
    }

    public Long getAccessedTimeAverage() {
        if (accessedTimeAverage == null) {
            accessedTimeAverage = 0L;
        }
        return accessedTimeAverage;
    }

    public void setAccessedTimeAverage(Long accessedTimeAverage) {
        this.accessedTimeAverage = accessedTimeAverage;
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

    public List<SongCollectionElement> getSongCollectionElements() {
        if (songCollectionElements == null) {
            songCollectionElements = new ArrayList<>();
        }
        return songCollectionElements;
    }

    public void setSongCollectionElements(List<SongCollectionElement> songCollectionElements) {
        this.songCollectionElements = songCollectionElements;
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

    public void addToSongCollectionElements(SongCollectionElement songCollectionElement) {
        if (songCollectionElement == null) {
            return;
        }
        List<SongCollectionElement> songCollectionElements = getSongCollectionElements();
        if (!containsSongCollectionElement(songCollectionElements, songCollectionElement)) {
            songCollectionElements.add(songCollectionElement);
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

    private boolean containsSongCollectionElement(List<SongCollectionElement> songCollectionElements, SongCollectionElement songCollectionElement) {
        for (SongCollectionElement collectionElement : songCollectionElements) {
            if (collectionElement.getId().equals(songCollectionElement.getId())) {
                return true;
            }
        }
        return false;
    }

    public String getCreatedByEmail() {
        return createdByEmail;
    }

    public void setCreatedByEmail(String createdByEmail) {
        this.createdByEmail = createdByEmail;
    }

    public String getVersionGroup() {
        return versionGroup;
    }

    public void setVersionGroup(String versionGroup) {
        this.versionGroup = versionGroup;
    }

    public String getYoutubeUrl() {
        return youtubeUrl;
    }

    public void setYoutubeUrl(String youtubeUrl) {
        this.youtubeUrl = youtubeUrl;
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

    @NonNull
    @Override
    public String toString() {
        if (title == null) {
            return "";
        }
        return title;
    }

    private long getViews() {
        return views;
    }

    public void setViews(long views) {
        this.views = views;
    }

    public Integer getScore() {
        if (savedScore != null) {
            return savedScore;
        }
        int score = 0;
        score += getAccessedTimes() * 3;
        score += getViews();
        score += getFavourites();
        if (getYoutubeUrl() != null) {
            score += 10;
        }
        FavouriteSong favourite = getFavourite();
        if (favourite != null && favourite.isFavourite()) {
            score += 10;
        }
        long l = Song.getCurrentDate() - createdDate.getTime();
        if (l < 2592000000L) {
            score += 14 * ((1 - (double) l / 2592000000L));
        }
        l = Song.getCurrentDate() - modifiedDate.getTime();
        if (l < 2592000000L) {
            score += 4 * ((1 - (double) l / 2592000000L));
        }
        savedScore = score;
        return score;
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
        verseOrder = s.toString();
    }

    private long getFavourites() {
        return favourites;
    }

    public void setFavourites(long favourites) {
        this.favourites = favourites;
    }

    public List<SongVerse> getSongVersesByVerseOrder() {
        List<Short> verseOrderList = getVerseOrderList();
        List<SongVerse> songVerses = new ArrayList<>(verseOrderList.size());
        List<SongVerse> verses = getVerses();
        for (Short index : verseOrderList) {
            if (0 <= index && index < verses.size()) {
                songVerses.add(verses.get(index));
            }
        }
        return songVerses;
    }

    public boolean isNotNewSong() {
        return !newSong;
    }

    public void setNewSong(boolean newSong) {
        this.newSong = newSong;
    }

    public boolean isAsDeleted() {
        return asDeleted != null && asDeleted;
    }

    public void setAsDeleted(boolean asDeleted) {
        this.asDeleted = asDeleted;
    }

    public void setSavedOnlyToDevice(Boolean savedOnlyToDevice) {
        this.savedOnlyToDevice = savedOnlyToDevice;
    }

    public boolean isSavedOnlyToDevice() {
        return savedOnlyToDevice != null && savedOnlyToDevice;
    }

}
