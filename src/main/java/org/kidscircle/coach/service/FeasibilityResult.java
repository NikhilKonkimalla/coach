package org.kidscircle.coach.service;

public class FeasibilityResult {

    public enum Status { FEASIBLE, TIGHT, OVER_CAPACITY, INCOMPLETE_DATA, INVALID }

    private Status status;
    private int totalRemainingMinutes;
    private int weeklyCapacityMinutes;
    private int weeksRemaining;
    private int availableMinutes;
    private int requiredMinutesPerWeek;
    private int capacityUtilizationPercent;
    private String explanation;
    private int tasksWithoutEstimates;

    public static FeasibilityResult incompleteData(String reason) {
        FeasibilityResult r = new FeasibilityResult();
        r.status = Status.INCOMPLETE_DATA;
        r.explanation = reason;
        return r;
    }

    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }

    public int getTotalRemainingMinutes() { return totalRemainingMinutes; }
    public void setTotalRemainingMinutes(int totalRemainingMinutes) { this.totalRemainingMinutes = totalRemainingMinutes; }

    public int getWeeklyCapacityMinutes() { return weeklyCapacityMinutes; }
    public void setWeeklyCapacityMinutes(int weeklyCapacityMinutes) { this.weeklyCapacityMinutes = weeklyCapacityMinutes; }

    public int getWeeksRemaining() { return weeksRemaining; }
    public void setWeeksRemaining(int weeksRemaining) { this.weeksRemaining = weeksRemaining; }

    public int getAvailableMinutes() { return availableMinutes; }
    public void setAvailableMinutes(int availableMinutes) { this.availableMinutes = availableMinutes; }

    public int getRequiredMinutesPerWeek() { return requiredMinutesPerWeek; }
    public void setRequiredMinutesPerWeek(int requiredMinutesPerWeek) { this.requiredMinutesPerWeek = requiredMinutesPerWeek; }

    public int getCapacityUtilizationPercent() { return capacityUtilizationPercent; }
    public void setCapacityUtilizationPercent(int capacityUtilizationPercent) { this.capacityUtilizationPercent = capacityUtilizationPercent; }

    public String getExplanation() { return explanation; }
    public void setExplanation(String explanation) { this.explanation = explanation; }

    public int getTasksWithoutEstimates() { return tasksWithoutEstimates; }
    public void setTasksWithoutEstimates(int tasksWithoutEstimates) { this.tasksWithoutEstimates = tasksWithoutEstimates; }

    public boolean isFeasible() { return status == Status.FEASIBLE || status == Status.TIGHT; }

    public String getStatusLabel() {
        if (status == null) return "Unknown";
        switch (status) {
            case FEASIBLE: return "On track";
            case TIGHT: return "Tight";
            case OVER_CAPACITY: return "Over capacity";
            case INCOMPLETE_DATA: return "Incomplete data";
            case INVALID: return "Invalid";
            default: return status.name();
        }
    }

    public String getStatusCssClass() {
        if (status == null) return "badge-secondary";
        switch (status) {
            case FEASIBLE: return "badge-success";
            case TIGHT: return "badge-warning";
            case OVER_CAPACITY: return "badge-danger";
            case INCOMPLETE_DATA: return "badge-secondary";
            case INVALID: return "badge-danger";
            default: return "badge-secondary";
        }
    }
}
