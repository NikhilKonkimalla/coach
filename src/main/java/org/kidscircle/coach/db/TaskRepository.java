package org.kidscircle.coach.db;

import org.kidscircle.coach.model.Task;
import org.springframework.data.repository.CrudRepository;
import java.util.List;

public interface TaskRepository extends CrudRepository<Task, Long> {
    List<Task> findByUserId(Long userId);
    List<Task> findByGoalId(Long goalId);
}
