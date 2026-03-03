package org.kidscircle.coach;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.*;
import java.util.stream.Collectors;
import javax.servlet.http.HttpSession;

import org.kidscircle.coach.db.*;
import org.kidscircle.coach.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;


@Controller
public class MainController {

	private static final Logger logger = LoggerFactory.getLogger(MainController.class);

	@Autowired private UserRepository userRepository;
	@Autowired private SurveyRepository surveyService;
	@Autowired private GoalService goalService;
	@Autowired private TaskService taskService;




	@GetMapping("/greeting")
	public String greeting(@RequestParam(name="name", required=false, defaultValue="World") String name, Model model) {
		model.addAttribute("name", name);
		return "greeting";
	}

	@GetMapping(value = {"/", "/login"})
	public String login() {
  	logger.info("This is an info message");
  	logger.debug("This is a debug message");
  	logger.error("This is an error message");
		return "login";
	}

	@GetMapping("/error")
	public String error() {
		return "login";
	}


  @PostMapping("/login")
  public String authenticate(@RequestParam("username") String userName,
                             @RequestParam("password") String password,
                             Model model,
                             HttpSession session) {
      try {
      	logger.error("Hello Word" + userName+password);
        User u = userRepository.findUserByUserName(userName);
        if( !u.getPassword().equals(password))
        {
            model.addAttribute("error", true);
            return "login";
        }
        session.setAttribute("user", u);
      	return "redirect:/calendar";
      } catch (Exception e) {
          model.addAttribute("error", true);
          return "login";
      }
  }


  @GetMapping("/register")
  public String register(Model model) {
      model.addAttribute("user", new User());
      return "register";
  }

  @PostMapping("/register-submit")
  public String submitStudentForm(HttpSession session, Model model, @ModelAttribute User user) {
      System.out.println(user);
      userRepository.save(user);
      session.setAttribute("user", user);
      return "redirect:/survey";
  }



  @GetMapping("/survey")
  public String survey(HttpSession session, Model model) {
	  User user = (User) session.getAttribute("user");
	  Survey s = surveyService.findSurveyByUserId(user.getUserId());
	  if ( s == null)
		  s = new Survey();
	  s.setUserId(user.getUserId());
	  model.addAttribute("survey", s);
      return "survey";
  }

  @PostMapping("/survey-submit")
  public String submitSurvey(HttpSession session, Model model, @ModelAttribute Survey s) {
	  logger.info(s.getDrive());
	  User user = (User) session.getAttribute("user");
	  s.setUserId(user.getUserId());
	  surveyService.save(s);
	  return "redirect:/calendar";
  }

  @GetMapping("/goals")
  public String showGoals(HttpSession session, Model model) {
	  User user = (User) session.getAttribute("user");
	  List<Goal> goals = goalService.getGoalForUser(user.getUserId());
	  model.addAttribute("goals", goals);

	  // Group tasks by goalId for display
	  List<Task> allTasks = taskService.getTasksForUser(user.getUserId());
	  Map<Long, List<Task>> tasksByGoal = allTasks.stream()
	      .collect(Collectors.groupingBy(Task::getGoalId));
	  model.addAttribute("tasksByGoal", tasksByGoal);
	  model.addAttribute("newTask", new Task());

      return "goals";
  }

  @GetMapping("/showNewGoalForm")
  public String showNewGoalForm(Model model) {
      Goal goal = new Goal();
      model.addAttribute("goal", goal);
      return "new_goal";
  }

  @PostMapping("/saveGoal")
  public String saveGoal(HttpSession session,@ModelAttribute("Goal") Goal goal) {
	  User user = (User) session.getAttribute("user");
	  goal.setUserId(user.getUserId());
      goalService.saveGoal(goal);
      return "redirect:/goals";
  }

  @GetMapping("/showFormForUpdate/{id}")
  public String showFormForUpdate(@PathVariable(value = "id") long id, Model model) {
      Goal goal = (Goal) goalService.getGoalById(id);
      model.addAttribute("goal", goal);
      return "update_goal";
  }

  @GetMapping("/deleteGoal/{id}")
  public String deleteGoal(@PathVariable(value = "id") long id) {
      this.goalService.deleteGoalById(id);
      return "redirect:/goals";
  }

  @PostMapping("/saveTask")
  public String saveTask(HttpSession session, @ModelAttribute Task task) {
	  User user = (User) session.getAttribute("user");
	  task.setUserId(user.getUserId());
	  taskService.saveTask(task);
	  return "redirect:/goals";
  }

  @GetMapping("/deleteTask/{id}")
  public String deleteTask(@PathVariable(value = "id") long id) {
	  taskService.deleteTaskById(id);
	  return "redirect:/goals";
  }

  @GetMapping("/calendar")
  public String showCalendar(HttpSession session, Model model,
          @RequestParam(required = false) Integer year,
          @RequestParam(required = false) Integer month) {

	  User user = (User) session.getAttribute("user");

	  LocalDate now = LocalDate.now();
	  int displayYear  = (year  != null) ? year  : now.getYear();
	  int displayMonth = (month != null) ? month : now.getMonthValue();

	  YearMonth yearMonth = YearMonth.of(displayYear, displayMonth);
	  int daysInMonth   = yearMonth.lengthOfMonth();
	  // DayOfWeek: Monday=1 … Sunday=7. We want Sunday=0 for a Sun-first grid.
	  int firstDayOfWeek = yearMonth.atDay(1).getDayOfWeek().getValue() % 7;

	  // Build a list of weeks; each week is 7 day-numbers (0 = empty padding)
	  List<List<Integer>> weeks = new ArrayList<>();
	  List<Integer> week = new ArrayList<>();
	  for (int i = 0; i < firstDayOfWeek; i++) week.add(0);
	  for (int day = 1; day <= daysInMonth; day++) {
	      week.add(day);
	      if (week.size() == 7) { weeks.add(week); week = new ArrayList<>(); }
	  }
	  while (week.size() < 7 && !week.isEmpty()) week.add(0);
	  if (!week.isEmpty()) weeks.add(week);

	  // Tasks grouped by day-of-month (only matching year/month)
	  List<Task> tasks = taskService.getTasksForUser(user.getUserId());
	  Map<Integer, List<Task>> tasksByDay = new HashMap<>();
	  for (Task t : tasks) {
	      if (t.getDueDate() != null
	          && t.getDueDate().getYear() == displayYear
	          && t.getDueDate().getMonthValue() == displayMonth) {
	          tasksByDay.computeIfAbsent(t.getDueDate().getDayOfMonth(), k -> new ArrayList<>()).add(t);
	      }
	  }

	  // Goal name lookup map
	  List<Goal> goals = goalService.getGoalForUser(user.getUserId());
	  Map<Long, String> goalNames = new HashMap<>();
	  for (Goal g : goals) goalNames.put(g.getGoalId(), g.getGoalName());

	  YearMonth prev = yearMonth.minusMonths(1);
	  YearMonth next = yearMonth.plusMonths(1);

	  model.addAttribute("year",        displayYear);
	  model.addAttribute("month",       displayMonth);
	  model.addAttribute("monthName",   yearMonth.getMonth().getDisplayName(TextStyle.FULL, Locale.ENGLISH));
	  model.addAttribute("weeks",       weeks);
	  model.addAttribute("tasksByDay",  tasksByDay);
	  model.addAttribute("goalNames",   goalNames);
	  model.addAttribute("prevYear",    prev.getYear());
	  model.addAttribute("prevMonth",   prev.getMonthValue());
	  model.addAttribute("nextYear",    next.getYear());
	  model.addAttribute("nextMonth",   next.getMonthValue());
	  model.addAttribute("todayDay",    now.getDayOfMonth());
	  model.addAttribute("todayYear",   now.getYear());
	  model.addAttribute("todayMonth",  now.getMonthValue());

	  return "calendar";
  }

  @GetMapping("/resources")
  public String resources(Model model) {
      return "resources";
  }

  @ModelAttribute("potentialGoals")
  public List<PotentialGoal> getPotentialGoals()
  {
	  List<PotentialGoal> potentialGoals = new ArrayList<PotentialGoal>();
	  potentialGoals.add(new PotentialGoal("ACT", "This is somehing about how to prep for ACT"));
	  potentialGoals.add(new PotentialGoal("SAT", "This is somehing about how to prep for SAT"));
	  return potentialGoals;
  }


}
