package org.kidscircle.coach.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.kidscircle.coach.db.SurveyRepository;
import org.kidscircle.coach.db.TaskService;
import org.kidscircle.coach.db.UserService;
import org.kidscircle.coach.model.Survey;
import org.kidscircle.coach.model.Task;
import org.kidscircle.coach.model.User;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class ReminderServiceTest {

    private UserService userService;
    private TaskService taskService;
    private EmailService emailService;
    private SurveyRepository surveyRepository;
    private OllamaService ollamaService;
    private ReminderService reminderService;

    private User user;
    private ZonedDateTime sevenAm;

    @BeforeEach
    void setUp() {
        userService = mock(UserService.class);
        taskService = mock(TaskService.class);
        emailService = mock(EmailService.class);
        surveyRepository = mock(SurveyRepository.class);
        ollamaService = mock(OllamaService.class);

        reminderService = new ReminderService();
        setField("userService", userService);
        setField("taskService", taskService);
        setField("emailService", emailService);
        setField("surveyRepository", surveyRepository);
        setField("ollamaService", ollamaService);

        user = new User();
        user.setUserId(1L);
        user.setEmail("deepika.appana@gmail.com");

        sevenAm = ZonedDateTime.of(LocalDate.of(2026, 10, 12), java.time.LocalTime.of(7, 5), ZoneId.of("America/New_York"));

        when(taskService.getTasksDueOnForUser(anyLong(), eq(sevenAm.toLocalDate()))).thenReturn(Collections.emptyList());
        when(taskService.getReadyTasksForUser(anyLong())).thenReturn(Collections.emptyList());
        when(ollamaService.generateReminderMessage(any(), any(), any())).thenReturn("You've got this today.");
    }

    private void setField(String name, Object value) {
        try {
            java.lang.reflect.Field field = ReminderService.class.getDeclaredField(name);
            field.setAccessible(true);
            field.set(reminderService, value);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void doesNothingOutsideReminderHour() {
        ZonedDateTime notSevenAm = sevenAm.withHour(13);

        reminderService.sendReminderIfDue(user, notSevenAm);

        verifyNoInteractions(emailService);
        verify(userService, never()).saveUser(any());
    }

    @Test
    void doesNothingIfAlreadySentToday() {
        user.setLastReminderSentAt(sevenAm.toLocalDate());

        reminderService.sendReminderIfDue(user, sevenAm);

        verifyNoInteractions(emailService);
        verify(userService, never()).saveUser(any());
    }

    @Test
    void marksSentWithoutEmailingWhenNothingIsDue() {
        reminderService.sendReminderIfDue(user, sevenAm);

        verifyNoInteractions(emailService);
        verifyNoInteractions(ollamaService);
        verify(userService).saveUser(user);
        assertEquals(sevenAm.toLocalDate(), user.getLastReminderSentAt());
    }

    @Test
    void sendsDigestWithAiIntroAndMarksSentWhenTasksAreDue() {
        Task dueToday = new Task();
        dueToday.setTitle("Write report");
        when(taskService.getTasksDueOnForUser(eq(1L), eq(sevenAm.toLocalDate())))
                .thenReturn(List.of(dueToday));

        Task overdue = new Task();
        overdue.setTitle("Call dentist");
        overdue.setDueDate(sevenAm.toLocalDate().minusDays(2));
        when(taskService.getReadyTasksForUser(1L)).thenReturn(List.of(overdue, dueToday));

        Survey survey = new Survey();
        survey.setDrive("To support my family");
        when(surveyRepository.findSurveyByUserId(1L)).thenReturn(survey);
        when(ollamaService.generateReminderMessage(eq(survey), eq(List.of(dueToday)), eq(List.of(overdue))))
                .thenReturn("Remember why you started.");

        reminderService.sendReminderIfDue(user, sevenAm);

        verify(emailService).sendDailyReminderEmail(
                eq("deepika.appana@gmail.com"),
                eq("Remember why you started."),
                eq(List.of(dueToday)),
                eq(List.of(overdue)));
        verify(userService).saveUser(user);
        assertEquals(sevenAm.toLocalDate(), user.getLastReminderSentAt());
    }

    @Test
    void stillSendsDigestWhenAiIntroGenerationFails() {
        Task dueToday = new Task();
        dueToday.setTitle("Write report");
        when(taskService.getTasksDueOnForUser(eq(1L), eq(sevenAm.toLocalDate())))
                .thenReturn(List.of(dueToday));
        when(ollamaService.generateReminderMessage(any(), any(), any())).thenReturn(null);

        reminderService.sendReminderIfDue(user, sevenAm);

        verify(emailService).sendDailyReminderEmail(eq("deepika.appana@gmail.com"), eq((String) null), any(), any());
        verify(userService).saveUser(user);
    }

    @Test
    void doesNotMarkSentWhenEmailSendingFails() {
        Task dueToday = new Task();
        dueToday.setTitle("Write report");
        when(taskService.getTasksDueOnForUser(eq(1L), eq(sevenAm.toLocalDate())))
                .thenReturn(List.of(dueToday));
        doThrow(new RuntimeException("smtp down"))
                .when(emailService).sendDailyReminderEmail(any(), any(), any(), any());

        reminderService.sendReminderIfDue(user, sevenAm);

        verify(userService, never()).saveUser(any());
        assertNull(user.getLastReminderSentAt());
    }
}
