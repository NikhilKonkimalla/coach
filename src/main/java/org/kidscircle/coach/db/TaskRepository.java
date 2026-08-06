package org.kidscircle.coach.db;

import org.kidscircle.coach.model.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {

    List<Task> findByUserId(Long userId);

    List<Task> findByGoalId(Long goalId);

    List<Task> findByGoalIdAndStatus(Long goalId, String status);

    @Query("select t from Task t where t.userId = :userId and t.dueDate = :date and t.status not in ('COMPLETED','CANCELED','SKIPPED')")
    List<Task> findDueTodayForUser(@Param("userId") Long userId, @Param("date") LocalDate date);

    @Query("select t from Task t where t.userId = :userId and t.dueDate between :weekStart and :weekEnd and t.status not in ('COMPLETED','CANCELED','SKIPPED')")
    List<Task> findForWeek(@Param("userId") Long userId, @Param("weekStart") LocalDate weekStart, @Param("weekEnd") LocalDate weekEnd);

    @Query("select t from Task t where t.userId = :userId and t.status in ('READY','IN_PROGRESS') order by t.priority desc, t.dueDate asc")
    List<Task> findReadyTasksForUser(@Param("userId") Long userId);

    List<Task> findByGoalIdAndParentTaskIdIsNull(Long goalId);
}
