package org.kidscircle.coach;

import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;

import javax.servlet.http.HttpServletRequest;

import org.kidscircle.coach.db.UserService;
import org.kidscircle.coach.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
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
	 @Autowired private UserService userService;
	

	@GetMapping("/greeting")
	public String greeting(@RequestParam(name="name", required=false, defaultValue="World") String name, Model model) {
		model.addAttribute("name", name);
		return "greeting";
	}
	
	@GetMapping("/login")
	public String login() {
  	logger.info("This is an info message");
  	logger.debug("This is a debug message");
  	logger.error("This is an error message");
		return "login";
	}
	


  @PostMapping("/login")
  public String authenticate(@RequestParam("username") String username,
                             @RequestParam("password") String password,
                             Model model,
                             HttpServletRequest request) {
      try {
      	logger.info("This is an info message");
      	logger.debug("This is a debug message");
      	logger.error("This is an error message");
      	logger.error("Hello Word" + username+password);
      	//Get user from db
      	
      	//Save user in session
      	
      	//If this user has not answered the behavior questioonaire, send to questionnaire fill
      	
      	
      	//If the user has not set goals, redirect to goal setting page
      	
      	//Redirect to current goals page

         //return "redirect:/greeting?name="+username;
      	return "redirect:/goals-survey";
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
  public String submitStudentForm(Model model, @ModelAttribute User user) {
      // Process the submitted form data (e.g., save to a database, send an email, etc.)
      System.out.println(user);
      userService.saveUser(user);
      // Redirect to the form page after processing
      return "redirect:/survey";
  }
  
  
  @GetMapping("/goals-survey")
  public String showGoals(Model model) {
      return "goals-survey";
  }
  
  @SuppressWarnings("unchecked")
  @PostMapping("/submit-goals")
  public String submitGoals(@ModelAttribute("goals") ArrayList<Goal> goals) {
      // Save the survey to the database using the SurveyRepository
	  //Get current user from session
	  //add survey to user
	  //save the user
	  //userService.save(user);
	  //List<Goal> goals = (List<Goal>) model.getAttribute("goals");
	  logger.info("Hello Word" +goals.toString());
      return "redirect:/greeting?name=whatever"; // Redirect to a thank-you page or any other page you prefer
  }
  
  @GetMapping("/survey")
  public String survey(Model model) {
      model.addAttribute("survey", new Survey());
      return "survey";
  }
  
  @PostMapping("/survey-submit")
  public String submitSurvey(Model model, @ModelAttribute Survey s) {
	  logger.info(s.getDrive());
	  return "redirect:/greeting?name=whatever" + s.getDrive();
  }
  
  @ModelAttribute("potentialGoals")
  public List<PotentialGoal> getPotentialGoals()
  {
	  List<PotentialGoal> potentialGoals = new ArrayList<PotentialGoal>();
	  potentialGoals.add(new PotentialGoal("ACT", "This is somehing about how to prep for ACT"));
	  potentialGoals.add(new PotentialGoal("SAT", "This is somehing about how to prep for SAT"));
	  return potentialGoals;
  }
  
  @ModelAttribute("goals")
  public List<Goal> getGoals()
  {
	  List<Goal> goals = new ArrayList<Goal>();
      goals.add(new Goal("ACT"));
      goals.add(new Goal("SAT"));
      goals.add(new Goal("SomeThing Else"));
	  return goals;
  }

}