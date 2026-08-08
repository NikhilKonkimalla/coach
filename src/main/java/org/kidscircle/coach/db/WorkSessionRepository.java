package org.kidscircle.coach.db;

import org.kidscircle.coach.model.WorkSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface WorkSessionRepository extends JpaRepository<WorkSession, Long> {

    List<WorkSession> findByUserIdAndScheduledDate(Long userId, LocalDate date);

    @Query("select s from WorkSession s where s.userId = :userId and s.scheduledDate between :start and :end")
    List<WorkSession> findByUserIdAndWeek(@Param("userId") Long userId,
                                          @Param("start") LocalDate start,
                                          @Param("end") LocalDate end);

    List<WorkSession> findByTaskId(Long taskId);
}
