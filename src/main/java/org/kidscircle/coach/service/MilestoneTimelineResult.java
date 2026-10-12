package org.kidscircle.coach.service;

import java.time.LocalDate;
import java.util.List;

public class MilestoneTimelineResult {

    private final List<MilestoneTimelineItem> items;
    private final LocalDate windowEnd;
    private final boolean capped;

    public MilestoneTimelineResult(List<MilestoneTimelineItem> items, LocalDate windowEnd, boolean capped) {
        this.items = items;
        this.windowEnd = windowEnd;
        this.capped = capped;
    }

    public List<MilestoneTimelineItem> getItems() { return items; }
    public LocalDate getWindowEnd() { return windowEnd; }
    public boolean isCapped() { return capped; }
}
