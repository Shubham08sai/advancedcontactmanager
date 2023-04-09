package com.smart.controller;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.util.Map;
import java.util.Optional;

import com.razorpay.*;

import javax.servlet.http.HttpSession;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.smart.dao.ContactRepository;
import com.smart.dao.MyOrderRepository;
import com.smart.dao.UserRepository;
import com.smart.entities.Contact;
import com.smart.entities.MyOrder;
import com.smart.entities.User;
import com.smart.helper.Message;

@Controller
@RequestMapping("/user")
public class UserController {

	@Autowired
	private BCryptPasswordEncoder bCryptPasswordEncoder;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private ContactRepository  contactRepository;
	
	@Autowired
	private MyOrderRepository myOrderRepository;

	//method for Adding common data to response
	@ModelAttribute
	public void addCommonData(Model model,Principal principal) {
		String userName = principal.getName();
		System.out.println("USERNAME"+userName);
		//get the user using username(Email)
		User user = userRepository.getUserByUserName(userName);
		System.out.println("User "+user);
		model.addAttribute("user", user);
	}
	//dashboard home
	@RequestMapping("/index")
	public String dashboard(Model m) {
		m.addAttribute("title", "User Dashboard");
		System.out.println("this is user dashbord");
		return "normal/user_dashboard";
	}
	//Add contact form handler
	@GetMapping("/add-contact")
	public String openAddContactForm(Model m){
		m.addAttribute("title","Add Contact");
		m.addAttribute("contact",new Contact());
		System.out.println("this is Add Contact handler");
		return "normal/add_contact_form" ;
	}

	//process add contact form
	@PostMapping("/process-contact")
	public String processContact(@ModelAttribute Contact contact,
			@RequestParam("profileImage") MultipartFile	file,Principal principal ,HttpSession session ) {
		try {
			String name = principal.getName();
			User user = this.userRepository.getUserByUserName(name);
			//processing and uploading file
			if(file.isEmpty()) {
				//file is empty
				System.out.println("file is empty");
				contact.setImage("contact.png");
			}else {
				//update file to folder and update the name to contact
				contact.setImage(file.getOriginalFilename());
				File saveFile = new ClassPathResource("static/img").getFile();
				Path path = Paths.get(saveFile.getAbsolutePath()+File.separator+file.getOriginalFilename());
				Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
				System.out.println("Image is uploaded");

			}

			contact.setUser(user);   
			user.getContacts().add(contact);
			this.userRepository.save(user);
			System.out.println("User"+contact);
			System.out.println("Added to database");
			//message success
			session.setAttribute("message", new Message("Your Contact is Added", "success"));
		} catch (Exception e) {
			System.out.println("ERROR"+e);
			e.printStackTrace();
			//error message
			session.setAttribute("message", new Message("Something Went Wrong", "danger"));

		}
		return "normal/add_contact_form";

	}
	//show contacts handler
	//per 5 contact
	//current page=0[page]
	@GetMapping("/show-contacts/{page}")
	public String showContacts(@PathVariable("page") Integer page, Model m,Principal principal) {
		String userName = principal.getName();
		m.addAttribute("title", "show user contacts");
		User user = this.userRepository.getUserByUserName(userName);
		//current page -page
		//contact per page-5
		org.springframework.data.domain.Pageable pageable = PageRequest.of(page, 3);

		//List<Contact> contacts = user.getContacts();
		//Contacts ki list ko  bhejma hai
		Page<Contact> Contacts = this.contactRepository.findContactsByUser(user.getId(),(org.springframework.data.domain.Pageable)pageable);
		m.addAttribute("contacts", Contacts);
		m.addAttribute("currentPage", page);
		m.addAttribute("totalPages", Contacts.getTotalPages());
		System.out.println("this is show contacts handler");
		return "normal/show_contacts";
	}
	//showing specific contact detail
	@RequestMapping("/{cId}/contact")
	public String showContactDetail(@PathVariable ("cId")Integer cid,Model m,Principal principal) {
		Optional<Contact> contactOptional = this.contactRepository.findById(cid);
		Contact contact = contactOptional.get();

		String userName = principal.getName();
		User user = this.userRepository.getUserByUserName(userName);
		if(user.getId()==contact.getUser().getId()) {
			m.addAttribute("contact", contact);
			m.addAttribute("title", contact.getName());
		} 		
		System.out.println("CID "+cid);
		return "normal/contact_detail";
	}
	//delete Contact handler
	@GetMapping("/delete/{cId}")
	public String deleteContact(@PathVariable("cId")Integer cId,HttpSession session,Principal principal) {
		Contact contact = this.contactRepository.findById(cId).get();
		//contact.setUser(null);
		//check Assignment
		User user = this.userRepository.getUserByUserName(principal.getName());
		user.getContacts().remove(contact);
		this.userRepository.save(user);
		//remove 
		//img
		//contact.getImage()
		this.contactRepository.delete(contact);
		session.setAttribute("message", new Message("Contact deleted successfully","success" ));
		return "redirect:/user/show-contacts/0";

	}
	//open update form handler
	@PostMapping("/update-contact/{cid}")
	public String updateForm(@PathVariable("cid")  Integer cid ,Model m){
		m.addAttribute("title", "Update your form");
		Contact contact = this.contactRepository.findById(cid).get();		 
		m.addAttribute("contact", contact); 
		return "normal/update_form";
	}
	//update contact handler
	@PostMapping("/process-update")
	public String updateHandler(@ModelAttribute Contact contact,@RequestParam("profileImage")MultipartFile file,
			Model m,HttpSession session,Principal principal) {
		try {
			//old contact details
			Contact oldContactDetail = this.contactRepository.findById(contact.getcId()).get();
			if(!file.isEmpty()) {
				//file work rewrite
				//delete old photo
				File deleteFile = new ClassPathResource("static/img").getFile();
				File file1=new File(deleteFile, oldContactDetail.getImage());
				file1.delete();

				//update new photo
				File saveFile = new ClassPathResource("static/img").getFile();
				Path path = Paths.get(saveFile.getAbsolutePath()+File.separator+file.getOriginalFilename());
				Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
				contact.setImage(file.getOriginalFilename());				
			}else{
				contact.setImage(oldContactDetail.getImage());
			}
			User user = this.userRepository.getUserByUserName(principal.getName());
			contact.setUser(user);
			this.contactRepository.save(contact);

			session.setAttribute("message", new Message("Your Contact is updated", "success"));

		}catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("Contact Name " + contact.getName());
		System.out.println("Contact ID " + contact.getcId());
		return "redirect:/user/"+contact.getcId()+"/contact";
	}
	//your profile handler
	@RequestMapping("/profile")
	public String yourProfile(Model model) {
		model.addAttribute("title","Your Profile");
		System.out.println("this your profile handler");
		return "normal/profile";
	}

	//open setting handler

	@RequestMapping("/settings")
	public String openSettings(Model m) {
		m.addAttribute("title","settings");
		System.out.println("this your setting handler");
		return "normal/settings";
	}
	//change password  handler
	@PostMapping("/change-password")
	public String changePassword(@RequestParam ("oldPassword") String oldPassword,@RequestParam("newPassword") String newPassword,Principal principal,HttpSession session)  {
		System.out.println("Old Password " + oldPassword);
		System.out.println("New Password " + newPassword);
		String userName = principal.getName();
		User currentUser = this.userRepository.getUserByUserName(userName);
		System.out.println("CurrentUserPassword "+currentUser.getPassword());
		if(this.bCryptPasswordEncoder.matches(oldPassword,currentUser.getPassword() )) {
			//change the password
			currentUser.setPassword(bCryptPasswordEncoder.encode(newPassword));
			this.userRepository.save(currentUser);
			session.setAttribute("message",new Message("Your Password is Successfully Changed","success" ));
		}else {
			//error
			session.setAttribute("message",new Message("WRONG PASSWORD!!! please Enter correct password","danger" ));
			return "redirect:/user/settings" ;

		}

		return "redirect:/user/index" ;
	}
	//creating order for payment
	@PostMapping("/create_order")
	@ResponseBody
	public String createOrder(@RequestBody Map<String, Object> data,Principal principal) throws RazorpayException {
		//System.out.println("Hey order function is Executed");
		System.out.println(data);
		int amt = Integer.parseInt(data.get("amount").toString());
		
		var client = new RazorpayClient("rzp_test_aY0jnnG05j4MrR","SA71jHcDvvDjE8GE4563sr94");
		
		JSONObject obj=new JSONObject();
		obj.put("amount",amt*100);
		obj.put("currency", "INR");
		obj.put("receipt", "txn_23565455");
		
		//creating new order
		Order order = client.orders.create(obj);
		System.out.println("order" +order);
		
		//Order save in Database
		MyOrder myOrder = new MyOrder();
		myOrder.setAmount(order.get("amount")+"");
		myOrder.setOrderId(order.get("id"));
		myOrder.setPaymentId(null);
		myOrder.setReceipt(order.get("receipt"));
		myOrder.setUser(this.userRepository.getUserByUserName(principal.getName()));
		myOrder.setStatus("created");
		
		this.myOrderRepository.save(myOrder);
		System.out.println("save to database"+ myOrder);
		return order.toString();
	}
     
	@PostMapping("/update_order")
	public ResponseEntity<?> updateOrder(@RequestBody Map<String, Object>data){
		MyOrder myOrder = this.myOrderRepository.findByOrderId(data.get("order_id").toString());
		myOrder.setPaymentId(data.get("payment_id").toString());
		myOrder.setStatus(data.get("status").toString());
		this.myOrderRepository.save(myOrder);
		System.out.println("data" +data);
		return ResponseEntity.ok(Map.of("msg","updated"));
		
	}
}
