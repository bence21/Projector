package com.bence.projector.server.backend.model;

import javax.persistence.Entity;
import java.util.Date;

@Entity
public class NotificationStatus extends BaseEntity {

    private NotificationType notificationType;
    private Date date;

    @SuppressWarnings("unused")
    public NotificationType getNotificationType() {
        return notificationType;
    }

    public void setNotificationType(NotificationType notificationType) {
        this.notificationType = notificationType;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }
}
