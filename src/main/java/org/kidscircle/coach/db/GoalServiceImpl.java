package org.kidscircle.coach.db;

import org.kidscircle.coach.model.Goal;
import org.kidscircle.coach.model.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class GoalServiceImpl implements GoalService {

    @Autowired
    private GoalRepository goalRepository;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private MilestoneRepository milestoneRepository;

    @Autowired
    private WorkSessionRepository workSessionRepository;

    @Override
    public List<Goal> getGoalForUser(long userId) {
        return goalRepository.findByUserId(userId);
    }

    @Override
    public List<Goal> getActiveGoalsForUser(long userId) {
        return goalRepository.findActiveByUserId(userId);
    }

    @Override
    public void saveGoal(Goal goal) {
        goalRepository.save(goal);
    }

    @Override
    public Goal getGoalById(long id) {
        Optional<Goal> optional = goalRepository.findById(id);
        return optional.orElseThrow(() -> new RuntimeException("Goal not found: " + id));
    }

    @Override
    @Transactional
    public void deleteGoalById(long id) {
        workSessionRepository.deleteAll(workSessionRepository.findByGoalId(id));
        taskRepository.deleteAll(taskRepository.findByGoalId(id));
        milestoneRepository.deleteAll(milestoneRepository.findByGoalIdOrderBySequenceAsc(id));
        goalRepository.deleteById(id);
    }

    @Override
    public void updateStatus(long goalId, String status) {
        Goal goal = getGoalById(goalId);
        goal.setStatus(status);
        goalRepository.save(goal);
    }

    @Override
    public int calculateProgressPercent(long goalId) {
        List<Task> tasks = taskRepository.findByGoalId(goalId);
        if (tasks == null || tasks.isEmpty()) return 0;
        long required = tasks.stream().filter(t -> Boolean.TRUE.equals(t.getRequired()) && t.getParentTaskId() == null).count();
        if (required == 0) return 0;
        long completed = tasks.stream()
                .filter(t -> Boolean.TRUE.equals(t.getRequired()) && t.getParentTaskId() == null && "COMPLETED".equals(t.getStatus()))
                .count();
        return (int) ((completed * 100) / required);
    }
}
