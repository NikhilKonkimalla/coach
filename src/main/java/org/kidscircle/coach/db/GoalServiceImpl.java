package org.kidscircle.coach.db;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.kidscircle.coach.model.Goal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class GoalServiceImpl implements GoalService{
	
	    @Autowired
	    private GoalRepository goalRepository;

	    @Override
	    public List < Goal > getGoalForUser(long userId) 
	    { 
	    	List<Goal> goals = (List<Goal>) goalRepository.findAll();
	    	return goals.stream().filter(g-> g.getUserId()==userId).collect(Collectors.toList());
	    }

	    @Override
	    public void saveGoal(Goal goal) {
	        this.goalRepository.save(goal);
	    }

	    @Override
	    public Goal getGoalById(long id) {
	        Optional < Goal > optional = goalRepository.findById(id);
	        Goal goal = null;
	        if (optional.isPresent()) {
	            goal = optional.get();
	        } else {
	            throw new RuntimeException(" Goal not found for id :: " + id);
	        }
	        return goal;
	    }

	    @Override
	    public void deleteGoalById(long id) {
	        this.goalRepository.deleteById(id);
	    }

}
