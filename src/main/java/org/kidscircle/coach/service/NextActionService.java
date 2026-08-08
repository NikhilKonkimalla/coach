package org.kidscircle.coach.service;

import org.kidscircle.coach.db.GoalRepository;
import org.kidscircle.coach.db.TaskRepository;
import org.kidscircle.coach.model.Goal;
import org.kidscircle.coach.model.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;

@Service
public class NextActionService {

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private GoalRepository goalRepository;

    public static class Recommendation {
        public final Task task;
        public final String reason;
        public Recommendation(Task task, String reason) {
            this.task = task;
            this.reason = reason;
        }
    }

    public Recommendation getNextAction(long userId) {
        List<Task> candidates = taskRepository.findReadyTasksForUser(userId);
        if (candidates == null || candidates.isEmpty()) return null;

        LocalDate today = LocalDate.now();
        Task best = null;
        String reason = null;
        int bestScore = -1;

        for (Task t : candidates) {
            int score = 0;

            if ("IN_PROGRESS".equals(t.getStatus())) score += 100;
            if (t.getDueDate() != null && t.getDueDate().isBefore(today)) score += 80;
            if (t.getDueDate() != null && t.getDueDate().isEqual(today)) score += 60;
            if (t.getDueDate() != null && !t.getDueDate().isAfter(today.plusDays(3))) score += 40;
            if ("CRITICAL".equals(t.getPriority())) score += 30;
            if ("HIGH".equals(t.getPriority())) score += 20;
            if ("MEDIUM".equals(t.getPriority())) score += 10;

            if (score > bestScore) {
                bestScore = score;
                best = t;
            }
        }

        if (best == null) return null;

        if ("IN_PROGRESS".equals(best.getStatus())) {
            reason = "You already started this task — keep going!";
        } else if (best.getDueDate() != null && best.getDueDate().isBefore(today)) {
            reason = "Overdue since " + best.getDueDate() + ".";
        } else if (best.getDueDate() != null && best.getDueDate().isEqual(today)) {
            reason = "Due today.";
        } else if (best.getDueDate() != null && !best.getDueDate().isAfter(today.plusDays(3))) {
            reason = "Due soon (" + best.getDueDate() + ").";
        } else if ("CRITICAL".equals(best.getPriority())) {
            reason = "Highest priority task in your active goals.";
        } else {
            reason = "Next ready task in your plan.";
        }

        return new Recommendation(best, reason);
    }
}
