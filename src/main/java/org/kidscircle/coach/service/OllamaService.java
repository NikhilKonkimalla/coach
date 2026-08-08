package org.kidscircle.coach.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.kidscircle.coach.model.Goal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
public class OllamaService {

    private static final Logger log = LoggerFactory.getLogger(OllamaService.class);

    @Value("${ollama.base-url:http://localhost:11434}")
    private String baseUrl;

    @Value("${ollama.model:qwen3:8b}")
    private String model;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper;

    public OllamaService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public List<AiTaskSuggestion> suggestTasks(Goal goal) {
        try {
            String prompt = buildPrompt(goal);

            Map<String, Object> body = new HashMap<>();
            body.put("model", model);
            body.put("stream", false);
            body.put("messages", List.of(Map.of("role", "user", "content", prompt)));

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

            @SuppressWarnings("unchecked")
            ResponseEntity<Map> responseEntity = restTemplate.exchange(
                    baseUrl + "/api/chat", HttpMethod.POST, request, Map.class);
            Map<String, Object> response = responseEntity.getBody();

            if (response == null) return Collections.emptyList();

            @SuppressWarnings("unchecked")
            Map<String, Object> message = (Map<String, Object>) response.get("message");
            String content = (String) message.get("content");

            content = stripThinking(content);
            content = stripCodeFences(content);
            content = fixSpuriousQuotes(content);

            AiTaskSuggestion[] suggestions = objectMapper.readValue(content, AiTaskSuggestion[].class);
            return Arrays.asList(suggestions);

        } catch (Exception e) {
            log.error("Ollama task suggestion failed for goal {}: {}", goal.getGoalId(), e.getMessage());
            return Collections.emptyList();
        }
    }

    private String buildPrompt(Goal goal) {
        return "You are a productivity coach helping someone achieve their goal.\n\n"
                + "Goal: " + goal.getTitle() + "\n"
                + "Description: " + (goal.getDescription() != null ? goal.getDescription() : "") + "\n"
                + "Target Date: " + (goal.getTargetDate() != null ? goal.getTargetDate() : "not set") + "\n"
                + "Success Criteria: " + (goal.getSuccessCriteria() != null ? goal.getSuccessCriteria() : "") + "\n\n"
                + "Generate 5-7 specific, actionable tasks to accomplish this goal.\n"
                + "Return ONLY a valid JSON array with no other text. Each element must have exactly these fields:\n"
                + "- \"title\": string under 150 chars starting with an action verb\n"
                + "- \"description\": string, 1-2 sentences\n"
                + "- \"estimatedMinutes\": integer\n"
                + "- \"priority\": one of \"HIGH\", \"MEDIUM\", \"LOW\"\n"
                + "- \"definitionOfDone\": string describing how you'll know it's complete\n\n"
                + "/no_think";
    }

    // qwen3 sometimes emits `" "fieldName"` instead of `"fieldName"` — strip the spurious `" "` prefix
    private String fixSpuriousQuotes(String content) {
        return content.replaceAll("\"\\s+\"(?=[a-zA-Z])", "\"");
    }

    private String stripThinking(String content) {
        return content.replaceAll("(?s)<think>.*?</think>", "").trim();
    }

    private String stripCodeFences(String content) {
        return content.replaceAll("(?m)^```[a-z]*\\s*$", "").replaceAll("(?m)^```\\s*$", "").trim();
    }
}
