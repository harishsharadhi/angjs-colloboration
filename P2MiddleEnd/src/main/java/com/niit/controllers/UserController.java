package com.niit.controllers;

import javax.servlet.http.HttpSession;

import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.niit.dao.UserDao;
import com.niit.model.ErrorClass;
import com.niit.model.User;


 
@Controller
public class UserController {
@Autowired
private UserDao userDao;
@Autowired
private SessionFactory sessionFactory;
public UserController(){
	System.out.println("UserController bean is created");
}
//?Any type [ErrorClass  /User object]
@RequestMapping(value="/registerUser",method=RequestMethod.POST)
public ResponseEntity<?> registerUser(@RequestBody User user){    
	
	//user is from front end
    //check for duplicate email
	//if email is unique then call registerUser method
	System.out.println("registerUser in UserController"+ user);//call toString method in User class
	if(!userDao.isEmailUnique(user.getEmail())){
		ErrorClass error=new ErrorClass(1,"Email already exists...please enter different email");
		return new ResponseEntity<ErrorClass>(error,HttpStatus.CONFLICT);
	}
	try{
	userDao.registerUser(user);//insert
	}catch(Exception e){
		ErrorClass error=new ErrorClass(2,"some required fields are empty..."+e.getMessage());
		return new ResponseEntity<ErrorClass>(error,HttpStatus.INTERNAL_SERVER_ERROR);
	}
	return new ResponseEntity<User>(user,HttpStatus.OK);
}
@RequestMapping(value="/login",method=RequestMethod.POST)
public ResponseEntity<?> login(@RequestBody User user,HttpSession session){    
	System.out.println(user);
	User validUser=userDao.login(user);
	System.out.println(validUser);
	 
	if(validUser==null){    //invalid credentials
		ErrorClass error=new ErrorClass(5,"Login failed....Invalid email/password..");   //response.data
		return new ResponseEntity<ErrorClass>(error,HttpStatus.UNAUTHORIZED);
	}
	else{     //valid credentials
		validUser.setOnline(true);
		 userDao.update(validUser);//update online status to true
	session.setAttribute("loginId", user.getEmail());
		return new ResponseEntity<User>(validUser,HttpStatus.OK);
	}
	
}
@RequestMapping(value="/logout",method=RequestMethod.PUT)
public ResponseEntity<?>logout(HttpSession session){
	String email=(String)session.getAttribute("loginId");
	if (email==null){
		ErrorClass error=new ErrorClass(4,"Please login...");
		return new ResponseEntity<ErrorClass>(error,HttpStatus.UNAUTHORIZED);
	}
	
	User user=userDao.getUser(email);
	user.setOnline(false);
	userDao.update(user);  //update onlinestatus to false
	session.removeAttribute("loginId");
	session.invalidate();
	return new ResponseEntity<User>(user,HttpStatus.OK);
	
	
}
@RequestMapping(value="/getuser",method=RequestMethod.GET)
public ResponseEntity<?> getUser(HttpSession session){
	String email=(String)session.getAttribute("loginId");
	if(email==null){
		ErrorClass error=new ErrorClass(5,"Unauthorised access...");
		return new ResponseEntity<ErrorClass>(error,HttpStatus.UNAUTHORIZED);
	}
	User user=userDao.getUser(email);
	/*user.setOnline(false);
    userDao.update(user);  //update onlinestatus to false
    session.removeAttribute("loginId");
    session.invalidate();*/
	return new ResponseEntity<User>(user,HttpStatus.OK);
    }

@RequestMapping(value="/updateuser",method=RequestMethod.PUT)
public ResponseEntity<?> updateUser(@RequestBody User user,HttpSession session){
    String email=(String)session.getAttribute("loginId");
     if(email==null){   //not logged in  ,loginId returns null. 
      	ErrorClass error=new ErrorClass(5,"Unauthorised access...");
	    return new ResponseEntity<ErrorClass>(error,HttpStatus.UNAUTHORIZED);
}
     try{
    	 userDao.update(user);
    	 return new ResponseEntity<User>(user,HttpStatus.OK);
     }catch(Exception e){
    	 ErrorClass error=new ErrorClass(5,"Unable to update user details...."+e.getMessage());
    	 return new ResponseEntity<ErrorClass>(error,HttpStatus.INTERNAL_SERVER_ERROR);
     }
    	 
    	 
    	 
    	 
    	 
    	 
    	  }
}
