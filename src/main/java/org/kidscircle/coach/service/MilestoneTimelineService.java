package org.kidscircle.coach.service;

import org.kidscircle.coach.model.Goal;
import org.kidscircle.coach.model.Milestone;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class MilestoneTimelineService {

    private static final int WINDOW_CAP_YEARS = 2;

    public MilestoneTimelineResult build(Goal goal, List<Milestone> milestones) {
        LocalDate today = LocalDate.now();
        LocalDate cap = today.plusYears(WINDOW_CAP_YEARS);
        boolean capped = goal.getTargetDate() == null || goal.getTargetDate().isAfter(cap);
        LocalDate windowEnd = capped ? cap : goal.getTargetDate();
        long totalDays = Math.max(1, ChronoUnit.DAYS.between(today, windowEnd));

        List<Milestone> inWindow = milestones.stream()
                .filter(m -> m.getTargetDate() != null && !m.getTargetDate().isAfter(windowEnd))
                .sorted(Comparator.comparing(Milestone::getTargetDate))
                .collect(java.util.stream.Collectors.toList());

        List<MilestoneTimelineItem> items = new ArrayList<>();
        for (int i = 0; i < inWindow.size(); i++) {
            Milestone m = inWindow.get(i);
            long daysFromToday = ChronoUnit.DAYS.between(today, m.getTargetDate());
            long clamped = Math.max(0, Math.min(totalDays, daysFromToday));
            int pct = (int) Math.round((clamped * 100.0) / totalDays);

            String status;
            if ("COMPLETED".equals(m.getStatus())) {
                status = "COMPLETED";
            } else if (m.getTargetDate().isBefore(today)) {
                status = "OVERDUE";
            } else {
                status = "UPCOMING";
            }

            items.add(new MilestoneTimelineItem(m, pct, status, i % 2 == 0));
        }

        return new MilestoneTimelineResult(items, windowEnd, capped);
    }
}
