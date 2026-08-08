package org.kidscircle.coach;

import org.kidscircle.coach.db.GoalService;
import org.kidscircle.coach.db.TaskService;
import org.kidscircle.coach.db.WorkSessionService;
import org.kidscircle.coach.model.Goal;
import org.kidscircle.coach.model.Task;
import org.kidscircle.coach.model.User;
import org.kidscircle.coach.model.WorkSession;
import org.kidscircle.coach.service.NextActionService;
import org.kidscircle.coach.web.BaseController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class TodayController extends BaseController {

    @Autowired private TaskService taskService;
    @Autowired private WorkSessionService workSessionService;
    @Autowired private GoalService goalService;
    @Autowired private NextActionService nextActionService;

    @GetMapping("/today")
    public String today(Principal principal, Model model) {
        User user = getCurrentUser(principal);
        long uid = user.getUserId();
        LocalDate today = LocalDate.now();

        List<Task> todayTasks = taskService.getDueTodayForUser(uid);
        List<WorkSession> sessions = workSessionService.getSessionsForToday(uid);
        NextActionService.Recommendation nextAction = nextActionService.getNextAction(uid);

        // Build goal name lookup for task cards
        Map<Long, String> goalTitleMap = new HashMap<>();
        for (Task t : todayTasks) {
            if (t.getGoalId() != null && !goalTitleMap.containsKey(t.getGoalId())) {
                try {
                    Goal g = goalService.getGoalById(t.getGoalId());
                    goalTitleMap.put(g.getGoalId(), g.getTitle());
                } catch (Exception ignored) {}
            }
        }

        // Overdue tasks (past due, not completed)
        List<Task> overdueTasks = taskService.getReadyTasksForUser(uid).stream()
                .filter(t -> t.getDueDate() != null && t.getDueDate().isBefore(today))
                .collect(java.util.stream.Collectors.toList());

        model.addAttribute("user", user);
        model.addAttribute("today", today);
        model.addAttribute("todayTasks", todayTasks);
        model.addAttribute("sessions", sessions);
        model.addAttribute("nextAction", nextAction);
        model.addAttribute("overdueTasks", overdueTasks);
        model.addAttribute("goalTitles", goalTitleMap);
        return "today";
    }

    @PostMapping("/today/complete-task")
    public String completeTaskFromToday(@RequestParam long taskId,
                                        @RequestParam(required = false) Integer actualMinutes,
                                        @RequestParam(required = false) String note,
                                        Principal principal,
                                        RedirectAttributes ra) {
        Task task = taskService.getTaskById(taskId);
        User user = getCurrentUser(principal);
        if (!task.getUserId().equals(user.getUserId())) return "redirect:/today";
        taskService.completeTask(taskId, actualMinutes, note);
        ra.addFlashAttribute("success", "Task marked complete!");
        return "redirect:/today";
    }

    @PostMapping("/today/defer-task")
    public String deferTask(@RequestParam long taskId,
                            @RequestParam String newDueDate,
                            Principal principal,
                            RedirectAttributes ra) {
        Task task = taskService.getTaskById(taskId);
        User user = getCurrentUser(principal);
        if (!task.getUserId().equals(user.getUserId())) return "redirect:/today";
        task.setDueDate(LocalDate.parse(newDueDate));
        task.setStatus("DEFERRED");
        taskService.saveTask(task);
        ra.addFlashAttribute("success", "Task deferred.");
        return "redirect:/today";
    }
}
