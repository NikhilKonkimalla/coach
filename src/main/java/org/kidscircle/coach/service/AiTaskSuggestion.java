package org.kidscircle.coach.service;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AiTaskSuggestion {

    private String title;
    private String description;
    private Integer estimatedMinutes;
    private String priority = "MEDIUM";
    private String definitionOfDone;

    public AiTaskSuggestion() {}

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Integer getEstimatedMinutes() { return estimatedMinutes; }
    public void setEstimatedMinutes(Integer estimatedMinutes) { this.estimatedMinutes = estimatedMinutes; }

    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }

    public String getDefinitionOfDone() { return definitionOfDone; }
    public void setDefinitionOfDone(String definitionOfDone) { this.definitionOfDone = definitionOfDone; }
}
