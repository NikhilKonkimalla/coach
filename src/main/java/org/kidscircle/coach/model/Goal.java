package org.kidscircle.coach.model;

import org.springframework.format.annotation.DateTimeFormat;
import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "goal")
public class Goal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long goalId;

    private Long userId;

    // Mapped to existing goal_name column; used as the goal title
    @NotBlank
    @Size(max = 120)
    @Column(name = "goal_name")
    private String title;

    // Mapped to existing goal_importance column; used as description
    @Size(max = 2000)
    @Column(name = "goal_importance")
    private String description;

    // Legacy date fields kept for backward compatibility
    private Integer monthNumber;
    private Integer year;
    private Integer prepStartMonths;

    // New fields added for the Coach MVP
    @Column(name = "status")
    private String status = "DRAFT";

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    @Column(name = "target_date")
    private LocalDate targetDate;

    // True for open-ended goals with no target date (e.g. "Stay healthy")
    private Boolean lifelong = false;

    @Column(name = "weekly_capacity_minutes")
    private Integer weeklyCapacityMinutes = 300;

    private String priority = "MEDIUM";

    @Size(max = 1000)
    @Column(name = "success_criteria", length = 1000)
    private String successCriteria;

    @Size(max = 1000)
    private String motivation;

    private String category;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    @Column(name = "earliest_start_date")
    private LocalDate earliestStartDate;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Version
    private Long version;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (status == null) status = "DRAFT";
        if (lifelong == null) lifelong = false;
        if (targetDate == null && year != null && year > 0 && monthNumber != null && monthNumber > 0) {
            targetDate = LocalDate.of(year, monthNumber, 1).withDayOfMonth(
                    LocalDate.of(year, monthNumber, 1).lengthOfMonth());
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public Goal() {}

    public Goal(String title) {
        this.title = title;
    }

    // Convenience: return title or fall back to legacy goalName
    public String getGoalName() { return title; }
    public void setGoalName(String name) { this.title = name; }

    public Long getGoalId() { return goalId; }
    public void setGoalId(Long id) { this.goalId = id; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getGoalImportance() { return description; }
    public void setGoalImportance(String s) { this.description = s; }

    public Integer getMonthNumber() { return monthNumber; }
    public void setMonthNumber(Integer monthNumber) { this.monthNumber = monthNumber; }

    public Integer getYear() { return year; }
    public void setYear(Integer year) { this.year = year; }

    public Integer getPrepStartMonths() { return prepStartMonths; }
    public void setPrepStartMonths(Integer prepStartMonths) { this.prepStartMonths = prepStartMonths; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDate getTargetDate() { return targetDate; }
    public void setTargetDate(LocalDate targetDate) { this.targetDate = targetDate; }

    public Boolean getLifelong() { return lifelong; }
    public void setLifelong(Boolean lifelong) { this.lifelong = lifelong; }

    public Integer getWeeklyCapacityMinutes() { return weeklyCapacityMinutes; }
    public void setWeeklyCapacityMinutes(Integer weeklyCapacityMinutes) { this.weeklyCapacityMinutes = weeklyCapacityMinutes; }

    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }

    public String getSuccessCriteria() { return successCriteria; }
    public void setSuccessCriteria(String successCriteria) { this.successCriteria = successCriteria; }

    public String getMotivation() { return motivation; }
    public void setMotivation(String motivation) { this.motivation = motivation; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public LocalDate getEarliestStartDate() { return earliestStartDate; }
    public void setEarliestStartDate(LocalDate earliestStartDate) { this.earliestStartDate = earliestStartDate; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public Long getVersion() { return version; }
}
