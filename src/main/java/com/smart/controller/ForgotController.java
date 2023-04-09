package com.smart.controller;

import java.util.Random;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.smart.dao.UserRepository;
import com.smart.entities.User;
import com.smart.service.EmailService;

@Controller
public class ForgotController {
    
	@Autowired
	private EmailService emailService;
	
	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private BCryptPasswordEncoder bCryptPasswordEncoder;
	
	//email id  form open handler
    @RequestMapping("/forgot")
	public String openEmailForm() {
		return "forgot_email_form";
	}
    
	@PostMapping("/send-otp")
	public String sendOTP(@RequestParam ("email")String email,HttpSession session) {
		System.out.println("Email : "+email);
		
		//generating 4 digit otp
		Random random = new Random();
		int otp = random.nextInt(9999);
		System.out.println("otp "+otp);
		
		
		//write code to send otp
		String subject="OTP from ADM";
		String message=" "
		     +"<div style='border:1px solid #e2e2e2; padding:20px'>"
			 +"<h1>"
		     +"OTP is "
			 +"<b>"+otp+
		     "</n>"
			 +"</h1>"
		     +"</div>";
		String to= email;
		boolean sendEmail = this.emailService.sendEmail(subject, message, to);
		  if(sendEmail==true) {
			  session.setAttribute("myotp",otp);
			  session.setAttribute("email", email);
				return "verify_otp";
           }else {
			      session.setAttribute("message","Please Enter Correct Email ID ");
				  return "forgot_email_form";
			}
      }
	
	//verify OTP handler
	 @PostMapping("/verify-otp")
	 public String verifyOtp(@RequestParam ("otp") int otp,HttpSession session) {
		 
		 int myOtp=(int) session.getAttribute("myotp");
		 String email=(String) session.getAttribute("email");
		 if(myOtp==otp) {
			 //password for change form
			 User user = this.userRepository.getUserByUserName(email);
			 if(user==null) {
				 //send error message
				   session.setAttribute("message","USER NOT FOUND..with this email !!!");
				   return "forgot_email_form";
				  }else {
				 //send change password form
 
			 }
			 return "password_change_form";

			 
		 }else {
			 session.setAttribute("message", "Please enter Correct OTP");
			 return "verify_otp";
		 }
	  }	
	 
	 //change password
	 @PostMapping("/change-password")
	 public String changePassword(@RequestParam ("newpassword")String newPassword,HttpSession session) {
		 String email=(String) session.getAttribute("email");
         User user = this.userRepository.getUserByUserName(email);
         user.setPassword(this.bCryptPasswordEncoder.encode(newPassword));
         this.userRepository.save(user);
         return "redirect:/signin?change=password changed Successfully...";
		 
	 }
	 
	}

