package ru.draftplace.santanizer.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import ru.draftplace.santanizer.access.AccessActivator;

@Configuration
@EnableScheduling
public class ScheduleConfig
{
    private final AccessActivator accessActivator;

    @Autowired
    public ScheduleConfig(AccessActivator accessActivator)
    {
        this.accessActivator = accessActivator;
    }

    @Scheduled(fixedDelay = 60000, initialDelay = 60000)
    public void activateAccess()
    {
        accessActivator.activate();
    }
}
