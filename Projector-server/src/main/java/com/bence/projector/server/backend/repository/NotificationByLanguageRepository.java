package com.bence.projector.server.backend.repository;

import com.bence.projector.server.backend.model.NotificationByLanguage;
import com.bence.projector.server.backend.model.UserProperties;
import org.springframework.data.repository.CrudRepository;

import javax.transaction.Transactional;

public interface NotificationByLanguageRepository extends CrudRepository<NotificationByLanguage, Long> {
    @Transactional
    void deleteAllByUserProperties(UserProperties userProperties);
}
