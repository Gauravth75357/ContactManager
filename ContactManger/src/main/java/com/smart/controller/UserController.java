package com.smart.controller;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.util.List;
import java.util.Optional;

import javax.servlet.http.HttpSession;
import javax.websocket.Session;

import org.aspectj.bridge.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.smart.dao.ContactRepository;
import com.smart.dao.UserRepository;
import com.smart.entities.Contact;
import com.smart.entities.User;

@Controller
@RequestMapping("/User")
public class UserController {
	
	@Autowired
	private BCryptPasswordEncoder bCryptPasswordEncoder;
 
	@Autowired
	private UserRepository userRepository;
    
	@Autowired
	private ContactRepository contactRepository;

	

	// method for adding common data to response
	@ModelAttribute
	public void  addCommonData(Model model, Principal principal)
	{
		
		String userName= principal.getName();
		System.out.println("USERNAME"+userName);
		
		
		// get the user using username (Email)
		User user = userRepository.getUserByUserName(userName);
		
		System.out.println("USER" + user);

		model.addAttribute("user", user);
		
		
	}
	
	
    
	//home dashboard
	@RequestMapping("/index")
	public String dashboard(Model model,Principal principal)
	{
		model.addAttribute("title","User Dashboard");
		return "normal/user_dashboard";
	}
	

	
	// open add form handler
	@GetMapping("/add-contact")
	public String openAddContactForm(Model model)
	{
		model.addAttribute("title","Add Contact" );
		model.addAttribute("contact", new Contact());
		
		return "normal/add_contact_form";
				
	}
	
	// processing add contact form
	@PostMapping("/process-contact")
	public String processContact(
			@ModelAttribute Contact contact, 
			@RequestParam("profileImage") MultipartFile file,
			Principal principal, HttpSession session)
	{
		
		try {
		

		String name = principal.getName();
		User user = this.userRepository.getUserByUserName(name);


		//processing and uploading file 
		if(file.isEmpty())
				
		{
			//if the file is empty then try our message
			System.out.println("File is Empty");
			contact.setImage("profile.png");
		}
		
		else {
			//find the file to folder and update the name to contact 
		    contact.setImage(file.getOriginalFilename());
		    
		    
		   File saveFile = new ClassPathResource("static/img").getFile();
		   
		  Path path = Paths.get(saveFile.getAbsolutePath() + File.separator+file.getOriginalFilename());
		    
		  Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
		
		  System.out.println("Image is uploaded");
		}
				
				
				
		user.getContacts().add(contact);
		
		contact.setUser(user);
		
		
		this.userRepository.save(user);
		
	    System.out.println("DATA " + contact);
	    
	    System.out.println("Data added to database");
	    
	    //message success
	    session.setAttribute("message", new com.smart.helper.Message("Your Contact is added", "success") );
	    
		}catch(Exception e) {
		
			System.out.println("ERROR" + e.getMessage());
			e.printStackTrace();
			// message error
		    session.setAttribute("message", new com.smart.helper.Message("Something went wrong !! Try Again...", "danger") );

		}
	    
		return "normal/add_contact_form";
	}
	
	// show contacts handler 
	// per page = 5[n]
	// current page = 0[page]
	
	@GetMapping("/show-contacts/{page}")
	public String showContacts(@PathVariable("page") Integer page, Model m,Principal principal) {
		m.addAttribute("title","Show User - Contacts");
		
		// send contact list 
//		 String username = principal.getName();
//		
//		 User user = this.userRepository.getUserByUserName(username);
//		 
//		 List<Contact> contacts = user.getContacts();
		
		String username = principal.getName();
		User user= this.userRepository.getUserByUserName(username);
		
		Pageable pageable = PageRequest.of(page, 8);
		
		Page<Contact> contacts = this.contactRepository.findContactsByUser(user.getId(), pageable);
		
		m.addAttribute("currentPage", page);
		m.addAttribute("contacts", contacts);
		m.addAttribute("totalPages",contacts.getTotalPages());
		
		
		return "normal/show_contacts";
	}
	
	//showing particular contact details
	@RequestMapping("/{cId}/contact")
	public String showContactDetail(@PathVariable("cId") Integer cId, Model model, Principal principal)
	{
		System.out.println("CID" +cId);
		
		Optional<Contact> contactOptional = this.contactRepository.findById(cId);
		
		Contact contact = contactOptional.get();
		
		String username = principal.getName();
		User user = this.userRepository.getUserByUserName(username);
		
		
		if(user.getId() == contact.getUser().getId())
		{
			model.addAttribute("contact", contact);
			model.addAttribute("title", contact.getName());
		
		}
		
	
		
		return "normal/contact_detail";
	}
	
	
	// delete contact handler
	@GetMapping("/delete/{cId}")
	public String deleteContact(@PathVariable("cId") Integer cId, Model model,HttpSession session)
	{
		Optional<Contact> contactOptional = this.contactRepository.findById(cId);
		Contact  contact = contactOptional.get();
		
		//check...
		contact.setUser(null);
		
		this.contactRepository.delete(contact);
		
		contact.setUser(null);
		
		System.out.println("DELETED");
		session.setAttribute("message", new com.smart.helper.Message("Contact deleted successfully", "success"));
		
		
		return "redirect:/User/show-contacts/0";
	}
	
	// open update form handler
	@PostMapping("/update-contact/{cId}")
   public String updateForm(@PathVariable("cId") Integer cId, Model m)	
   {
		m.addAttribute("title", "Update Contact");
		
		Contact contact = this.contactRepository.findById(cId).get();
		
		m.addAttribute("contact", contact);
		
	   return "normal/update_form";
   }
	
	// update contact handler
	 @RequestMapping(value = "/process-update", method = RequestMethod.POST) 
	public String updateHandler(@ModelAttribute Contact contact,
			@RequestParam("profileImage") MultipartFile file, 
			Model m, HttpSession session, Principal principal) {
	
		 
		 try {
			 //old contact details
			 //image..
			 Contact oldcontactDetail = this.contactRepository.findById(contact.getcId()).get();
			 
			 if(file.isEmpty())
			 {
				 //file work..
				 //rewrite
				 // delete old photo and
				  File deleteFile = new ClassPathResource("static/img").getFile();
                  File file1=new File(deleteFile, oldcontactDetail.getImage());
				  file1.delete();
				 
				 
				 //update new photo
				 
				 
				  File saveFile = new ClassPathResource("static/img").getFile();
				   
				  Path path = Paths.get(saveFile.getAbsolutePath() + File.separator+file.getOriginalFilename());
				    
				  Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
				 
				  contact.setImage(file.getOriginalFilename());
				 
				 
				 
			 }else {
				 
				 contact.setImage(oldcontactDetail.getImage());
			 }
			 
			 
			 User user = this.userRepository.getUserByUserName(principal.getName());
			 
			 contact.setUser(user);
			 
			 this.contactRepository.save(contact);
			 
			 
			 session.setAttribute("message", new com.smart.helper.Message("Your contact is updated", "success"));
			 
			 
		 }
		 catch(Exception e){
			 e.printStackTrace();
		 }
		 
		 
		 System.out.println("CONTACT NAME" + contact.getName());
		 System.out.println("CONTACT  ID" + contact.getcId());

		 return "redirect:/User/"+contact.getcId()+"/contact";
	}
	
	
	 //your profile handler
	 @GetMapping("/profile")
	 public String yourProfile(Model model)
	 {
		 model.addAttribute("title", "Profile Page");
		 return "normal/profile";
	 }
	
	 
	 
	// open settings handler
	 @GetMapping("/settings")
	 public String openSettings()
	 {
		 return "normal/settings";
	 }
	
	 
	//change password handler
	 @PostMapping("/change-password")
	 public String changePassword(@RequestParam("oldPassword")String oldPassword,@RequestParam("newPassword") String newPassword, Principal principal, HttpSession session)
	 {
	     System.out.println("OLD PASSWORD" + oldPassword);
		 System.out.println("NEW PASSWORD" + newPassword);

		 String userName = principal.getName();
		 User currentUser = this.userRepository.getUserByUserName(userName);
		 
		 System.out.println(currentUser.getPassword());
		 
		 
		 if(this.bCryptPasswordEncoder.matches(oldPassword, currentUser.getPassword()))
		 {
			 //change the password
		   currentUser.setPassword(this.bCryptPasswordEncoder.encode(newPassword));
		   this.userRepository.save(currentUser);
		  session.setAttribute("message", new com.smart.helper.Message("Your Password is successfully changed", "success"));

		 }else
		 {
			 //error
		  session.setAttribute("message", new com.smart.helper.Message("Please enter correct oldPassword", "danger"));
			 return "redirect:/User/settings";

		 }
		 
		 
		 
		 
		 return "redirect:/User/index";

	 }
	
}
