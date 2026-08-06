package org.kidscircle.coach.db;

import org.kidscircle.coach.model.Goal;

import java.util.List;

public interface GoalService {

    List<Goal> getGoalForUser(long userId);

    List<Goal> getActiveGoalsForUser(long userId);

    void saveGoal(Goal goal);

    Goal getGoalById(long id);

    void deleteGoalById(long id);

    void updateStatus(long goalId, String status);

    int calculateProgressPercent(long goalId);
}
