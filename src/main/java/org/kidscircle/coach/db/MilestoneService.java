package org.kidscircle.coach.db;

import org.kidscircle.coach.model.Milestone;

import java.util.List;

public interface MilestoneService {

    List<Milestone> getMilestonesForGoal(long goalId);

    void saveMilestone(Milestone milestone);

    Milestone getMilestoneById(long id);

    void deleteMilestoneById(long id);
}
