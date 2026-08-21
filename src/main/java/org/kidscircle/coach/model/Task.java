package org.kidscircle.coach.model;

import org.springframework.format.annotation.DateTimeFormat;
import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "task")
public class Task {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long taskId;

    private Long goalId;
    private Long userId;
    private Long milestoneId;
    private Long parentTaskId;

    // Mapped to existing task_name column; used as task title
    @NotBlank
    @Size(max = 200)
    @Column(name = "task_name")
    private String title;

    @Size(max = 2000)
    private String description;

    @Column(name = "definition_of_done")
    private String definitionOfDone;

    @Column(name = "estimated_minutes")
    private Integer estimatedMinutes;

    @Column(name = "actual_minutes")
    private Integer actualMinutes;

    private String priority = "MEDIUM";

    private String status = "READY";

    @Column(name = "scheduling_type")
    private String schedulingType = "CHECKLIST";

    // ONCE, DAILY, WEEKLY, or MONTHLY — how often this task repeats
    private String frequency = "ONCE";

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    @Column(name = "due_date")
    private LocalDate dueDate;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    @Column(name = "earliest_start_date")
    private LocalDate earliestStartDate;

    private Integer sequence = 0;

    private Boolean required = true;

    private String source = "USER";

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

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
        if (status == null) status = "READY";
        if (priority == null) priority = "MEDIUM";
        if (source == null) source = "USER";
        if (schedulingType == null) schedulingType = "CHECKLIST";
        if (frequency == null) frequency = "ONCE";
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public Task() {}

    // Backward-compatible accessor
    public String getTaskName() { return title; }
    public void setTaskName(String name) { this.title = name; }

    public boolean isCompleted() {
        return "COMPLETED".equals(status);
    }

    public boolean isOverdue() {
        return dueDate != null && dueDate.isBefore(LocalDate.now()) && !isCompleted();
    }

    public Long getTaskId() { return taskId; }
    public void setTaskId(Long taskId) { this.taskId = taskId; }

    public Long getGoalId() { return goalId; }
    public void setGoalId(Long goalId) { this.goalId = goalId; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public Long getMilestoneId() { return milestoneId; }
    public void setMilestoneId(Long milestoneId) { this.milestoneId = milestoneId; }

    public Long getParentTaskId() { return parentTaskId; }
    public void setParentTaskId(Long parentTaskId) { this.parentTaskId = parentTaskId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getDefinitionOfDone() { return definitionOfDone; }
    public void setDefinitionOfDone(String definitionOfDone) { this.definitionOfDone = definitionOfDone; }

    public Integer getEstimatedMinutes() { return estimatedMinutes; }
    public void setEstimatedMinutes(Integer estimatedMinutes) { this.estimatedMinutes = estimatedMinutes; }

    public Integer getActualMinutes() { return actualMinutes; }
    public void setActualMinutes(Integer actualMinutes) { this.actualMinutes = actualMinutes; }

    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getSchedulingType() { return schedulingType; }
    public void setSchedulingType(String schedulingType) { this.schedulingType = schedulingType; }

    public String getFrequency() { return frequency; }
    public void setFrequency(String frequency) { this.frequency = frequency; }

    public LocalDate getDueDate() { return dueDate; }
    public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }

    public LocalDate getEarliestStartDate() { return earliestStartDate; }
    public void setEarliestStartDate(LocalDate earliestStartDate) { this.earliestStartDate = earliestStartDate; }

    public Integer getSequence() { return sequence; }
    public void setSequence(Integer sequence) { this.sequence = sequence; }

    public Boolean getRequired() { return required; }
    public void setRequired(Boolean required) { this.required = required; }

    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }

    public LocalDateTime getCompletedAt() { return completedAt; }
    public void setCompletedAt(LocalDateTime completedAt) { this.completedAt = completedAt; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public Long getVersion() { return version; }
}
