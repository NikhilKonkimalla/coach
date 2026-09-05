package org.kidscircle.coach.service;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AiTaskSuggestion {

    private String title;
    private String description;
    private Integer estimatedMinutes;
    private String priority = "MEDIUM";
    private String frequency = "ONCE";
    private Integer timesPerPeriod = 1;

    // Model sometimes returns this as a JSON array — deserializer handles both
    @JsonDeserialize(using = StringOrArrayDeserializer.class)
    private String definitionOfDone;

    // Not part of the AI's JSON — filled in by the controller afterward so the
    // review page can group/label suggestions by milestone and give each a
    // unique form field index across all milestones combined.
    private Long milestoneId;
    private String milestoneTitle;
    private int globalIndex;

    public AiTaskSuggestion() {}

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Integer getEstimatedMinutes() { return estimatedMinutes; }
    // The review form's estimate field has min="1" — a non-positive value from the
    // model would fail that HTML5 validation on submit, so treat it as "no estimate".
    public void setEstimatedMinutes(Integer estimatedMinutes) {
        this.estimatedMinutes = (estimatedMinutes != null && estimatedMinutes >= 1) ? estimatedMinutes : null;
    }

    public String getPriority() { return priority; }
    public void setPriority(String p) { this.priority = (p != null) ? p.toUpperCase() : "MEDIUM"; }

    public String getFrequency() { return frequency; }
    public void setFrequency(String f) { this.frequency = (f != null) ? f.toUpperCase() : "ONCE"; }

    public Integer getTimesPerPeriod() { return timesPerPeriod; }
    // The review form's times-per-period field has min="1" — the model sometimes
    // returns 0 (e.g. for a ONCE-frequency task), which would fail that validation.
    public void setTimesPerPeriod(Integer timesPerPeriod) {
        this.timesPerPeriod = (timesPerPeriod != null && timesPerPeriod >= 1) ? timesPerPeriod : 1;
    }

    public String getDefinitionOfDone() { return definitionOfDone; }
    public void setDefinitionOfDone(String definitionOfDone) { this.definitionOfDone = definitionOfDone; }

    public Long getMilestoneId() { return milestoneId; }
    public void setMilestoneId(Long milestoneId) { this.milestoneId = milestoneId; }

    public String getMilestoneTitle() { return milestoneTitle; }
    public void setMilestoneTitle(String milestoneTitle) { this.milestoneTitle = milestoneTitle; }

    public int getGlobalIndex() { return globalIndex; }
    public void setGlobalIndex(int globalIndex) { this.globalIndex = globalIndex; }

    // Handles "done" (string) and ["done1", "done2"] (array) from the model
    public static class StringOrArrayDeserializer extends StdDeserializer<String> {
        public StringOrArrayDeserializer() { super(String.class); }

        @Override
        public String deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            if (p.currentToken() == JsonToken.START_ARRAY) {
                List<String> items = new ArrayList<>();
                while (p.nextToken() != JsonToken.END_ARRAY) {
                    items.add(p.getText());
                }
                return String.join(". ", items);
            }
            return p.getValueAsString();
        }
    }
}
