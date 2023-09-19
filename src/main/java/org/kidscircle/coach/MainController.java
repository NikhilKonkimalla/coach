package org.kidscircle.coach;

import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.kidscircle.coach.db.GoalRepository;
import org.kidscircle.coach.db.GoalService;
import org.kidscircle.coach.db.SurveyRepository;
import org.kidscircle.coach.db.UserRepository;
import org.kidscircle.coach.db.UserService;
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

import com.oracle.tools.packager.Log;

/*
 * Login - user security
 * Database
 * Data model
 * Motivational Questionnaire
 * Outbound email
 * Goals
 * Monthly Goals
 * Weekly goals
 */


@Controller
public class MainController {
	
	private static final Logger logger = LoggerFactory.getLogger(MainController.class);
	 
	@Autowired private UserRepository userRepository;
	@Autowired private SurveyRepository surveyRepository;
	@Autowired private GoalService goalService;

	 
	

	@GetMapping("/greeting")
	public String greeting(@RequestParam(name="name", required=false, defaultValue="World") String name, Model model) {
		model.addAttribute("name", name);
		return "greeting";
	}
	
	//@GetMapping("/login")
	@GetMapping(value = {"/", "/login"})
	public String login() {
  	logger.info("This is an info message");
  	logger.debug("This is a debug message");
  	logger.error("This is an error message");
		return "login";
	}
	
	@GetMapping("/error")
	public String error() {
  	logger.info("This is an info message");
  	logger.debug("This is a debug message");
  	logger.error("This is an error message");
		return "login";
	}


  @PostMapping("/login")
  public String authenticate(@RequestParam("username") String userName,
                             @RequestParam("password") String password,
                             Model model,
                             HttpSession session) {
      try {

      	logger.error("Hello Word" + userName+password);
      	//Get user from db
        User u = userRepository.findUserByUserName(userName);
        if( !u.getPassword().equals(password))
        {
            model.addAttribute("error", true);
            return "login";
        }
      	//Save user in session	
        session.setAttribute("user", u);

      	return "redirect:/goals";
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
      // Process the submitted form data (e.g., save to a database, send an email, etc.)
      System.out.println(user);
      //user.getSurvey().setU
      
      userRepository.save(user);
      //userRepository.
      // Redirect to the form page after processing
      session.setAttribute("user", user);
      return "redirect:/survey";
  }
  
  

  
  
  @GetMapping("/survey")
  public String survey(HttpSession session, Model model) {
	  //See if a survey is done for this user?
	  User user = (User) session.getAttribute("user");
	  Survey s = new Survey();
	  s.setUserId(user.getUserId());
	  model.addAttribute("survey", s);
      return "survey";
  }
  
  @PostMapping("/survey-submit")
  public String submitSurvey(HttpSession session, Model model, @ModelAttribute Survey s) {
	  logger.info(s.getDrive());
	  User user = (User) session.getAttribute("user");
	  s.setUserId(user.getUserId());
	  surveyRepository.save(s);
	  return "redirect:/goals";
  }
  
  @GetMapping("/goals")
  public String showGoals(HttpSession session, Model model) {
	  User user = (User) session.getAttribute("user");
	  //Get all goals for this user
	  List<Goal> goals = goalService.getGoalForUser(user.getUserId());
	  if( goals.isEmpty())
	  {
	      goals.add(new Goal("ACT"));
	      goals.add(new Goal("SAT"));
	      goals.add(new Goal("SomeThing Else"));
	  }
	  model.addAttribute("goals", goals); 
	  System.out.println(goals);
      return "goals";
  }
  
  @GetMapping("/showNewGoalForm")
  public String showNewGoalForm(Model model) {
      // create model attribute to bind form data
      Goal goal = new Goal();
      model.addAttribute("goal", goal);
      return "new_goal";
  }
  
  @PostMapping("/saveGoal")
  public String saveGoal(HttpSession session,@ModelAttribute("Goal") Goal goal) {
      // save Goal to database
	  User user = (User) session.getAttribute("user");
	  goal.setUserId(user.getUserId());
      goalService.saveGoal(goal);
      return "redirect:/goals";
  }

  @GetMapping("/showFormForUpdate/{id}")
  public String showFormForUpdate(@PathVariable(value = "id") long id, Model model) {
      // get Goal from the service
      Goal goal = (Goal) goalService.getGoalById(id);
      

      // set Goal as a model attribute to pre-populate the form
      model.addAttribute("goal", goal);
      return "update_Goal";
  }

  @GetMapping("/deleteGoal/{id}")
  public String deleteGoal(@PathVariable(value = "id") long id) {

      // call delete Goal method 
      this.goalService.deleteGoalById(id);
      return "redirect:/goals";
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