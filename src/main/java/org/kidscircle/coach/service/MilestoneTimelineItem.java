package org.kidscircle.coach.service;

import org.kidscircle.coach.model.Milestone;

public class MilestoneTimelineItem {

    private final Milestone milestone;
    private final int positionPercent;
    private final String status; // COMPLETED, OVERDUE, UPCOMING
    private final boolean labelAbove;

    public MilestoneTimelineItem(Milestone milestone, int positionPercent, String status, boolean labelAbove) {
        this.milestone = milestone;
        this.positionPercent = positionPercent;
        this.status = status;
        this.labelAbove = labelAbove;
    }

    public Milestone getMilestone() { return milestone; }
    public int getPositionPercent() { return positionPercent; }
    public String getStatus() { return status; }
    public boolean isLabelAbove() { return labelAbove; }
}
