package org.kidscircle.coach.db;

import java.util.List;

import org.kidscircle.coach.model.Goal;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

public interface GoalRepository extends CrudRepository<Goal, Long>{
	

}
