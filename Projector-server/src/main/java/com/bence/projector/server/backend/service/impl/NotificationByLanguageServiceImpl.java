package com.bence.projector.server.backend.service.impl;

import com.bence.projector.server.backend.model.NotificationByLanguage;
import com.bence.projector.server.backend.model.UserProperties;
import com.bence.projector.server.backend.repository.NotificationByLanguageRepository;
import com.bence.projector.server.backend.service.NotificationByLanguageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class NotificationByLanguageServiceImpl extends BaseServiceImpl<NotificationByLanguage> implements NotificationByLanguageService {

    @Autowired
    private NotificationByLanguageRepository notificationByLanguageRepository;

    @Override
    public void deleteAllByUserProperties(UserProperties userProperties) {
        notificationByLanguageRepository.deleteAllByUserProperties(userProperties);
    }
}
