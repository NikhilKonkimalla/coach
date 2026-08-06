package org.kidscircle.coach.db;

import org.kidscircle.coach.model.WorkSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class WorkSessionServiceImpl implements WorkSessionService {

    @Autowired
    private WorkSessionRepository workSessionRepository;

    @Override
    public List<WorkSession> getSessionsForToday(long userId) {
        return workSessionRepository.findByUserIdAndScheduledDate(userId, LocalDate.now());
    }

    @Override
    public List<WorkSession> getSessionsForWeek(long userId, LocalDate weekStart, LocalDate weekEnd) {
        return workSessionRepository.findByUserIdAndWeek(userId, weekStart, weekEnd);
    }

    @Override
    public void saveSession(WorkSession session) {
        workSessionRepository.save(session);
    }

    @Override
    public void deleteSession(long id) {
        workSessionRepository.deleteById(id);
    }

    @Override
    public void completeSession(long id, int actualMinutes, String note) {
        WorkSession session = workSessionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Session not found: " + id));
        session.setStatus("COMPLETED");
        session.setActualMinutes(actualMinutes);
        session.setCompletionNote(note);
        workSessionRepository.save(session);
    }
}
