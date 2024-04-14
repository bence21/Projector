package com.bence.projector.server.backend.model;

import com.bence.projector.server.backend.repository.SongRepository;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Index;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.util.Date;

@Entity
@Table(
        indexes = {@Index(name = "uuid_index", columnList = "uuid", unique = true)}
)
public class SongLink extends AbstractModel {

    @ManyToOne(fetch = FetchType.LAZY)
    private Song song1;
    @ManyToOne(fetch = FetchType.LAZY)
    private Song song2;
    private Date createdDate;
    private Date modifiedDate;
    private Boolean applied;
    private String createdByEmail;

    public void setSong1(Song song1) {
        this.song1 = song1;
    }

    public void setSong2(Song song2) {
        this.song2 = song2;
    }

    public Date getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
    }

    public Boolean getApplied() {
        return applied;
    }

    public void setApplied(Boolean applied) {
        this.applied = applied;
    }

    public String getCreatedByEmail() {
        return createdByEmail;
    }

    public void setCreatedByEmail(String createdByEmail) {
        this.createdByEmail = createdByEmail;
    }

    public Date getModifiedDate() {
        if (modifiedDate == null) {
            return getCreatedDate();
        }
        return modifiedDate;
    }

    public void setModifiedDate(Date modifiedDate) {
        this.modifiedDate = modifiedDate;
    }

    public boolean isUnApplied() {
        Boolean applied = getApplied();
        if (applied == null) {
            return true;
        }
        return !applied;
    }

    public Song getSong1(SongRepository songRepository) {
        if (song1 == null) {
            return null;
        }
        return songRepository.findOneByUuid(getSong1Uuid());
    }

    public String getSong1Uuid() {
        if (song1 == null) {
            return null;
        }
        return song1.getUuid();
    }

    public Song getSong2(SongRepository songRepository) {
        if (song2 == null) {
            return null;
        }
        return songRepository.findOneByUuid(getSong2Uuid());
    }

    public String getSong2Uuid() {
        if (song2 == null) {
            return null;
        }
        return song2.getUuid();
    }

    public boolean hasLanguage(Language language, SongRepository songRepository) {
        if (language == null) {
            return false;
        }
        Song song1 = getSong1(songRepository);
        try {
            if (song1 != null && language.equalsById(song1.getLanguage())) {
                return true;
            }
        } catch (NullPointerException e) {
            e.printStackTrace();
            return false;
        }
        Song song2 = getSong2(songRepository);
        return song2 != null && language.equalsById(song2.getLanguage());
    }

    public boolean alreadyTheSameVersionGroup(SongRepository songRepository) {
        Song song1 = getSong1(songRepository);
        if (song1 == null) {
            return false;
        }
        Song song2 = getSong2(songRepository);
        return song1.isSameVersionGroup(song2);
    }
}
