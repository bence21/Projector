package com.bence.projector.server.backend.model;

import com.bence.projector.server.utils.AppProperties;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Index;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.bence.projector.server.utils.MemoryUtil.getEmptyList;

@Entity
@Table(
        indexes = {@Index(name = "uuid_index", columnList = "uuid", unique = true)}
)
public class Song extends AbstractModel {

    private String originalId;
    private String title;
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "song")
    private List<SongVerse> verses;
    private Date createdDate;
    private Date modifiedDate;
    private boolean deleted = false;
    @ManyToOne(fetch = FetchType.LAZY)
    private Language language;
    private Boolean uploaded;
    private long views;
    private Date lastIncrementViewDate;
    private long favourites;
    private Date lastIncrementFavouritesDate;
    private String createdByEmail;
    @Transient
    transient private double percentage;
    @ManyToOne(fetch = FetchType.LAZY)
    private Song versionGroup;
    private String youtubeUrl;
    private String verseOrder;
    private String author;
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "song")
    private List<SongVerseOrderListItem> verseOrderList;
    @ManyToOne(fetch = FetchType.LAZY)
    private User lastModifiedBy;
    @ManyToOne(fetch = FetchType.LAZY)
    private Song backUp;
    private Boolean isBackUp;
    private Boolean reviewerErased;
    @SuppressWarnings({"FieldCanBeLocal", "unused"})
    @Transient
    private String beforeId;
    @ManyToMany(fetch = FetchType.LAZY, mappedBy = "newSongStack")
    private List<NotificationByLanguage> notificationByLanguages;
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "song")
    private List<Suggestion> suggestions;
    @ManyToMany(fetch = FetchType.LAZY, mappedBy = "song")
    private List<SongListElement> songListElements;
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "song")
    private List<FavouriteSong> favouriteSongs;

    public Song() {
    }

    public Song(Song song) {
        originalId = song.originalId;
        title = song.title;
        setVerses(createCopy(song.verses));
        createdDate = song.createdDate;
        modifiedDate = song.modifiedDate;
        language = song.language;
        uploaded = song.uploaded;
        views = song.views;
        lastIncrementViewDate = song.lastIncrementViewDate;
        favourites = song.favourites;
        lastIncrementFavouritesDate = song.lastIncrementFavouritesDate;
        createdByEmail = song.createdByEmail;
        percentage = song.percentage;
        versionGroup = song.versionGroup;
        youtubeUrl = song.youtubeUrl;
        verseOrder = song.verseOrder;
        author = song.author;
        setSongVerseOrderListItems(createCopyOfVerseOrderList(song.verseOrderList));
        lastModifiedBy = song.lastModifiedBy;
        backUp = song.backUp;
    }

    private List<SongVerseOrderListItem> createCopyOfVerseOrderList(List<SongVerseOrderListItem> verseOrderList) {
        ArrayList<SongVerseOrderListItem> songVerseOrderListItems = new ArrayList<>();
        for (SongVerseOrderListItem item : verseOrderList) {
            songVerseOrderListItems.add(new SongVerseOrderListItem(item));
        }
        return songVerseOrderListItems;
    }

    private List<SongVerse> createCopy(List<SongVerse> verses) {
        ArrayList<SongVerse> songVerses = new ArrayList<>();
        for (SongVerse songVerse : verses) {
            songVerses.add(new SongVerse(songVerse));
        }
        return songVerses;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        int MAX_TITLE_LENGTH = 255;
        this.title = title.substring(0, Math.min(title.length(), MAX_TITLE_LENGTH));
    }

    public List<SongVerse> getVerses() {
        return verses;
    }

    public void setVerses(List<SongVerse> verses) {
        for (SongVerse songVerse : verses) {
            songVerse.setSong(this);
        }
        this.verses = verses;
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

    public boolean isDeleted() {
        return deleted || isBackUp() || isReviewerErased();
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

    @Override
    public String toString() {
        return title;
    }

    public Boolean getUploaded() {
        return uploaded;
    }

    public void incrementViews() {
        ++this.views;
    }

    public long getViews() {
        return views;
    }

    public void setLastIncrementViewDate(Date lastIncrementViewDate) {
        this.lastIncrementViewDate = lastIncrementViewDate;
    }

    public String getCreatedByEmail() {
        return createdByEmail;
    }

    public void setCreatedByEmail(String createdByEmail) {
        this.createdByEmail = createdByEmail;
    }

    public String getOriginalId() {
        return originalId;
    }

    public void setOriginalId(String originalId) {
        this.originalId = originalId;
    }

    public double getPercentage() {
        return percentage;
    }

    public void setPercentage(double percentage) {
        this.percentage = percentage;
    }

    public Song getVersionGroup() {
        return versionGroup;
    }

    public void setVersionGroup(Song versionGroup) {
        this.versionGroup = versionGroup;
    }

    public boolean isUploaded() {
        return uploaded != null && uploaded;
    }

    public void setUploaded(Boolean uploaded) {
        this.uploaded = uploaded;
    }

    public String getYoutubeUrl() {
        return youtubeUrl;
    }

    public void setYoutubeUrl(String youtubeUrl) {
        this.youtubeUrl = youtubeUrl;
    }

    public void setLastIncrementFavouritesDate(Date lastIncrementFavouritesDate) {
        this.lastIncrementFavouritesDate = lastIncrementFavouritesDate;
    }

    public void incrementFavourites() {
        ++favourites;
    }

    public long getFavourites() {
        return favourites;
    }

    public String getVerseOrder() {
        return verseOrder;
    }

    public void setVerseOrder(String verseOrder) {
        this.verseOrder = verseOrder;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public List<Short> getVerseOrderList() {
        if (verses != null && verseOrderWasSaved()) {
            for (short index = 0; index < verses.size(); ++index) {
                if (!containsIndex(index)) {
                    addToVerseOrderList(index, verseOrderList);
                }
            }
        } else if (verseOrderWasNotSaved()) {
            return null;
        }
        return getShortOrderList();
    }

    public void setVerseOrderList(List<Short> verseOrderList) {
        this.verseOrderList = getVerseOrderFromShorts(verseOrderList);
    }

    private List<SongVerseOrderListItem> getVerseOrderFromShorts(List<Short> verseOrderList) {
        if (verseOrderList == null) {
            return null;
        }
        ArrayList<SongVerseOrderListItem> songVerseOrderListItems = new ArrayList<>();
        for (Short aShort : verseOrderList) {
            addToVerseOrderList(aShort, songVerseOrderListItems);
        }
        return songVerseOrderListItems;
    }

    private List<Short> getShortOrderList() {
        if (verseOrderList == null) {
            return null;
        }
        ArrayList<Short> shorts = new ArrayList<>();
        for (SongVerseOrderListItem songVerseOrderListItem : verseOrderList) {
            shorts.add(songVerseOrderListItem.getPosition());
        }
        return shorts;
    }

    private boolean containsIndex(short index) {
        if (verseOrderList == null) {
            return false;
        }
        for (SongVerseOrderListItem songVerseOrderListItem : verseOrderList) {
            if (songVerseOrderListItem.getPosition() == index) {
                return true;
            }
        }
        return false;
    }

    private void addToVerseOrderList(short index, List<SongVerseOrderListItem> verseOrderList) {
        SongVerseOrderListItem songVerseOrderListItem = new SongVerseOrderListItem();
        songVerseOrderListItem.setPosition(index);
        songVerseOrderListItem.setSong(this);
        verseOrderList.add(songVerseOrderListItem);
    }

    public User getLastModifiedBy() {
        return lastModifiedBy;
    }

    public void setLastModifiedBy(User lastModifiedBy) {
        this.lastModifiedBy = lastModifiedBy;
    }

    public Song getBackUp() {
        return backUp;
    }

    public void setIsBackUp(Boolean isBackUp) {
        this.isBackUp = isBackUp;
    }

    public boolean isBackUp() {
        return isBackUp != null && isBackUp;
    }

    public void setBackUp(Song backUp) {
        this.backUp = backUp;
    }

    public boolean isReviewerErased() {
        return reviewerErased != null && reviewerErased;
    }

    public void setReviewerErased(Boolean reviewerErased) {
        this.reviewerErased = reviewerErased;
    }

    @SuppressWarnings("unused") // it's used by queue.html
    public String getBeforeId() {
        return beforeId;
    }

    public void setBeforeId(String beforeId) {
        this.beforeId = beforeId;
    }

    public boolean isPublic() {
        return !isReviewerErased() && !isDeleted() && !isBackUp();
    }

    private String idOrVersionGroup() {
        Song versionGroup = getVersionGroup();
        if (versionGroup != null) {
            return versionGroup.getUuid();
        }
        return getUuid();
    }

    public boolean isSameVersionGroup(Song other) {
        if (other == null) {
            return false;
        }
        return idOrVersionGroup().equals(other.idOrVersionGroup());
    }

    public String getVersionGroupUuid() {
        Song versionGroup = getVersionGroup();
        if (versionGroup == null) {
            return null;
        }
        return versionGroup.getUuid();
    }

    public List<Suggestion> getSuggestions() {
        return suggestions;
    }

    public List<SongVerseOrderListItem> getSongVerseOrderListItems() {
        return verseOrderList;
    }

    public void setSongVerseOrderListItems(List<SongVerseOrderListItem> songVerseOrderListItems) {
        if (songVerseOrderListItems != null) {
            for (SongVerseOrderListItem songVerseOrderListItem : songVerseOrderListItems) {
                songVerseOrderListItem.setSong(this);
            }
        }
        this.verseOrderList = songVerseOrderListItems;
    }

    public boolean verseOrderWasNotSaved() {
        List<SongVerseOrderListItem> songVerseOrderListItems = getSongVerseOrderListItems();
        if (songVerseOrderListItems == null) {
            return true;
        }
        return songVerseOrderListItems.isEmpty();
    }

    private boolean verseOrderWasSaved() {
        return !verseOrderWasNotSaved();
    }

    public List<SongListElement> getSongListElements() {
        if (songListElements == null) {
            songListElements = new ArrayList<>();
        }
        return songListElements;
    }

    @SuppressWarnings("unused")
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

    public String getText() {
        StringBuilder s = new StringBuilder(getTitle() + "\n\n");
        for (SongVerse verse : getVerses()) {
            s.append(verse.getText()).append("\n\n");
        }
        return s.toString();
    }

    public List<FavouriteSong> getFavouriteSongs() {
        if (favouriteSongs == null) {
            return favouriteSongs = getEmptyList();
        }
        return favouriteSongs;
    }

    public String getSongLinkWithTitle() {
        return getSongLink() + " " + getTitle();
    }

    public String getSongLink() {
        return AppProperties.getInstance().baseUrl() + "/#/song/" + getUuid();
    }
}
