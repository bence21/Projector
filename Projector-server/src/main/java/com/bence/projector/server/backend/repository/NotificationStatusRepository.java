package com.bence.projector.server.backend.repository;

import com.bence.projector.server.backend.model.NotificationStatus;
import com.bence.projector.server.backend.model.NotificationType;
import org.springframework.data.repository.CrudRepository;

import java.util.Date;
import java.util.List;

public interface NotificationStatusRepository extends CrudRepository<NotificationStatus, Long> {

    List<NotificationStatus> findAllByNotificationTypeAndDateAfter(NotificationType notificationType, Date date);
}
