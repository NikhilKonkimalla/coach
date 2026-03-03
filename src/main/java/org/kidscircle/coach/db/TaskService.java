package org.kidscircle.coach.db;

import org.kidscircle.coach.model.Task;
import java.util.List;

public interface TaskService {
    List<Task> getTasksForUser(long userId);
    List<Task> getTasksForGoal(long goalId);
    void saveTask(Task task);
    void deleteTaskById(long id);
}
