package ru.draftplace.santanizer.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import ru.draftplace.santanizer.NotificationSender;
import ru.draftplace.santanizer.access.AccessActivator;
import ru.draftplace.santanizer.access.AccessDeactivator;

@Configuration
@EnableScheduling
public class ScheduleConfig
{
    private final AccessActivator accessActivator;

    private final AccessDeactivator accessDeactivator;

    private final NotificationSender notificationSender;

    private static final short MAIL_LIMIT = 5;

    @Autowired
    public ScheduleConfig(
            AccessActivator accessActivator,
            AccessDeactivator accessDeactivator,
            NotificationSender notificationSender
    )
    {
        this.accessActivator = accessActivator;
        this.accessDeactivator = accessDeactivator;
        this.notificationSender = notificationSender;
    }

    @Scheduled(fixedDelay = 60000, initialDelay = 60000)
    public void activateAccess()
    {
        accessActivator.activate();
    }

    @Scheduled(fixedDelay = 60000, initialDelay = 60000)
    public void deactivateAccessByTimeout()
    {
        accessDeactivator.deactivateByTimeout(24 * 60 * 60);
    }

    @Scheduled(fixedDelay = 30000, initialDelay = 60000)
    public void queuedMail()
    {
        int inQueue = 1;

        for (short i = 0; inQueue > 0 && i < MAIL_LIMIT; i++) {
            inQueue = notificationSender.sendNextQueuedMail();
        }

    }
}
