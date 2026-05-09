package com.bence.projector.server.mailsending;

import com.bence.projector.server.backend.model.NotificationStatus;
import com.bence.projector.server.backend.model.NotificationType;
import com.bence.projector.server.backend.model.User;
import com.bence.projector.server.backend.repository.NotificationStatusRepository;
import com.bence.projector.server.backend.service.UserService;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

@Component
public class NewUserEmailScheduler {

    private final UserService userService;
    private final MailSenderService mailSenderService;
    private final NotificationStatusRepository notificationStatusRepository;

    @Autowired
    public NewUserEmailScheduler(UserService userService, MailSenderService mailSenderService,
                                 NotificationStatusRepository notificationStatusRepository) {
        this.userService = userService;
        this.mailSenderService = mailSenderService;
        this.notificationStatusRepository = notificationStatusRepository;
    }

    @Scheduled(cron = "0 0 9 * * *")
    @SchedulerLock(name = "NewUserEmailScheduler_sendNewUserDigest", lockAtMostFor = "PT30M", lockAtLeastFor = "PT10S")
    public void sendNewUserDigest() {
        Date lastNotificationDate = getLastNotificationDateOrOneDayBefore();
        List<User> users = userService.findAllByCreatedDateAfter(lastNotificationDate);
        if (users != null && !users.isEmpty()) {
            mailSenderService.sendEmailNewUsers(users);
        }
    }

    private Date getLastNotificationDateOrOneDayBefore() {
        NotificationStatus notificationStatus = notificationStatusRepository.findFirstByNotificationTypeOrderByDateDesc(NotificationType.NEW_USER);
        if (notificationStatus != null && notificationStatus.getDate() != null) {
            return notificationStatus.getDate();
        }
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR, -1);
        return calendar.getTime();
    }
}
