package org.kidscircle.coach;

import org.kidscircle.coach.db.GoalService;
import org.kidscircle.coach.db.TaskService;
import org.kidscircle.coach.db.WorkSessionService;
import org.kidscircle.coach.model.Goal;
import org.kidscircle.coach.model.Task;
import org.kidscircle.coach.model.User;
import org.kidscircle.coach.service.NextActionService;
import org.kidscircle.coach.web.BaseController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.security.Principal;
import java.time.LocalDate;
import java.util.List;

@Controller
public class DashboardController extends BaseController {

    @Autowired private GoalService goalService;
    @Autowired private TaskService taskService;
    @Autowired private WorkSessionService workSessionService;
    @Autowired private NextActionService nextActionService;

    @GetMapping("/dashboard")
    public String dashboard(Principal principal, Model model) {
        User user = getCurrentUser(principal);
        long uid = user.getUserId();

        List<Goal> activeGoals = goalService.getActiveGoalsForUser(uid);
        List<Task> todayTasks = taskService.getDueTodayForUser(uid);
        List<Task> readyTasks = taskService.getReadyTasksForUser(uid);
        NextActionService.Recommendation nextAction = nextActionService.getNextAction(uid);

        LocalDate now = LocalDate.now();
        LocalDate weekEnd = now.plusDays(6);
        List<Task> weekTasks = taskService.getTasksForWeek(uid, now, weekEnd);

        int weekPlanned = weekTasks.stream()
                .mapToInt(t -> t.getEstimatedMinutes() != null ? t.getEstimatedMinutes() : 0)
                .sum();

        model.addAttribute("user", user);
        model.addAttribute("activeGoals", activeGoals);
        model.addAttribute("todayTasks", todayTasks);
        model.addAttribute("nextAction", nextAction);
        model.addAttribute("weekTaskCount", weekTasks.size());
        model.addAttribute("weekPlannedMinutes", weekPlanned);
        model.addAttribute("today", now);
        return "dashboard";
    }
}
