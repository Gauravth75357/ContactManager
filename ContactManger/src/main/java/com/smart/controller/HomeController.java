package com.smart.controller;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.smart.dao.UserRepository;
import com.smart.entities.User;
import com.smart.helper.Message;

@Controller
public class HomeController {
	
	@Autowired
	private BCryptPasswordEncoder passwordEncoder;
	
	

	@Autowired
	private UserRepository userRepository;
	
	@RequestMapping("/")
	public String home(Model model)
	
	{
		model.addAttribute("title","Home - Contact Manager");
		return "home";
	}
	
	
	
     @RequestMapping("/About")
	
	public String about(Model model)
	
	{
		model.addAttribute("title","About - Contact Manager");
		return "About";
	}
     
     
     
     
     @RequestMapping("/Signup")
 	
 	public String Signup(Model model)
 	
 	{
 		model.addAttribute("title","Register - Contact Manager");
 		model.addAttribute("user",new User());
 		return "Signup";
 	}
	
	// handler for registering user
     @RequestMapping(value = "/do_register", method =RequestMethod.POST)
     public String registerUser(@Valid @ModelAttribute("user") User user, BindingResult result1, @RequestParam(value = "agreement", defaultValue = "false")boolean agreement,
    		 Model model, HttpSession session) 
     {
    	 
    	try {
    		
    		if(!agreement)
       	 {
       		 System.out.println("You have not agreed to the terms and conditions");
       		 throw new Exception("You have not agreed to the terms and conditions");
       		 
       	 }

    		 
    	 if	(result1.hasErrors())
    	 {
    		 System.out.println("ERROR" + result1.toString());
    		 model.addAttribute("user", user);
    		 return "Signup";
    	 }
    		
       	 user.setRole("ROLE_USER");
       	 user.setEnabled(true);
       	 user.setImageUrl("profile.png");
       	 user.setPassword(passwordEncoder.encode(user.getPassword()));
       	 
       	 
       	 
       	 System.out.println("Agreement " + agreement );
       	 System.out.println("USER"+ user);
       	 
       	 
          User result = this.userRepository.save(user);
       	 
       	 model.addAttribute("user",new User());
       	 session.setAttribute("message", new Message("Succesfully Registered", "alert-success"));
       	 return "Signup";
			
		} catch (Exception e) {
			
			e.printStackTrace();
			model.addAttribute("user",user);
			session.setAttribute("message", new Message("Something went wrong !!" +  e.getMessage(),"alert-danger"));
	  
			 return "Signup";
		
		}
    	 
    		
     }
	
     
     // handler for login 
     @GetMapping("/Signin")
     public String customLogin(Model model)
     {
    	 model.addAttribute("title", "Login Page");
    	 return  "login";
     }
     
     
     
     
     
     
}
