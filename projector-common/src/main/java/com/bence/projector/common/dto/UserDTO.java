package com.bence.projector.common.dto;

import java.util.Date;
import java.util.List;

public class UserDTO extends BaseDTO {
    private String email;
    private String preferredLanguage;
    private int role;
    private String surname;
    private String firstName;
    private boolean activated;
    private Date modifiedDate;
    private List<LanguageDTO> reviewLanguages;
    private boolean hadUploadedSongs;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPreferredLanguage() {
        return preferredLanguage;
    }

    public void setPreferredLanguage(String preferredLanguage) {
        this.preferredLanguage = preferredLanguage;
    }

    public int getRole() {
        return role;
    }

    public void setRole(int role) {
        this.role = role;
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public Date getModifiedDate() {
        return modifiedDate;
    }

    public void setModifiedDate(Date modifiedDate) {
        this.modifiedDate = modifiedDate;
    }

    public List<LanguageDTO> getReviewLanguages() {
        return reviewLanguages;
    }

    public void setReviewLanguages(List<LanguageDTO> reviewLanguages) {
        this.reviewLanguages = reviewLanguages;
    }

    public boolean isActivated() {
        return activated;
    }

    public void setActivated(boolean activated) {
        this.activated = activated;
    }

    public boolean isHadUploadedSongs() {
        return hadUploadedSongs;
    }

    public void setHadUploadedSongs(boolean hadUploadedSongs) {
        this.hadUploadedSongs = hadUploadedSongs;
    }
}
