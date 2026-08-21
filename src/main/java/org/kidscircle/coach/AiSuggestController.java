package org.kidscircle.coach;

import org.kidscircle.coach.db.GoalService;
import org.kidscircle.coach.db.TaskService;
import org.kidscircle.coach.model.Goal;
import org.kidscircle.coach.model.Task;
import org.kidscircle.coach.model.User;
import org.kidscircle.coach.service.AiTaskSuggestion;
import org.kidscircle.coach.service.OllamaService;
import org.kidscircle.coach.web.BaseController;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.List;
import java.util.Map;

@Controller
public class AiSuggestController extends BaseController {

    private final GoalService goalService;
    private final TaskService taskService;
    private final OllamaService ollamaService;

    public AiSuggestController(GoalService goalService, TaskService taskService, OllamaService ollamaService) {
        this.goalService = goalService;
        this.taskService = taskService;
        this.ollamaService = ollamaService;
    }

    @GetMapping("/goal/{goalId}/suggest-tasks")
    public String suggestTasks(@PathVariable Long goalId, Model model,
                               Principal principal, RedirectAttributes ra) {
        User user = getCurrentUser(principal);
        Goal goal = goalService.getGoalById(goalId);
        assertOwnership(goal.getUserId(), principal);

        List<AiTaskSuggestion> suggestions = ollamaService.suggestTasks(goal);

        if (suggestions.isEmpty()) {
            ra.addFlashAttribute("warning", "AI could not generate task suggestions. Add tasks manually.");
            return "redirect:/goal/" + goalId;
        }

        model.addAttribute("user", user);
        model.addAttribute("goal", goal);
        model.addAttribute("suggestions", suggestions);
        model.addAttribute("count", suggestions.size());
        return "suggest_tasks";
    }

    @PostMapping("/goal/{goalId}/accept-tasks")
    public String acceptTasks(@PathVariable Long goalId,
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
            if (title.isEmpty()) continue;

            Task task = new Task();
            task.setUserId(user.getUserId());
            task.setGoalId(goalId);
            task.setTitle(title);
            task.setDescription(allParams.get("description_" + i));
            task.setDefinitionOfDone(allParams.get("dod_" + i));
            task.setPriority(allParams.getOrDefault("priority_" + i, "MEDIUM"));
            task.setFrequency(allParams.getOrDefault("frequency_" + i, "ONCE"));

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
