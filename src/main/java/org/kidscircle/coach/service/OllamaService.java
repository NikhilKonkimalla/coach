package org.kidscircle.coach.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.kidscircle.coach.model.Goal;
import org.kidscircle.coach.model.Milestone;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
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

    public List<AiMilestoneSuggestion> suggestMilestones(Goal goal) {
        try {
            String content = callOllama(buildMilestonePrompt(goal));
            AiMilestoneSuggestion[] suggestions = objectMapper.readValue(content, AiMilestoneSuggestion[].class);
            return Arrays.asList(suggestions);
        } catch (Exception e) {
            log.error("Ollama milestone suggestion failed for goal {}: {}", goal.getGoalId(), e.getMessage());
            return Collections.emptyList();
        }
    }

    public List<AiTaskSuggestion> suggestTasksForMilestone(Goal goal, Milestone milestone) {
        try {
            String content = callOllama(buildTaskPrompt(goal, milestone));
            AiTaskSuggestion[] suggestions = objectMapper.readValue(content, AiTaskSuggestion[].class);
            return Arrays.asList(suggestions);
        } catch (Exception e) {
            log.error("Ollama task suggestion failed for milestone {}: {}", milestone.getMilestoneId(), e.getMessage());
            return Collections.emptyList();
        }
    }

    private String callOllama(String prompt) {
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

        if (response == null) throw new IllegalStateException("Empty response from Ollama");

        @SuppressWarnings("unchecked")
        Map<String, Object> message = (Map<String, Object>) response.get("message");
        String content = (String) message.get("content");

        content = stripThinking(content);
        content = stripCodeFences(content);
        content = fixSpuriousQuotes(content);
        return content;
    }

    private String buildMilestonePrompt(Goal goal) {
        String deadline = Boolean.TRUE.equals(goal.getLifelong())
                ? "This is a lifelong, open-ended goal with no fixed deadline — space milestones out sensibly (e.g. quarterly) starting from today."
                : "Target Date: " + (goal.getTargetDate() != null ? goal.getTargetDate() : "not set");

        return "You are a productivity coach. Break this goal down into SMART milestones.\n\n"
                + "A milestone must be Specific, Measurable, and Time-bound, and — unlike the goal itself, "
                + "which may partly depend on things outside the person's control (e.g. \"raise funding\") — "
                + "every milestone must be a checkpoint entirely WITHIN the person's own control "
                + "(e.g. \"Contact 5 potential investors\", not \"Get funded\").\n\n"
                + "Goal: " + goal.getTitle() + "\n"
                + "Description: " + (goal.getDescription() != null ? goal.getDescription() : "") + "\n"
                + deadline + "\n"
                + "Today's date: " + LocalDate.now() + "\n\n"
                + "Respond with ONLY a JSON array (no markdown, no explanation). Example format:\n"
                + "[{\"title\":\"Contact 5 potential investors\",\"description\":\"Reach out to angel investors in your network.\","
                + "\"targetDate\":\"2026-10-15\",\"successCriteria\":\"5 people contacted and responses logged.\"}]\n\n"
                + "Rules:\n"
                + "- Output 3 to 6 milestones, in chronological order\n"
                + "- title: a specific, controllable checkpoint under 150 chars — an action/outcome the person "
                + "directly controls, not the goal's ultimate external outcome\n"
                + "- description: string, 1 sentence\n"
                + "- targetDate: ISO date (YYYY-MM-DD), strictly after today"
                + (Boolean.TRUE.equals(goal.getLifelong()) ? "" : ", and on or before the goal's target date")
                + "\n"
                + "- successCriteria: string — how you'll measurably know this milestone is complete\n"
                + "- No trailing commas. No extra fields. No comments.\n\n"
                + "/no_think";
    }

    private String buildTaskPrompt(Goal goal, Milestone milestone) {
        return "You are a productivity coach. Generate concrete tasks that accomplish this milestone.\n\n"
                + "Goal: " + goal.getTitle() + "\n"
                + "Milestone: " + milestone.getTitle() + "\n"
                + "Milestone description: " + (milestone.getDescription() != null ? milestone.getDescription() : "") + "\n"
                + "Milestone success criteria: " + (milestone.getSuccessCriteria() != null ? milestone.getSuccessCriteria() : "") + "\n"
                + "Milestone target date: " + milestone.getTargetDate() + "\n"
                + "Today's date: " + LocalDate.now() + "\n\n"
                + "Respond with ONLY a JSON array (no markdown, no explanation). Example format:\n"
                + "[{\"title\":\"Do X\",\"description\":\"How to do X.\",\"estimatedMinutes\":30,\"priority\":\"HIGH\","
                + "\"frequency\":\"ONCE\",\"timesPerPeriod\":1,\"definitionOfDone\":\"X is done when Y.\"}]\n\n"
                + "Rules:\n"
                + "- Output 3 to 6 items that together accomplish this milestone by its target date\n"
                + "- title: string, starts with action verb, under 150 chars\n"
                + "- description: string, 1-2 sentences\n"
                + "- estimatedMinutes: integer only, per single occurrence\n"
                + "- priority: exactly one of HIGH, MEDIUM, LOW (uppercase)\n"
                + "- frequency: exactly one of ONCE, DAILY, WEEKLY, MONTHLY (uppercase). Use ONCE for a "
                + "single one-off task. Use DAILY/WEEKLY/MONTHLY for a task the person should repeat on "
                + "that cadence until the milestone's target date.\n"
                + "- timesPerPeriod: integer, how many times per frequency period (e.g. frequency=WEEKLY, "
                + "timesPerPeriod=2 means twice a week). Always 1 when frequency is ONCE.\n"
                + "- definitionOfDone: string (never an array)\n"
                + "- No trailing commas. No extra fields. No comments.\n\n"
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
