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

    // Model sometimes returns this as a JSON array — deserializer handles both
    @JsonDeserialize(using = StringOrArrayDeserializer.class)
    private String definitionOfDone;

    public AiTaskSuggestion() {}

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Integer getEstimatedMinutes() { return estimatedMinutes; }
    public void setEstimatedMinutes(Integer estimatedMinutes) { this.estimatedMinutes = estimatedMinutes; }

    public String getPriority() { return priority; }
    public void setPriority(String p) { this.priority = (p != null) ? p.toUpperCase() : "MEDIUM"; }

    public String getFrequency() { return frequency; }
    public void setFrequency(String f) { this.frequency = (f != null) ? f.toUpperCase() : "ONCE"; }

    public String getDefinitionOfDone() { return definitionOfDone; }
    public void setDefinitionOfDone(String definitionOfDone) { this.definitionOfDone = definitionOfDone; }

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
