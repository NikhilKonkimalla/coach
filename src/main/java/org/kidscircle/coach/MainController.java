package org.kidscircle.coach;

import java.security.Principal;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;
import javax.validation.Valid;

import org.kidscircle.coach.db.*;
import org.kidscircle.coach.model.*;
import org.kidscircle.coach.service.FeasibilityResult;
import org.kidscircle.coach.service.FeasibilityService;
import org.kidscircle.coach.service.MilestoneTimelineResult;
import org.kidscircle.coach.service.MilestoneTimelineService;
import org.kidscircle.coach.service.NextActionService;
import org.kidscircle.coach.web.BaseController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class MainController extends BaseController {

    private static final Logger logger = LoggerFactory.getLogger(MainController.class);

    @Autowired private SurveyRepository surveyRepository;
    @Autowired private GoalService goalService;
    @Autowired private TaskService taskService;
    @Autowired private MilestoneService milestoneService;
    @Autowired private FeasibilityService feasibilityService;
    @Autowired private MilestoneTimelineService milestoneTimelineService;
    @Autowired private NextActionService nextActionService;
    @Autowired private PasswordEncoder passwordEncoder;

    // ─── Public pages ───────────────────────────────────────────

    @GetMapping(value = {"/", "/login"})
    public String loginPage() {
        return "login";
    }

    @GetMapping("/register")
    public String registerPage(Model model) {
        model.addAttribute("user", new User());
        return "register";
    }

    @PostMapping("/register-submit")
    public String submitRegister(@ModelAttribute User user,
                                 RedirectAttributes ra) {
        if (userRepository.findByEmail(user.getEmail()) != null) {
            ra.addFlashAttribute("error", "An account with that email already exists.");
            return "redirect:/register";
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        if (user.getDisplayName() == null || user.getDisplayName().isBlank()) {
            user.setDisplayName(user.getName() != null ? user.getName() : user.getEmail());
        }
        userRepository.save(user);
        ra.addFlashAttribute("success", "Account created! Please log in.");
        return "redirect:/login";
    }

    // ─── Survey (legacy) ────────────────────────────────────────

    @GetMapping("/survey")
    public String survey(Principal principal, Model model) {
        User user = getCurrentUser(principal);
        Survey s = surveyRepository.findSurveyByUserId(user.getUserId());
        if (s == null) s = new Survey();
        s.setUserId(user.getUserId());
        model.addAttribute("survey", s);
        return "survey";
    }

    @PostMapping("/survey-submit")
    public String submitSurvey(Principal principal, @ModelAttribute Survey s,
                               RedirectAttributes ra) {
        User user = getCurrentUser(principal);
        s.setUserId(user.getUserId());
        surveyRepository.save(s);
        ra.addFlashAttribute("success", "Your profile has been saved.");
        return "redirect:/survey";
    }

    // ─── Goals ──────────────────────────────────────────────────

    @GetMapping("/goals")
    public String showGoals(Principal principal, Model model) {
        User user = getCurrentUser(principal);
        List<Goal> goals = goalService.getGoalForUser(user.getUserId());
        model.addAttribute("goals", goals);
        List<Task> allTasks = taskService.getTasksForUser(user.getUserId());
        Map<Long, List<Task>> tasksByGoal = allTasks.stream()
                .collect(Collectors.groupingBy(Task::getGoalId));
        model.addAttribute("tasksByGoal", tasksByGoal);
        model.addAttribute("newTask", new Task());
        return "goals";
    }

    @GetMapping("/showNewGoalForm")
    public String showNewGoalForm(Model model) {
        model.addAttribute("goal", new Goal());
        return "new_goal";
    }

    @PostMapping("/saveGoal")
    public String saveGoal(Principal principal,
                           @ModelAttribute("goal") Goal goal,
                           RedirectAttributes ra) {
        if (!hasTargetDateOrIsLifelong(goal, ra)) {
            return "redirect:/showNewGoalForm";
        }
        User user = getCurrentUser(principal);
        goal.setUserId(user.getUserId());
        if (goal.getStatus() == null) goal.setStatus("ACTIVE");
        if (Boolean.TRUE.equals(goal.getLifelong())) goal.setTargetDate(null);
        goalService.saveGoal(goal);
        return "redirect:/goal/" + goal.getGoalId() + "/suggest-milestones";
    }

    private boolean hasTargetDateOrIsLifelong(Goal goal, RedirectAttributes ra) {
        if (!Boolean.TRUE.equals(goal.getLifelong()) && goal.getTargetDate() == null) {
            ra.addFlashAttribute("error", "Set a target date, or mark this goal as lifelong.");
            return false;
        }
        return true;
    }

    @GetMapping("/showFormForUpdate/{id}")
    public String showFormForUpdate(@PathVariable long id, Principal principal, Model model) {
        Goal goal = goalService.getGoalById(id);
        assertOwnership(goal.getUserId(), principal);
        model.addAttribute("goal", goal);
        return "update_goal";
    }

    @PostMapping("/updateGoal")
    public String updateGoal(@ModelAttribute("goal") Goal goal,
                             Principal principal,
                             RedirectAttributes ra) {
        Goal existing = goalService.getGoalById(goal.getGoalId());
        assertOwnership(existing.getUserId(), principal);
        if (!hasTargetDateOrIsLifelong(goal, ra)) {
            return "redirect:/showFormForUpdate/" + goal.getGoalId();
        }
        existing.setTitle(goal.getTitle());
        existing.setDescription(goal.getDescription());
        existing.setLifelong(Boolean.TRUE.equals(goal.getLifelong()));
        existing.setTargetDate(Boolean.TRUE.equals(goal.getLifelong()) ? null : goal.getTargetDate());
        existing.setWeeklyCapacityMinutes(goal.getWeeklyCapacityMinutes());
        existing.setPriority(goal.getPriority());
        existing.setSuccessCriteria(goal.getSuccessCriteria());
        existing.setMotivation(goal.getMotivation());
        existing.setStatus(goal.getStatus() != null ? goal.getStatus() : existing.getStatus());
        goalService.saveGoal(existing);
        ra.addFlashAttribute("success", "Goal updated.");
        return "redirect:/goals";
    }

    @PostMapping("/deleteGoal/{id}")
    public String deleteGoal(@PathVariable long id, Principal principal, RedirectAttributes ra) {
        Goal goal = goalService.getGoalById(id);
        assertOwnership(goal.getUserId(), principal);
        goalService.deleteGoalById(id);
        ra.addFlashAttribute("success", "Goal deleted.");
        return "redirect:/goals";
    }

    @GetMapping("/goal/{id}")
    public String goalDetail(@PathVariable long id, Principal principal, Model model) {
        Goal goal = goalService.getGoalById(id);
        assertOwnership(goal.getUserId(), principal);
        User user = getCurrentUser(principal);
        List<Milestone> milestones = milestoneService.getMilestonesForGoal(id);
        List<Task> tasks = taskService.getTopLevelTasksForGoal(id);
        Map<Long, List<Task>> tasksByMilestone = tasks.stream()
                .collect(Collectors.groupingBy(Task::getMilestoneId));
        FeasibilityResult feasibility = feasibilityService.calculate(goal);
        MilestoneTimelineResult timeline = milestoneTimelineService.build(goal, milestones);
        int progress = goalService.calculateProgressPercent(id);
        model.addAttribute("goal", goal);
        model.addAttribute("milestones", milestones);
        model.addAttribute("tasksByMilestone", tasksByMilestone);
        model.addAttribute("feasibility", feasibility);
        model.addAttribute("timeline", timeline);
        model.addAttribute("progress", progress);
        model.addAttribute("newTask", new Task());
        model.addAttribute("newMilestone", new Milestone());
        return "goal_detail";
    }

    @PostMapping("/goal/{id}/status")
    public String updateGoalStatus(@PathVariable long id,
                                   @RequestParam String status,
                                   Principal principal,
                                   RedirectAttributes ra) {
        Goal goal = goalService.getGoalById(id);
        assertOwnership(goal.getUserId(), principal);
        goalService.updateStatus(id, status);
        ra.addFlashAttribute("success", "Goal status updated to " + status + ".");
        return "redirect:/goal/" + id;
    }

    // ─── Milestones ─────────────────────────────────────────────

    @PostMapping("/goal/{goalId}/milestone/save")
    public String saveMilestone(@PathVariable long goalId,
                                @ModelAttribute Milestone milestone,
                                Principal principal,
                                RedirectAttributes ra) {
        Goal goal = goalService.getGoalById(goalId);
        assertOwnership(goal.getUserId(), principal);
        if (!isSmart(milestone, ra)) {
            return "redirect:/goal/" + goalId;
        }
        milestone.setGoalId(goalId);
        milestoneService.saveMilestone(milestone);
        ra.addFlashAttribute("success", "Milestone saved.");
        return "redirect:/goal/" + goalId;
    }

    @PostMapping("/milestone/{id}/save")
    public String updateMilestone(@PathVariable long id,
                                  @ModelAttribute Milestone formMilestone,
                                  Principal principal,
                                  RedirectAttributes ra) {
        Milestone milestone = milestoneService.getMilestoneById(id);
        Goal goal = goalService.getGoalById(milestone.getGoalId());
        assertOwnership(goal.getUserId(), principal);
        if (!isSmart(formMilestone, ra)) {
            return "redirect:/goal/" + goal.getGoalId();
        }
        milestone.setTitle(formMilestone.getTitle());
        milestone.setDescription(formMilestone.getDescription());
        milestone.setTargetDate(formMilestone.getTargetDate());
        milestone.setSuccessCriteria(formMilestone.getSuccessCriteria());
        if (formMilestone.getStatus() != null) milestone.setStatus(formMilestone.getStatus());
        milestoneService.saveMilestone(milestone);
        ra.addFlashAttribute("success", "Milestone updated.");
        return "redirect:/goal/" + goal.getGoalId();
    }

    private boolean isSmart(Milestone milestone, RedirectAttributes ra) {
        if (milestone.getTargetDate() == null) {
            ra.addFlashAttribute("error", "Milestones must be SMART — set a target date.");
            return false;
        }
        if (milestone.getSuccessCriteria() == null || milestone.getSuccessCriteria().isBlank()) {
            ra.addFlashAttribute("error", "Milestones must be SMART — describe how you'll measure it done.");
            return false;
        }
        return true;
    }

    @PostMapping("/milestone/delete/{id}")
    public String deleteMilestone(@PathVariable long id, Principal principal, RedirectAttributes ra) {
        Milestone m = milestoneService.getMilestoneById(id);
        Goal goal = goalService.getGoalById(m.getGoalId());
        assertOwnership(goal.getUserId(), principal);
        milestoneService.deleteMilestoneById(id);
        ra.addFlashAttribute("success", "Milestone deleted.");
        return "redirect:/goal/" + m.getGoalId();
    }

    // ─── Tasks ──────────────────────────────────────────────────

    @PostMapping("/saveTask")
    public String saveTask(Principal principal,
                           @ModelAttribute Task task,
                           @RequestParam long goalId,
                           RedirectAttributes ra) {
        Goal goal = goalService.getGoalById(goalId);
        assertOwnership(goal.getUserId(), principal);
        if (task.getMilestoneId() == null) {
            ra.addFlashAttribute("error", "Every task belongs to a milestone — pick one.");
            return "redirect:/goal/" + goalId;
        }
        Milestone milestone = milestoneService.getMilestoneById(task.getMilestoneId());
        if (!milestone.getGoalId().equals(goalId)) {
            throw new SecurityException("Milestone does not belong to this goal");
        }
        User user = getCurrentUser(principal);
        task.setUserId(user.getUserId());
        task.setGoalId(goalId);
        taskService.saveTask(task);
        ra.addFlashAttribute("success", "Task saved.");
        return "redirect:/goal/" + goalId;
    }

    @GetMapping("/task/{id}")
    public String taskDetail(@PathVariable long id, Principal principal, Model model) {
        Task task = taskService.getTaskById(id);
        User user = getCurrentUser(principal);
        if (!task.getUserId().equals(user.getUserId())) return "redirect:/goals";
        Goal goal = goalService.getGoalById(task.getGoalId());
        Milestone milestone = milestoneService.getMilestoneById(task.getMilestoneId());
        List<Task> subtasks = taskService.getTasksForGoal(task.getGoalId()).stream()
                .filter(t -> task.getTaskId().equals(t.getParentTaskId()))
                .collect(Collectors.toList());
        model.addAttribute("task", task);
        model.addAttribute("goal", goal);
        model.addAttribute("milestone", milestone);
        model.addAttribute("subtasks", subtasks);
        return "task_detail";
    }

    @GetMapping("/task/{id}/edit")
    public String taskEdit(@PathVariable long id, Principal principal, Model model) {
        Task task = taskService.getTaskById(id);
        User user = getCurrentUser(principal);
        if (!task.getUserId().equals(user.getUserId())) return "redirect:/goals";
        model.addAttribute("task", task);
        model.addAttribute("milestones", milestoneService.getMilestonesForGoal(task.getGoalId()));
        return "task_edit";
    }

    @PostMapping("/task/{id}/save")
    public String taskSave(@PathVariable long id,
                           @ModelAttribute Task formTask,
                           Principal principal,
                           RedirectAttributes ra) {
        Task task = taskService.getTaskById(id);
        User user = getCurrentUser(principal);
        if (!task.getUserId().equals(user.getUserId())) return "redirect:/goals";
        if (formTask.getMilestoneId() == null) {
            ra.addFlashAttribute("error", "Every task belongs to a milestone — pick one.");
            return "redirect:/task/" + id + "/edit";
        }
        Milestone milestone = milestoneService.getMilestoneById(formTask.getMilestoneId());
        if (!milestone.getGoalId().equals(task.getGoalId())) {
            throw new SecurityException("Milestone does not belong to this task's goal");
        }
        task.setTitle(formTask.getTitle());
        task.setDescription(formTask.getDescription());
        task.setDefinitionOfDone(formTask.getDefinitionOfDone());
        task.setEstimatedMinutes(formTask.getEstimatedMinutes());
        task.setPriority(formTask.getPriority());
        task.setDueDate(formTask.getDueDate());
        task.setSchedulingType(formTask.getSchedulingType());
        task.setFrequency(formTask.getFrequency());
        task.setTimesPerPeriod(formTask.getTimesPerPeriod());
        task.setMilestoneId(formTask.getMilestoneId());
        taskService.saveTask(task);
        ra.addFlashAttribute("success", "Task updated.");
        return "redirect:/task/" + id;
    }

    @PostMapping("/task/{id}/complete")
    public String completeTask(@PathVariable long id,
                               @RequestParam(required = false) Integer actualMinutes,
                               @RequestParam(required = false) String completionNote,
                               Principal principal,
                               RedirectAttributes ra) {
        Task task = taskService.getTaskById(id);
        User user = getCurrentUser(principal);
        if (!task.getUserId().equals(user.getUserId())) return "redirect:/goals";
        taskService.completeTask(id, actualMinutes, completionNote);
        ra.addFlashAttribute("success", "Great work! Task marked complete.");
        return "redirect:/today";
    }

    @PostMapping("/task/{id}/reopen")
    public String reopenTask(@PathVariable long id, Principal principal, RedirectAttributes ra) {
        Task task = taskService.getTaskById(id);
        User user = getCurrentUser(principal);
        if (!task.getUserId().equals(user.getUserId())) return "redirect:/goals";
        taskService.updateStatus(id, "READY");
        ra.addFlashAttribute("success", "Task reopened.");
        return "redirect:/task/" + id;
    }

    @PostMapping("/task/{id}/status")
    public String updateTaskStatus(@PathVariable long id,
                                   @RequestParam String status,
                                   Principal principal,
                                   RedirectAttributes ra) {
        Task task = taskService.getTaskById(id);
        User user = getCurrentUser(principal);
        if (!task.getUserId().equals(user.getUserId())) return "redirect:/goals";
        taskService.updateStatus(id, status);
        ra.addFlashAttribute("success", "Task status updated.");
        String referer = task.getGoalId() != null ? "/goal/" + task.getGoalId() : "/goals";
        return "redirect:" + referer;
    }

    @PostMapping("/deleteTask/{id}")
    public String deleteTask(@PathVariable long id, Principal principal, RedirectAttributes ra) {
        Task task = taskService.getTaskById(id);
        User user = getCurrentUser(principal);
        if (!task.getUserId().equals(user.getUserId())) return "redirect:/goals";
        Long goalId = task.getGoalId();
        taskService.deleteTaskById(id);
        ra.addFlashAttribute("success", "Task deleted.");
        return "redirect:" + (goalId != null ? "/goal/" + goalId : "/goals");
    }


    @GetMapping("/resources")
    public String resources() {
        return "resources";
    }

    @GetMapping("/error")
    public String error() {
        return "login";
    }

    // ─── Helpers ────────────────────────────────────────────────

    @ModelAttribute("potentialGoals")
    public List<PotentialGoal> getPotentialGoals() {
        List<PotentialGoal> list = new ArrayList<>();
        list.add(new PotentialGoal("ACT", "Prepare for the ACT exam"));
        list.add(new PotentialGoal("SAT", "Prepare for the SAT exam"));
        return list;
    }
}
