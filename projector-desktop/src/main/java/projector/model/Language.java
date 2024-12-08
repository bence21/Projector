package projector.model;

import com.j256.ormlite.dao.ForeignCollection;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import projector.service.ServiceManager;
import projector.service.SongService;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Language extends BaseEntity {

    @DatabaseField
    private String englishName;
    @DatabaseField
    private String nativeName;
    @DatabaseField
    private boolean selected;
    @DatabaseField
    private Date favouriteSongDate;
    @DatabaseField
    private Boolean sectionTypeDownloadedCorrectly;
    @ForeignCollectionField
    private ForeignCollection<Song> songForeignCollection;
    private List<Song> songs;
    private Long songsCount = null;

    public String getEnglishName() {
        return englishName;
    }

    public void setEnglishName(String englishName) {
        this.englishName = englishName;
    }

    public String getNativeName() {
        return nativeName;
    }

    public void setNativeName(String nativeName) {
        this.nativeName = nativeName;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public List<Song> getSongs() {
        if (songs == null) {
            if (songForeignCollection == null) {
                return new ArrayList<>();
            }
            List<Song> songs = new ArrayList<>(songForeignCollection.size());
            SongService songService = ServiceManager.getSongService();
            for (Song song : songForeignCollection) {
                songs.add(songService.getFromMemoryOrSong(song));
            }
            this.songs = songs;
            return songs;
        }
        return songs;
    }

    public void setSongs(List<Song> songs) {
        if (songs != null) {
            for (Song song : songs) {
                song.setLanguage(this);
            }
        }
        this.songs = songs;
    }

    public long getCountedSongsSize() {
        if (this.songsCount != null) {
            return this.songsCount;
        }
        return 0;
    }

    public long getSongsSize(SongService songService) {
        if (songs == null) {
            if (this.songsCount != null) {
                return this.songsCount;
            }
            if (songService == null) {
                return 0;
            }
            return songService.countByLanguage(this);
        }
        return songs.size();
    }

    @Override
    public String toString() {
        if (nativeName.equals(englishName)) {
            return nativeName;
        }
        return nativeName + " - " + englishName;
    }

    public Date getFavouriteSongLastServerModifiedDate() {
        if (favouriteSongDate == null) {
            return new Date(0);
        }
        return favouriteSongDate;
    }

    public void setFavouriteSongLastServerModifiedDate(Date favouriteSongLastServerModifiedDate) {
        this.favouriteSongDate = favouriteSongLastServerModifiedDate;
    }

    public boolean equivalent(Language other) {
        boolean equivalent = super.equivalent(other);
        if (!equivalent && other != null) {
            String uuid = getUuid();
            if (uuid != null && uuid.equals(other.getUuid())) {
                return true;
            }
        }
        return equivalent;
    }

    public void setSongsSize(long songsCount) {
        this.songsCount = songsCount;
    }

    public boolean isSectionTypeDownloadedCorrectly() {
        return sectionTypeDownloadedCorrectly != null && sectionTypeDownloadedCorrectly;
    }

    public void setSectionTypeDownloadedCorrectly(Boolean sectionTypeDownloadedCorrectly) {
        this.sectionTypeDownloadedCorrectly = sectionTypeDownloadedCorrectly;
    }
}
