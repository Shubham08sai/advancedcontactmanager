package com.smart.service;

import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.springframework.stereotype.Service;

@Service
public class EmailService {
	
		public boolean sendEmail(String subject,String message,String to) {
			boolean f=false;
			String from="shubhamsaini08031996@gmail.com";
			//variable for gmail host
			String host="smtp.gmail.com";
					
			//get the system properties
			Properties properties = System.getProperties();
			System.out.println("Properties " +properties);
				
			//etting important information to properties object	
			//host set
			properties.put("mail.smtp.host", host);
			properties.put("mail.smtp.port", "465");
			properties.put("mail.smtp.ssl.trust", "*");
			properties.put("mail.smtp.ssl.enable", "true");
			properties.put("mail.smtp.auth", "true");
			
			//step 1: to get the session object
			Session session = Session.getInstance(properties, new Authenticator() {
				//
				@Override
				protected PasswordAuthentication getPasswordAuthentication() {

					return new PasswordAuthentication("shubhamsaini08031996@gmail.com","uptvbxnkvgbftrvu");
				}
			});
			session.setDebug(true);
			
			//step 2: compose the message[text,multimedia] 
			MimeMessage mMessage = new MimeMessage(session);
			try {
				//from email
				mMessage.setFrom(from);

				//adding recipient to message
				mMessage.addRecipient(Message.RecipientType.TO, new InternetAddress(to));

				//adding subject to message
				mMessage.setSubject(subject);

				//adding text to message
				//mMessage.setText(message);
                  mMessage.setContent(message, "text/html");
				
				
				//send
				//step 3 : send the message using transport class
				Transport.send(mMessage);

				System.out.println("send successfully.....");
				f=true;
			} catch (Exception e) {
				e.printStackTrace();
			}
			return f;
		}
	}

