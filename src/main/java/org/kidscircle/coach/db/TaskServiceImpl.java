package org.kidscircle.coach.db;

import org.kidscircle.coach.model.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class TaskServiceImpl implements TaskService {

    @Autowired
    private TaskRepository taskRepository;

    @Override
    public List<Task> getTasksForUser(long userId) {
        return taskRepository.findByUserId(userId);
    }

    @Override
    public List<Task> getTasksForGoal(long goalId) {
        return taskRepository.findByGoalId(goalId);
    }

    @Override
    public List<Task> getTopLevelTasksForGoal(long goalId) {
        return taskRepository.findByGoalIdAndParentTaskIdIsNull(goalId);
    }

    @Override
    public List<Task> getDueTodayForUser(long userId) {
        return taskRepository.findDueTodayForUser(userId, LocalDate.now());
    }

    @Override
    public List<Task> getTasksForWeek(long userId, LocalDate weekStart, LocalDate weekEnd) {
        return taskRepository.findForWeek(userId, weekStart, weekEnd);
    }

    @Override
    public List<Task> getReadyTasksForUser(long userId) {
        return taskRepository.findReadyTasksForUser(userId);
    }

    @Override
    public void saveTask(Task task) {
        taskRepository.save(task);
    }

    @Override
    public Task getTaskById(long id) {
        Optional<Task> opt = taskRepository.findById(id);
        return opt.orElseThrow(() -> new RuntimeException("Task not found: " + id));
    }

    @Override
    public void deleteTaskById(long id) {
        taskRepository.deleteById(id);
    }

    @Override
    public void completeTask(long taskId, Integer actualMinutes, String completionNote) {
        Task task = getTaskById(taskId);
        task.setStatus("COMPLETED");
        task.setCompletedAt(LocalDateTime.now());
        if (actualMinutes != null) task.setActualMinutes(actualMinutes);
        taskRepository.save(task);
    }

    @Override
    public void updateStatus(long taskId, String status) {
        Task task = getTaskById(taskId);
        task.setStatus(status);
        if ("COMPLETED".equals(status) && task.getCompletedAt() == null) {
            task.setCompletedAt(LocalDateTime.now());
        } else if (!"COMPLETED".equals(status)) {
            task.setCompletedAt(null);
        }
        taskRepository.save(task);
    }
}
