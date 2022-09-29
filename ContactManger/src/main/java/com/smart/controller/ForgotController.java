package com.smart.controller;

import java.util.Random;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class ForgotController {
	
	Random random = new Random(1000); 

	
	// email id form open handler
	@RequestMapping("/forgot")
	public String openEmailForm()
	{
		return "forgot_email_form";
	}
	
	
	@PostMapping("/send-otp")
	public String sendOTP(@RequestParam("email") String email)
	{
		System.out.println("EMAIL " + email);
		
		// generating otp of a digit 
		
		
		int otp = random.nextInt(9999);
		
        System.out.println("OTP " + otp); 
		
        //code for otp sent to email
        
		return "verify_otp";
	}

}
