package com.bence.projector.server.backend.model;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Index;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
        indexes = {@Index(name = "uuid_index", columnList = "uuid", unique = true)}
)
public class Language extends AbstractModel {
    private String englishName;
    private String nativeName;
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "language")
    private List<Song> songs;
    private long songsCount;
    private double percentage;
    @ManyToMany(mappedBy = "reviewLanguages")
    private List<User> reviewers;
    private Boolean deleted;

    public Language() {
    }

    public void setLanguageForSongs() {
        for (Song song : getSongs()) {
            song.setLanguage(this);
        }
    }

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

    public List<Song> getSongs() {
        if (songs == null) {
            songs = new ArrayList<>();
        }
        return songs;
    }

    public void setSongs(List<Song> songs) {
        this.songs = songs;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((this.getUuid() == null) ? 0 : this.getUuid().hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        Language other = (Language) obj;
        if (this.getUuid() == null) {
            return other.getUuid() == null;
        } else {
            return this.getUuid().equals(other.getUuid());
        }
    }

    public long getSongsCount() {
        return songsCount;
    }

    public void setSongsCount(long songsCount) {
        this.songsCount = songsCount;
    }

    public double getPercentage() {
        return percentage;
    }

    public void setPercentage(double percentage) {
        this.percentage = percentage;
    }

    public List<User> getReviewers() {
        return reviewers;
    }

    public void setReviewers(List<User> reviewers) {
        this.reviewers = reviewers;
    }

    public boolean equalsById(Language language) {
        if (language == null) {
            return false;
        }
        return getId().equals(language.getId());
    }

    public Boolean getDeleted() {
        return deleted;
    }

    public boolean isDeleted() {
        return deleted != null && deleted;
    }

    public void setDeleted(Boolean deleted) {
        this.deleted = deleted;
    }

    public boolean isCzech() {
        return getUuid().equals("5d7bbbc70ca23e000465e286");
    }

    public boolean isSlovak() {
        return getUuid().equals("f5f2fe72-6b74-414b-a9a8-5f26451eb1a1");
    }

    public boolean isFilipino() {
        return getUuid().equals("5d7007d029e75400049df908");
    }

    public boolean isCebuano() {
        return getUuid().equals("2de44fc1-8112-4700-bf85-2432e85f35f4");
    }

    @Override
    public String toString() {
        return englishName;
    }
}
