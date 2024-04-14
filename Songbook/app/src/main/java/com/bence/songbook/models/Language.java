package com.bence.songbook.models;

import com.bence.songbook.repository.SongRepository;
import com.j256.ormlite.dao.ForeignCollection;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;

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
    private Boolean selectedForDownload;
    @DatabaseField
    private Date favouriteSongDate;
    @ForeignCollectionField
    private ForeignCollection<Song> songForeignCollection;
    private List<Song> songs;
    private Long size;
    private Long sumAccessedTimes = null;

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
            try {
                songs.addAll(songForeignCollection);
            } catch (Exception e) {
                e.printStackTrace();
            }
            this.songs = songs;
            return songs;
        }
        return songs;
    }

    public void setSongs(List<Song> songs) {
        for (Song song : songs) {
            song.setLanguage(this);
        }
        this.songs = songs;
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

    public boolean isSelectedForDownload() {
        return selectedForDownload == null || selectedForDownload;
    }

    public void setSelectedForDownload(boolean selectedForDownload) {
        this.selectedForDownload = selectedForDownload;
    }

    public void setSize(Long size) {
        this.size = size;
    }

    public Long getSize() {
        return size;
    }

    public long countAccessedTimesFromSongs(SongRepository songRepository) {
        if (sumAccessedTimes == null) {
            if (!isSelectedForDownload()) {
                sumAccessedTimes = 0L;
            } else {
                sumAccessedTimes = songRepository.sumAccessedTimesByLanguage(this);
            }
        }
        return sumAccessedTimes;
    }

    public long getSizeL() {
        Long size = getSize();
        if (size == null) {
            return 0L;
        }
        return size;
    }

    public long getSongsCount(SongRepository songRepository) {
        return songRepository.countByLanguage(this);
    }
}
