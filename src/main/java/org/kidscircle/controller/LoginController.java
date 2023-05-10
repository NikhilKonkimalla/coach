package org.kidscircle.controller;

import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.security.authentication.AuthenticationManager;
//import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
//import org.springframework.security.core.Authentication;
//import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import javax.servlet.http.HttpServletRequest;

@Controller
public class LoginController {
	
	private static final Logger logger = LoggerFactory.getLogger(LoginController.class);


//    @Autowired
//    private AuthenticationManager authenticationManager;
    
    // Login form
	@GetMapping("/login")
	public String login() {
		return "login";
	}

//    @GetMapping("/login")
//    public String login() {
//    	logger.info("This is an info message");
//    	logger.debug("This is a debug message");
//    	logger.error("This is an error message");
//
//        return "login";
//    }

//    @PostMapping("/login")
//    public String authenticate(@RequestParam("username") String username,
//                               @RequestParam("password") String password,
//                               Model model,
//                               HttpServletRequest request) {
//        try {
//        	logger.info("This is an info message");
//        	logger.debug("This is a debug message");
//        	logger.error("This is an error message");
//        	System.out.println("Hello Word" + username+password);
////            Authentication authentication = authenticationManager.authenticate(
////                    new UsernamePasswordAuthenticationToken(username, password)
////            );
////            SecurityContextHolder.getContext().setAuthentication(authentication);
//            return "redirect:/greeting";
//        } catch (Exception e) {
//            model.addAttribute("error", true);
//            return "login";
//        }
//    }
//    
//    @GetMapping("/register")
//    public String register() {
//        return "register";
//    }

}

