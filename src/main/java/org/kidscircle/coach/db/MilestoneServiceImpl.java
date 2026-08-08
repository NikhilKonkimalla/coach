package org.kidscircle.coach.db;

import org.kidscircle.coach.model.Milestone;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MilestoneServiceImpl implements MilestoneService {

    @Autowired
    private MilestoneRepository milestoneRepository;

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
    public void deleteMilestoneById(long id) {
        milestoneRepository.deleteById(id);
    }
}
