package org.kidscircle.coach.db;

import java.util.List;

import org.kidscircle.coach.model.Goal;

public interface GoalService {

	List<Goal> getGoalForUser(long userId);

	void saveGoal(Goal goal);

	Goal getGoalById(long id);

	void deleteGoalById(long id);

}
