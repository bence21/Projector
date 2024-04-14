package com.bence.projector.server.backend.model;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Index;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.bence.projector.server.utils.MemoryUtil.getEmptyList;

@Entity
@Table(
        indexes = {@Index(name = "uuid_index", columnList = "uuid", unique = true)}
)
public class User extends AbstractModel {

    private String password;
    private String email;
    private String phone;
    private Role role;
    private String token;
    private Date expiryDate;
    private String preferredLanguage;
    private Boolean activated;
    private String surname;
    private String firstName;
    private String activationCode;
    private Date modifiedDate;
    private Date createdDate;
    private Boolean hadUploadedSongs;
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "REVIEW_LANGUAGES")
    private List<Language> reviewLanguages;
    @ManyToOne(fetch = FetchType.LAZY)
    private UserProperties userProperties;
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "lastModifiedBy")
    private List<Suggestion> lastModifiedSuggestions;
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "lastModifiedBy")
    private List<Song> lastModifiedSongs;
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "user")
    private List<FavouriteSong> favouriteSongs;

    public User() {
        super();
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public Date getExpiryDate() {
        return expiryDate == null ? null : (Date) expiryDate.clone();
    }

    public void setExpiryDate(Date expiryDate) {
        this.expiryDate = expiryDate == null ? null : (Date) expiryDate.clone();
    }

    @Override
    public String toString() {
        return "User{" + "password='" + password + '\'' + ", email='" + email + '\'' + ", phone='" + phone + '\''
                + ", role=" + role + '}';
    }

    public String getPreferredLanguage() {
        if (preferredLanguage == null) {
            preferredLanguage = "en";
        }
        return preferredLanguage;
    }

    public void setPreferredLanguage(String preferredLanguage) {
        this.preferredLanguage = preferredLanguage;
    }

    public boolean isActivated() {
        return activated != null && activated;
    }

    public void setActivated(Boolean activated) {
        this.activated = activated;
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

    public String getActivationCode() {
        return activationCode;
    }

    public void setActivationCode(String activationCode) {
        this.activationCode = activationCode;
    }

    public Date getModifiedDate() {
        return modifiedDate;
    }

    public void setModifiedDate(Date modifiedDate) {
        this.modifiedDate = modifiedDate;
    }

    public Date getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
    }

    public List<Language> getReviewLanguages() {
        if (reviewLanguages == null) {
            reviewLanguages = new ArrayList<>();
        }
        return reviewLanguages;
    }

    public void setReviewLanguages(List<Language> reviewLanguages) {
        this.reviewLanguages = reviewLanguages;
    }

    private boolean hasReviewerRoleForLanguage(Language language) {
        if (role == Role.ROLE_ADMIN) {
            return true;
        }
        String id = language.getUuid();
        for (Language reviewLanguage : getReviewLanguages()) {
            if (reviewLanguage.getUuid().equals(id)) {
                return true;
            }
        }
        return false;
    }

    public NotificationByLanguage getNotificationByLanguage(Language language) {
        UserProperties userProperties = getUserProperties();
        NotificationByLanguage notificationByLanguage = userProperties.getNotificationByLanguage(language);
        if (notificationByLanguage == null) {
            notificationByLanguage = new NotificationByLanguage();
            notificationByLanguage.setLanguage(language);
            boolean hasReviewerRoleForLanguage = hasReviewerRoleForLanguage(language);
            notificationByLanguage.setSuggestions(hasReviewerRoleForLanguage);
            notificationByLanguage.setNewSongs(hasReviewerRoleForLanguage);
            if (hasReviewerRoleForLanguage) {
                userProperties.getNotifications().add(notificationByLanguage);
            }
        }
        return notificationByLanguage;
    }

    public UserProperties getUserProperties() {
        if (userProperties == null) {
            userProperties = new UserProperties();
            List<NotificationByLanguage> notifications = new ArrayList<>();
            for (Language language : getReviewLanguages()) {
                NotificationByLanguage notificationByLanguage = new NotificationByLanguage();
                notificationByLanguage.setLanguage(language);
                notificationByLanguage.setSuggestions(true);
                notificationByLanguage.setNewSongs(true);
                notifications.add(notificationByLanguage);
            }
            userProperties.setNotifications_(notifications);
        }
        return userProperties;
    }

    public void setUserProperties(UserProperties userProperties) {
        this.userProperties = userProperties;
    }

    public UserProperties getUserProperties(List<Language> languages) {
        if (userProperties == null) {
            userProperties = new UserProperties();
            List<NotificationByLanguage> notifications = new ArrayList<>();
            for (Language language : languages) {
                NotificationByLanguage notificationByLanguage = new NotificationByLanguage();
                notificationByLanguage.setLanguage(language);
                notificationByLanguage.setSuggestions(true);
                notificationByLanguage.setNewSongs(true);
                notifications.add(notificationByLanguage);
            }
            userProperties.setNotifications_(notifications);
        }
        return userProperties;
    }

    public boolean hasReviewLanguage(Language language) {
        for (Language reviewLanguage : reviewLanguages) {
            if (reviewLanguage.getId().equals(language.getId())) {
                return true;
            }
        }
        return false;
    }

    public List<FavouriteSong> getFavouriteSongs() {
        if (favouriteSongs == null) {
            return favouriteSongs = getEmptyList();
        }
        return favouriteSongs;
    }

    public boolean hasUserProperties() {
        return userProperties != null && userProperties.getId() != null;
    }

    public boolean isHadUploadedSongs() {
        return hadUploadedSongs != null && hadUploadedSongs;
    }

    public void setHadUploadedSongs(Boolean hadUploadedSongs) {
        this.hadUploadedSongs = hadUploadedSongs;
    }
}
