package com.bence.projector.server.backend.service;

import com.bence.projector.server.backend.model.NotificationByLanguage;
import com.bence.projector.server.backend.model.UserProperties;

public interface NotificationByLanguageService extends BaseService<NotificationByLanguage> {
    void deleteAllByUserProperties(UserProperties userProperties);
}
