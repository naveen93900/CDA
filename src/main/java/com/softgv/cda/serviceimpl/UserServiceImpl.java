package com.softgv.cda.serviceimpl;

import java.time.LocalTime;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import com.softgv.cda.dao.AdministratorProfileDao;
import com.softgv.cda.dao.FacultyProfileDao;
import com.softgv.cda.dao.StudentProfileDao;
import com.softgv.cda.dao.UserDao;
import com.softgv.cda.entity.AdministratorProfile;
import com.softgv.cda.entity.FacultyProfile;
import com.softgv.cda.entity.StudentProfile;
import com.softgv.cda.entity.User;
import com.softgv.cda.exceptionclasses.UserNotFoundException;
import com.softgv.cda.responsestructure.ResponseStructure;
import com.softgv.cda.service.UserService;
import com.softgv.cda.util.AuthUser;
import com.softgv.cda.util.Helper;
import com.softgv.cda.util.MyUtil;
import com.softgv.cda.util.Role;
import com.softgv.cda.util.UserStatus;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
public class UserServiceImpl implements UserService {

	@Autowired
	private Helper helper;
	@Autowired
	private JavaMailSender javaMailSender;

	@Autowired
	private UserDao userDao;

	@Autowired
	private StudentProfileDao studentProfileDao;

	@Autowired
	private FacultyProfileDao facultyProfileDao;

	@Autowired
	private AdministratorProfileDao administratorProfileDao;

	public ResponseEntity<?> findByUsernameAndPassword(AuthUser authUser) {
		Optional<User> optional = userDao.findByUsernameAndPassword(authUser.getUsername(), authUser.getPassword());
		if (optional.isEmpty())
			throw UserNotFoundException.builder().message("Invalid Credentials... Invalid Username or Password...")
					.build();
		return ResponseEntity.status(HttpStatus.OK).body(ResponseStructure.builder().status(HttpStatus.OK.value())
				.message("User Verified Successfully...").body(optional.get()).build());
	}

	@Override
	public ResponseEntity<?> saveUser(User user) {
//		int otp = helper.generateOTP();
//		boolean flag = helper.sendFirstMail(user.getEmail(), otp);
//		if (!flag)
//			return ResponseEntity.status(HttpStatus.BAD_REQUEST)
//					.body(ResponseStructure.builder().status(HttpStatus.BAD_REQUEST.value())
//							.message("Enter Valid Email").body("Invaid Email Id : " + user.getEmail()).build());
//	
		String photo = "C:\\Users\\gagan\\Documents\\My-React\\cda-react-app\\public\\images\\userprofile.jpg";
//		user.setStatus(UserStatus.IN_ACTIVE);
//		
		user.setOtp(MyUtil.getOTP());
		
		String otpmail="<!DOCTYPE html>\n"
				+ "<html lang=\"en\">\n"
				+ "<head>\n"
				+ "    <meta charset=\"UTF-8\">\n"
				+ "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n"
				+ "    <title>Account Created</title>\n"
				+ "    <style>\n"
				+ "        body {\n"
				+ "            font-family: Arial, sans-serif;\n"
				+ "            background-color: #f4f4f9;\n"
				+ "            color: #333;\n"
				+ "            text-align: center;\n"
				+ "            padding: 50px;\n"
				+ "            margin: 0;\n"
				+ "        }\n"
				+ "\n"
				+ "        h1 {\n"
				+ "            font-size: 24px;\n"
				+ "            color: #4CAF50; /* Green for success */\n"
				+ "            border: 2px solid #4CAF50;\n"
				+ "            padding: 20px;\n"
				+ "            display: inline-block;\n"
				+ "            border-radius: 10px;\n"
				+ "            background-color: #ffffff; /* White background for contrast */\n"
				+ "            box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);\n"
				+ "        }\n"
				+ "\n"
				+ "        @media (max-width: 600px) {\n"
				+ "            body {\n"
				+ "                padding: 20px;\n"
				+ "            }\n"
				+ "            h1 {\n"
				+ "                font-size: 20px;\n"
				+ "                padding: 15px;\n"
				+ "            }\n"
				+ "        }\n"
				+ "    </style>\n"
				+ "</head>\n"
				+ "<body>\n"
				+ "    <h1>Hello " + user.getName() + ", Your CDA account was created Successfully " + MyUtil.getOTP() + "</h1>\n"
				+ "</body>\n"
				+ "</html>\n"
				+ "";
		
		
		user = userDao.saveUser(user);
		
		MimeMessage mimeMessage=javaMailSender.createMimeMessage();
		try {
			MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage,true);
			mimeMessageHelper.addTo(user.getEmail());
			mimeMessageHelper.setSubject("Account Created");
			mimeMessageHelper.setText(otpmail,true);
		    
			javaMailSender.send(mimeMessage);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		
//		if (user.getRole() == Role.ADMINISTRATOR)
//			administratorProfileDao.saveAdministratorProfile(
//					AdministratorProfile.builder().id(user.getId()).photo(photo).user(user).build());
//		else if (user.getRole() == Role.FACULTY)
//			facultyProfileDao.saveFacultyProfile(FacultyProfile.builder().id(user.getId()).user(user).photo(photo)
//					.officeHours(LocalTime.of(8, 30)).build());
//		else
//			studentProfileDao
//					.saveStudentProfile(StudentProfile.builder().id(user.getId()).photo(photo).user(user).build());
		
		return ResponseEntity.status(HttpStatus.OK).body(ResponseStructure.builder().status(HttpStatus.OK.value())
				.message("User Saved Successfully...").body(user).build());
	}

	@Override
	public ResponseEntity<?> findAllUsers() {
		return ResponseEntity.status(HttpStatus.OK).body(ResponseStructure.builder().status(HttpStatus.OK.value())
				.message("All Users Found Successfully...").body(userDao.findAllUsers()).build());
	}

	@Override
	public ResponseEntity<?> findUserById(int id) {
		Optional<User> optional = userDao.findUserById(id);
		if (optional.isEmpty())
			throw UserNotFoundException.builder().message("Invalid User Id : " + id).build();
		User user = optional.get();
		return ResponseEntity.status(HttpStatus.OK).body(ResponseStructure.builder().status(HttpStatus.OK.value())
				.message("User Found Successfully...").body(user).build());
	}

	@Override
	public ResponseEntity<?> verifyOTP(int id, int otp) {

		    Optional<User> optional = userDao.findUserById(id);
		    if(optional.isEmpty()) 
		    	throw new RuntimeException("Invalid User Id unable to verify the OTP");
		    User user=optional.get();
		    if(otp!=user.getOtp())
		    		throw new RuntimeException("Invalid OTP ");
		    
		    user.setStatus(UserStatus.ACTIVE);
		    
		    user=userDao.saveUser(user);
		    
		    return ResponseEntity.status(HttpStatus.OK).body(ResponseStructure.builder().status(HttpStatus.OK.value())
					.message("OTP verified Successfully...").body(user).build());
		    
	
	}

}
