package org.kidscircle.coach;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;


@Controller
public class MainController {
	
	private static final Logger logger = LoggerFactory.getLogger(MainController.class);
	

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

          return "redirect:/greeting";
      } catch (Exception e) {
          model.addAttribute("error", true);
          return "login";
      }
  }
  
  @GetMapping("/register")
  public String register() {
      return "register";
  }

}