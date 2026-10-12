package org.kidscircle.coach.service;

import org.kidscircle.coach.model.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

    @Autowired
    private JavaMailSender mailSender;

    @Value("${app.mail.from}")
    private String fromAddress;

    @Value("${app.base-url}")
    private String baseUrl;

    public void sendPasswordResetEmail(String toEmail, String resetLink) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromAddress);
        message.setTo(toEmail);
        message.setSubject("Reset your Coach password");
        message.setText(
                "We received a request to reset your Coach password.\n\n" +
                "Reset your password: " + resetLink + "\n\n" +
                "This link expires in 1 hour. If you didn't request this, you can ignore this email."
        );
        mailSender.send(message);
        logger.info("Password reset email sent to {}", toEmail);
    }

    public void sendDailyReminderEmail(String toEmail, List<Task> dueToday, List<Task> overdue) {
        StringBuilder body = new StringBuilder("Here's what's on your plate today:\n\n");

        if (!overdue.isEmpty()) {
            body.append("Overdue:\n");
            for (Task task : overdue) {
                body.append(" - ").append(task.getTitle())
                        .append(" (was due ").append(task.getDueDate()).append(")\n");
            }
            body.append("\n");
        }

        if (!dueToday.isEmpty()) {
            body.append("Due today:\n");
            for (Task task : dueToday) {
                body.append(" - ").append(task.getTitle()).append("\n");
            }
            body.append("\n");
        }

        body.append("Open Coach to check them off: ").append(baseUrl).append("/today");

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromAddress);
        message.setTo(toEmail);
        message.setSubject("Your Coach reminders for today");
        message.setText(body.toString());
        mailSender.send(message);
        logger.info("Daily reminder email sent to {}", toEmail);
    }
}
