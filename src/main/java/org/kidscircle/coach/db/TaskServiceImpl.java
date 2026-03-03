package org.kidscircle.coach.db;

import org.kidscircle.coach.model.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

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
    public void saveTask(Task task) {
        taskRepository.save(task);
    }

    @Override
    public void deleteTaskById(long id) {
        taskRepository.deleteById(id);
    }
}
