package org.kidscircle.coach.db;

import org.kidscircle.coach.model.Milestone;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MilestoneRepository extends JpaRepository<Milestone, Long> {

    List<Milestone> findByGoalIdOrderBySequenceAsc(Long goalId);
}
