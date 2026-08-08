package org.kidscircle.coach.db;

import org.kidscircle.coach.model.WorkSession;

import java.time.LocalDate;
import java.util.List;

public interface WorkSessionService {

    List<WorkSession> getSessionsForToday(long userId);

    List<WorkSession> getSessionsForWeek(long userId, LocalDate weekStart, LocalDate weekEnd);

    void saveSession(WorkSession session);

    void deleteSession(long id);

    void completeSession(long id, int actualMinutes, String note);
}
