package com.bence.projector.server.backend.model;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import java.util.ArrayList;
import java.util.List;

@Entity
public class UserProperties extends BaseEntity {

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "userProperties")
    private List<NotificationByLanguage> notifications;

    public List<NotificationByLanguage> getNotifications() {
        if (notifications == null) {
            notifications = new ArrayList<>();
        }
        return notifications;
    }

    public final void setNotifications_(List<NotificationByLanguage> notifications) {
        if (notifications != null) {
            for (NotificationByLanguage notificationByLanguage : notifications) {
                notificationByLanguage.setUserProperties(this);
            }
            if (this.notifications != null) {
                this.notifications.clear();
                this.notifications.addAll(notifications);
            } else {
                this.notifications = notifications;
            }
        } else {
            this.notifications = null;
        }
    }

    public final List<NotificationByLanguage> getNotificationsAsIs() {
        return notifications;
    }

    public NotificationByLanguage getNotificationByLanguage(Language language) {
        if (language == null) {
            return null;
        }
        for (NotificationByLanguage notification : getNotifications()) {
            Language notificationLanguage = notification.getLanguage();
            if (notificationLanguage != null && notificationLanguage.getUuid().equals(language.getUuid())) {
                return notification;
            }
        }
        return null;
    }
}
