package org.kidscircle.coach;

import org.kidscircle.coach.service.ReminderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class ReminderScheduler {

    @Autowired private ReminderService reminderService;

    // Every 15 minutes, so each user's local 7am hour is checked a few times
    // even if server load delays a given run.
    @Scheduled(cron = "0 0,15,30,45 * * * *")
    public void checkReminders() {
        reminderService.sendDueReminders();
    }
}
