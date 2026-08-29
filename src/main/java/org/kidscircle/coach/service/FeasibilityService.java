package org.kidscircle.coach.service;

import org.kidscircle.coach.db.TaskRepository;
import org.kidscircle.coach.model.Goal;
import org.kidscircle.coach.model.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class FeasibilityService {

    @Autowired
    private TaskRepository taskRepository;

    public FeasibilityResult calculate(Goal goal) {
        if (Boolean.TRUE.equals(goal.getLifelong())) {
            return FeasibilityResult.lifelong();
        }
        if (goal.getTargetDate() == null) {
            return FeasibilityResult.incompleteData("Goal has no target date.");
        }
        if (goal.getWeeklyCapacityMinutes() == null || goal.getWeeklyCapacityMinutes() <= 0) {
            return FeasibilityResult.incompleteData("Goal has no weekly capacity set.");
        }

        LocalDate today = LocalDate.now();
        LocalDate target = goal.getTargetDate();

        if (!target.isAfter(today)) {
            FeasibilityResult r = new FeasibilityResult();
            r.setStatus(FeasibilityResult.Status.INVALID);
            r.setExplanation("Target date is in the past.");
            return r;
        }

        long daysRemaining = ChronoUnit.DAYS.between(today, target);
        int weeksRemaining = Math.max(1, (int) Math.ceil(daysRemaining / 7.0));

        List<Task> tasks = taskRepository.findByGoalId(goal.getGoalId());
        int totalRemaining = 0;
        int missingEstimates = 0;

        for (Task t : tasks) {
            if ("COMPLETED".equals(t.getStatus()) || "CANCELED".equals(t.getStatus()) || "SKIPPED".equals(t.getStatus())) {
                continue;
            }
            if (t.getEstimatedMinutes() == null) {
                missingEstimates++;
            } else {
                totalRemaining += t.getEstimatedMinutes();
            }
        }

        int weeklyCapacity = goal.getWeeklyCapacityMinutes();
        int availableMinutes = weeksRemaining * weeklyCapacity;
        int requiredPerWeek = weeksRemaining > 0 ? (int) Math.ceil((double) totalRemaining / weeksRemaining) : totalRemaining;
        int utilization = weeklyCapacity > 0 ? (int) ((double) requiredPerWeek / weeklyCapacity * 100) : 999;

        FeasibilityResult result = new FeasibilityResult();
        result.setTotalRemainingMinutes(totalRemaining);
        result.setWeeklyCapacityMinutes(weeklyCapacity);
        result.setWeeksRemaining(weeksRemaining);
        result.setAvailableMinutes(availableMinutes);
        result.setRequiredMinutesPerWeek(requiredPerWeek);
        result.setCapacityUtilizationPercent(utilization);
        result.setTasksWithoutEstimates(missingEstimates);

        if (missingEstimates > 0 && totalRemaining == 0) {
            result.setStatus(FeasibilityResult.Status.INCOMPLETE_DATA);
            result.setExplanation(missingEstimates + " task(s) have no estimate. Add estimates to get an accurate feasibility check.");
        } else if (utilization <= 80) {
            result.setStatus(FeasibilityResult.Status.FEASIBLE);
            result.setExplanation(String.format(
                    "This plan requires about %d min/week. You have %d min/week available — you're on track.",
                    requiredPerWeek, weeklyCapacity));
        } else if (utilization <= 100) {
            result.setStatus(FeasibilityResult.Status.TIGHT);
            result.setExplanation(String.format(
                    "This plan requires about %d min/week. You have %d min/week available — it's doable but tight.",
                    requiredPerWeek, weeklyCapacity));
        } else {
            result.setStatus(FeasibilityResult.Status.OVER_CAPACITY);
            result.setExplanation(String.format(
                    "This plan requires about %d min/week but you only have %d min/week. Consider extending the target date, increasing capacity, or removing lower-priority tasks.",
                    requiredPerWeek, weeklyCapacity));
        }

        if (missingEstimates > 0) {
            result.setExplanation(result.getExplanation() + " (Note: " + missingEstimates + " task(s) have no estimate and are not included.)");
        }

        return result;
    }
}
