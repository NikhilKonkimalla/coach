package org.kidscircle.coach;

import org.kidscircle.coach.db.GoalService;
import org.kidscircle.coach.db.TaskService;
import org.kidscircle.coach.db.WorkSessionService;
import org.kidscircle.coach.model.*;
import org.kidscircle.coach.web.BaseController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Controller
public class WeeklyPlanningController extends BaseController {

    @Autowired private TaskService taskService;
    @Autowired private GoalService goalService;
    @Autowired private WorkSessionService workSessionService;

    @GetMapping("/week")
    public String weeklyPlan(Principal principal, Model model,
                             @RequestParam(required = false)
                             @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate weekStart) {
        User user = getCurrentUser(principal);
        long uid = user.getUserId();

        if (weekStart == null) {
            LocalDate today = LocalDate.now();
            weekStart = today.with(DayOfWeek.MONDAY);
            if (today.getDayOfWeek() == DayOfWeek.SUNDAY) weekStart = today;
        }
        LocalDate weekEnd = weekStart.plusDays(6);

        // Tasks due this week
        List<Task> weekTasks = taskService.getTasksForWeek(uid, weekStart, weekEnd);
        // Work sessions this week
        List<WorkSession> sessions = workSessionService.getSessionsForWeek(uid, weekStart, weekEnd);
        // Backlog: ready tasks with no due date or due after weekEnd
        List<Task> backlog = taskService.getReadyTasksForUser(uid).stream()
                .filter(t -> t.getDueDate() == null || t.getDueDate().isAfter(weekEnd))
                .collect(Collectors.toList());

        // Group sessions by day
        Map<LocalDate, List<WorkSession>> sessionsByDay = sessions.stream()
                .collect(Collectors.groupingBy(WorkSession::getScheduledDate));

        // Days of week for display
        List<LocalDate> days = new ArrayList<>();
        for (int i = 0; i < 7; i++) days.add(weekStart.plusDays(i));

        // Group week tasks by day
        Map<LocalDate, List<Task>> tasksByDay = weekTasks.stream()
                .filter(t -> t.getDueDate() != null)
                .collect(Collectors.groupingBy(Task::getDueDate));

        // Weekly capacity stats
        int weeklyCapacity = goalService.getActiveGoalsForUser(uid).stream()
                .mapToInt(g -> g.getWeeklyCapacityMinutes() != null ? g.getWeeklyCapacityMinutes() : 0)
                .sum();
        int scheduledMinutes = sessions.stream()
                .mapToInt(s -> s.getPlannedMinutes() != null ? s.getPlannedMinutes() : 0)
                .sum();

        // Goal title lookup
        Map<Long, String> goalTitles = new HashMap<>();
        for (Task t : weekTasks) {
            if (t.getGoalId() != null && !goalTitles.containsKey(t.getGoalId())) {
                try { goalTitles.put(t.getGoalId(), goalService.getGoalById(t.getGoalId()).getTitle()); }
                catch (Exception ignored) {}
            }
        }
        for (Task t : backlog) {
            if (t.getGoalId() != null && !goalTitles.containsKey(t.getGoalId())) {
                try { goalTitles.put(t.getGoalId(), goalService.getGoalById(t.getGoalId()).getTitle()); }
                catch (Exception ignored) {}
            }
        }

        model.addAttribute("user", user);
        model.addAttribute("weekStart", weekStart);
        model.addAttribute("weekEnd", weekEnd);
        model.addAttribute("days", days);
        model.addAttribute("weekTasks", weekTasks);
        model.addAttribute("tasksByDay", tasksByDay);
        model.addAttribute("backlog", backlog);
        model.addAttribute("sessions", sessions);
        model.addAttribute("sessionsByDay", sessionsByDay);
        model.addAttribute("weeklyCapacity", weeklyCapacity);
        model.addAttribute("scheduledMinutes", scheduledMinutes);
        model.addAttribute("remainingMinutes", Math.max(0, weeklyCapacity - scheduledMinutes));
        model.addAttribute("goalTitles", goalTitles);
        model.addAttribute("prevWeek", weekStart.minusWeeks(1));
        model.addAttribute("nextWeek", weekStart.plusWeeks(1));
        return "weekly_planning";
    }

    @PostMapping("/week/schedule-task")
    public String scheduleTask(@RequestParam long taskId,
                               @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate scheduledDate,
                               @RequestParam(required = false) Integer plannedMinutes,
                               Principal principal,
                               RedirectAttributes ra) {
        Task task = taskService.getTaskById(taskId);
        User user = getCurrentUser(principal);
        if (!task.getUserId().equals(user.getUserId())) return "redirect:/week";
        WorkSession session = new WorkSession();
        session.setUserId(user.getUserId());
        session.setGoalId(task.getGoalId());
        session.setTaskId(taskId);
        session.setScheduledDate(scheduledDate);
        session.setPlannedMinutes(plannedMinutes != null ? plannedMinutes : task.getEstimatedMinutes());
        session.setStatus("PLANNED");
        workSessionService.saveSession(session);
        task.setDueDate(scheduledDate);
        if ("READY".equals(task.getStatus())) task.setStatus("SCHEDULED");
        taskService.saveTask(task);
        ra.addFlashAttribute("success", "Task scheduled for " + scheduledDate + ".");
        return "redirect:/week?weekStart=" + scheduledDate.with(DayOfWeek.MONDAY);
    }

    @GetMapping("/week/remove-session/{id}")
    public String removeSession(@PathVariable long id, Principal principal, RedirectAttributes ra) {
        User user = getCurrentUser(principal);
        workSessionService.deleteSession(id);
        ra.addFlashAttribute("success", "Session removed.");
        return "redirect:/week";
    }
}
