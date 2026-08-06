package org.kidscircle.coach.db;

import org.kidscircle.coach.model.Goal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GoalRepository extends JpaRepository<Goal, Long> {

    List<Goal> findByUserId(Long userId);

    List<Goal> findByUserIdAndStatus(Long userId, String status);

    @Query("select g from Goal g where g.userId = :userId and g.status not in ('COMPLETED','ABANDONED') order by g.createdAt desc")
    List<Goal> findActiveByUserId(@Param("userId") Long userId);
}
