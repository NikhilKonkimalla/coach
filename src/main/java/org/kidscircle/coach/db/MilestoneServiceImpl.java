package org.kidscircle.coach.db;

import org.kidscircle.coach.model.Milestone;
import org.kidscircle.coach.model.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class MilestoneServiceImpl implements MilestoneService {

    @Autowired
    private MilestoneRepository milestoneRepository;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private WorkSessionRepository workSessionRepository;

    @Override
    public List<Milestone> getMilestonesForGoal(long goalId) {
        return milestoneRepository.findByGoalIdOrderBySequenceAsc(goalId);
    }

    @Override
    public void saveMilestone(Milestone milestone) {
        milestoneRepository.save(milestone);
    }

    @Override
    public Milestone getMilestoneById(long id) {
        return milestoneRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Milestone not found: " + id));
    }

    @Override
    @Transactional
    public void deleteMilestoneById(long id) {
        List<Task> tasks = taskRepository.findByMilestoneId(id);
        List<Long> taskIds = tasks.stream().map(Task::getTaskId).collect(Collectors.toList());
        for (Long taskId : taskIds) {
            workSessionRepository.deleteAll(workSessionRepository.findByTaskId(taskId));
        }
        taskRepository.deleteAll(tasks);
        milestoneRepository.deleteById(id);
    }
}
