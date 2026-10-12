package org.kidscircle.coach.service;

import org.kidscircle.coach.db.TaskService;
import org.kidscircle.coach.db.UserService;
import org.kidscircle.coach.model.Task;
import org.kidscircle.coach.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ReminderService {

    private static final Logger logger = LoggerFactory.getLogger(ReminderService.class);

    // Reminders go out once each user's local clock reaches this hour.
    static final int REMINDER_HOUR = 7;
    private static final ZoneId DEFAULT_ZONE = ZoneId.of("America/New_York");

    @Autowired private UserService userService;
    @Autowired private TaskService taskService;
    @Autowired private EmailService emailService;

    public void sendDueReminders() {
        for (User user : userService.findAllUsers()) {
            sendReminderIfDue(user, ZonedDateTime.now(zoneFor(user)));
        }
    }

    // Package-private and time-injectable so the trigger condition can be unit tested
    // without waiting on the real wall clock.
    void sendReminderIfDue(User user, ZonedDateTime localNow) {
        if (user.getEmail() == null || user.getEmail().trim().isEmpty()) return;
        if (localNow.getHour() != REMINDER_HOUR) return;

        LocalDate today = localNow.toLocalDate();
        if (today.equals(user.getLastReminderSentAt())) return;

        List<Task> dueToday = taskService.getTasksDueOnForUser(user.getUserId(), today);
        List<Task> overdue = taskService.getReadyTasksForUser(user.getUserId()).stream()
                .filter(t -> t.getDueDate() != null && t.getDueDate().isBefore(today))
                .collect(Collectors.toList());

        if (dueToday.isEmpty() && overdue.isEmpty()) {
            markSent(user, today);
            return;
        }

        try {
            emailService.sendDailyReminderEmail(user.getEmail(), dueToday, overdue);
        } catch (Exception e) {
            logger.error("Failed to send daily reminder to {}; will retry on next check", user.getEmail(), e);
            return;
        }

        markSent(user, today);
    }

    private void markSent(User user, LocalDate today) {
        user.setLastReminderSentAt(today);
        userService.saveUser(user);
    }

    private ZoneId zoneFor(User user) {
        try {
            return ZoneId.of(user.getTimeZone());
        } catch (Exception e) {
            return DEFAULT_ZONE;
        }
    }
}
