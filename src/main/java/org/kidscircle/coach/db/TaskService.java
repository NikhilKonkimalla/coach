package org.kidscircle.coach.db;

import org.kidscircle.coach.model.Task;

import java.time.LocalDate;
import java.util.List;

public interface TaskService {

    List<Task> getTasksForUser(long userId);

    List<Task> getTasksForGoal(long goalId);

    List<Task> getTopLevelTasksForGoal(long goalId);

    List<Task> getDueTodayForUser(long userId);

    List<Task> getTasksForWeek(long userId, LocalDate weekStart, LocalDate weekEnd);

    List<Task> getReadyTasksForUser(long userId);

    void saveTask(Task task);

    Task getTaskById(long id);

    void deleteTaskById(long id);

    void completeTask(long taskId, Integer actualMinutes, String completionNote);

    void updateStatus(long taskId, String status);
}
