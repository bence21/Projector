package com.bence.projector.common.dto;

import java.util.List;

public class UserPropertiesDTO {
    private List<NotificationByLanguageDTO> notificationsByLanguage;

    public List<NotificationByLanguageDTO> getNotificationsByLanguage() {
        return notificationsByLanguage;
    }

    public void setNotificationsByLanguage(List<NotificationByLanguageDTO> notificationsByLanguage) {
        this.notificationsByLanguage = notificationsByLanguage;
    }
}
