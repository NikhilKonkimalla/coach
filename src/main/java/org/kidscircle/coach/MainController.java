package org.kidscircle.coach;

import javax.servlet.http.HttpServletRequest;

import org.kidscircle.coach.db.UserService;
import org.kidscircle.coach.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

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

          return "redirect:/greeting?name="+username;
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
  public String submitStudentForm(@ModelAttribute User user) {
      // Process the submitted form data (e.g., save to a database, send an email, etc.)
      System.out.println(user);
      userService.saveUser(user);

      // Redirect to the form page after processing
      return "redirect:/greeting?name="+user.getName();
  }

}