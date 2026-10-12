package org.kidscircle.coach;

import org.kidscircle.coach.db.GoalService;
import org.kidscircle.coach.db.MilestoneService;
import org.kidscircle.coach.db.TaskService;
import org.kidscircle.coach.model.Goal;
import org.kidscircle.coach.model.Milestone;
import org.kidscircle.coach.model.Task;
import org.kidscircle.coach.model.User;
import org.kidscircle.coach.service.AiMilestoneSuggestion;
import org.kidscircle.coach.service.AiTaskSuggestion;
import org.kidscircle.coach.service.OllamaService;
import org.kidscircle.coach.web.BaseController;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Controller
public class AiSuggestController extends BaseController {

    private final GoalService goalService;
    private final MilestoneService milestoneService;
    private final TaskService taskService;
    private final OllamaService ollamaService;

    public AiSuggestController(GoalService goalService, MilestoneService milestoneService,
                               TaskService taskService, OllamaService ollamaService) {
        this.goalService = goalService;
        this.milestoneService = milestoneService;
        this.taskService = taskService;
        this.ollamaService = ollamaService;
    }

    // ─── Step 1: AI suggests milestones for the goal ──────────────

    @GetMapping("/goal/{goalId}/suggest-milestones")
    public String suggestMilestones(@PathVariable Long goalId, Model model,
                                    Principal principal, RedirectAttributes ra) {
        User user = getCurrentUser(principal);
        Goal goal = goalService.getGoalById(goalId);
        assertOwnership(goal.getUserId(), principal);

        List<AiMilestoneSuggestion> suggestions = ollamaService.suggestMilestones(goal);

        if (suggestions.isEmpty()) {
            ra.addFlashAttribute("warning", "AI could not generate milestone suggestions. Add milestones manually.");
            return "redirect:/goal/" + goalId;
        }

        model.addAttribute("user", user);
        model.addAttribute("goal", goal);
        model.addAttribute("suggestions", suggestions);
        model.addAttribute("count", suggestions.size());
        return "suggest_milestones";
    }

    // ─── Suggest tasks for a single already-saved milestone ──────
    // Reuses the same review template/accept endpoint as the multi-milestone
    // flow above, just scoped to one milestone — lets you (re)generate AI
    // tasks for a milestone anytime, not only right after approving it.

    @GetMapping("/milestone/{milestoneId}/suggest-tasks")
    public String suggestTasksForOneMilestone(@PathVariable Long milestoneId, Model model,
                                              Principal principal, RedirectAttributes ra) {
        User user = getCurrentUser(principal);
        Milestone milestone = milestoneService.getMilestoneById(milestoneId);
        Goal goal = goalService.getGoalById(milestone.getGoalId());
        assertOwnership(goal.getUserId(), principal);

        List<AiTaskSuggestion> tasks = ollamaService.suggestTasksForMilestone(goal, milestone);
        if (tasks.isEmpty()) {
            ra.addFlashAttribute("warning", "AI could not generate task suggestions for this milestone. Add tasks manually.");
            return "redirect:/goal/" + goal.getGoalId();
        }

        for (int i = 0; i < tasks.size(); i++) {
            AiTaskSuggestion t = tasks.get(i);
            t.setMilestoneId(milestone.getMilestoneId());
            t.setMilestoneTitle(milestone.getTitle());
            t.setGlobalIndex(i);
        }

        model.addAttribute("user", user);
        model.addAttribute("goal", goal);
        model.addAttribute("milestones", List.of(milestone));
        model.addAttribute("suggestionsByMilestone", Map.of(milestone.getMilestoneId(), tasks));
        model.addAttribute("count", tasks.size());
        return "suggest_milestone_tasks";
    }

    // ─── Step 2: save approved milestones, then AI suggests tasks for each ──

    @PostMapping("/goal/{goalId}/accept-milestones")
    public String acceptMilestones(@PathVariable Long goalId,
                                   @RequestParam(value = "include", required = false) List<String> include,
                                   @RequestParam Map<String, String> allParams,
                                   Model model, Principal principal, RedirectAttributes ra) {
        User user = getCurrentUser(principal);
        Goal goal = goalService.getGoalById(goalId);
        assertOwnership(goal.getUserId(), principal);

        int count = Integer.parseInt(allParams.getOrDefault("count", "0"));
        List<Milestone> saved = new ArrayList<>();

        for (int i = 0; i < count; i++) {
            if (include == null || !include.contains(String.valueOf(i))) continue;

            String title = allParams.getOrDefault("title_" + i, "").trim();
            String targetDateStr = allParams.get("targetDate_" + i);
            if (title.isEmpty() || targetDateStr == null || targetDateStr.isBlank()) continue;

            Milestone milestone = new Milestone();
            milestone.setGoalId(goalId);
            milestone.setTitle(title);
            milestone.setDescription(allParams.get("description_" + i));
            milestone.setSuccessCriteria(allParams.get("successCriteria_" + i));
            milestone.setTargetDate(LocalDate.parse(targetDateStr));
            milestoneService.saveMilestone(milestone);
            saved.add(milestone);
        }

        if (saved.isEmpty()) {
            ra.addFlashAttribute("warning", "No milestones were saved.");
            return "redirect:/goal/" + goalId;
        }

        // Generate each milestone's tasks concurrently — sequential Ollama calls
        // would multiply the ~10-15s-per-call wait by the number of milestones.
        List<CompletableFuture<List<AiTaskSuggestion>>> futures = saved.stream()
                .map(m -> CompletableFuture.supplyAsync(() -> ollamaService.suggestTasksForMilestone(goal, m)))
                .collect(Collectors.toList());

        List<AiTaskSuggestion> allSuggestions = new ArrayList<>();
        Map<Long, List<AiTaskSuggestion>> suggestionsByMilestone = new LinkedHashMap<>();
        for (int m = 0; m < saved.size(); m++) {
            Milestone milestone = saved.get(m);
            List<AiTaskSuggestion> tasks = futures.get(m).join();
            for (AiTaskSuggestion t : tasks) {
                t.setMilestoneId(milestone.getMilestoneId());
                t.setMilestoneTitle(milestone.getTitle());
                t.setGlobalIndex(allSuggestions.size());
                allSuggestions.add(t);
            }
            suggestionsByMilestone.put(milestone.getMilestoneId(), tasks);
        }

        if (allSuggestions.isEmpty()) {
            ra.addFlashAttribute("success", saved.size() + " milestone(s) saved. AI could not generate tasks — add them manually.");
            return "redirect:/goal/" + goalId;
        }

        model.addAttribute("user", user);
        model.addAttribute("goal", goal);
        model.addAttribute("milestones", saved);
        model.addAttribute("suggestionsByMilestone", suggestionsByMilestone);
        model.addAttribute("count", allSuggestions.size());
        return "suggest_milestone_tasks";
    }

    // ─── Step 3: save approved tasks, each tied to its milestone ──

    @PostMapping("/goal/{goalId}/accept-milestone-tasks")
    public String acceptMilestoneTasks(@PathVariable Long goalId,
                                       @RequestParam(value = "include", required = false) List<String> include,
                                       @RequestParam Map<String, String> allParams,
                                       Principal principal, RedirectAttributes ra) {
        User user = getCurrentUser(principal);
        Goal goal = goalService.getGoalById(goalId);
        assertOwnership(goal.getUserId(), principal);

        int count = Integer.parseInt(allParams.getOrDefault("count", "0"));
        int saved = 0;

        for (int i = 0; i < count; i++) {
            if (include == null || !include.contains(String.valueOf(i))) continue;

            String title = allParams.getOrDefault("title_" + i, "").trim();
            String milestoneIdStr = allParams.get("milestoneId_" + i);
            if (title.isEmpty() || milestoneIdStr == null) continue;

            Milestone milestone = milestoneService.getMilestoneById(Long.parseLong(milestoneIdStr));
            if (!milestone.getGoalId().equals(goalId)) continue;

            Task task = new Task();
            task.setUserId(user.getUserId());
            task.setGoalId(goalId);
            task.setMilestoneId(milestone.getMilestoneId());
            task.setTitle(title);
            task.setDescription(allParams.get("description_" + i));
            task.setDefinitionOfDone(allParams.get("dod_" + i));
            task.setPriority(allParams.getOrDefault("priority_" + i, "MEDIUM"));
            task.setFrequency(allParams.getOrDefault("frequency_" + i, "ONCE"));

            String times = allParams.get("times_" + i);
            if (times != null && !times.isBlank()) {
                try { task.setTimesPerPeriod(Integer.parseInt(times.trim())); }
                catch (NumberFormatException ignored) {}
            }

            String est = allParams.get("est_" + i);
            if (est != null && !est.isBlank()) {
                try { task.setEstimatedMinutes(Integer.parseInt(est.trim())); }
                catch (NumberFormatException ignored) {}
            }

            taskService.saveTask(task);
            saved++;
        }

        ra.addFlashAttribute("success", saved + " AI-suggested task" + (saved == 1 ? "" : "s") + " added to your goal.");
        return "redirect:/goal/" + goalId;
    }
}
