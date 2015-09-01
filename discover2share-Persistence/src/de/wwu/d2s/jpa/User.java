package de.wwu.d2s.jpa;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Calendar;
import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Transient;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Entity
@JsonIgnoreProperties(ignoreUnknown = true)
public class User implements java.io.Serializable {
	private static final long serialVersionUID = -7399509240684000311L;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private int id;

	private String username;
	private String password;
	private String oldPassword;
	private String authToken;
	private String authRole;
	private Date authDate;
	
	@Transient
	public final static String ROLE_ADMIN = "admin";
	@Transient
	public final static String ROLE_MODERATOR = "moderator";

	public User() {
	}
	
	public boolean hasValidAuth(){
		if(!authToken.isEmpty() && !(authDate == null)){
			//Add 1 Day to the authDate to determine the expiration date
			Calendar cal = Calendar.getInstance();
			cal.setTime(authDate);
			cal.add(Calendar.DATE, 1);
			Date expiryTime = cal.getTime();
			if(expiryTime.after(new Date()))
				return true;
		}
		return false;
	}
	
	public void hashOwnPassword(){
		password = hashPassword(password);
	}

	public String hashPassword(String password) {
		MessageDigest messageDigest;
		try {
			messageDigest = MessageDigest.getInstance("SHA-256");
			messageDigest.update(password.getBytes());
			return new String(messageDigest.digest());
		} catch (NoSuchAlgorithmException e) {
			return "";
		}
	}
	
	public boolean comparePassword(String pw) {
		String hashed = hashPassword(pw);
		if(password.equals(hashed))
			return true;
		return false;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getOldPassword() {
		return oldPassword;
	}

	public void setOldPassword(String oldPassword) {
		this.oldPassword = oldPassword;
	}

	public String getAuthToken() {
		return authToken;
	}

	public void setAuthToken(String authToken) {
		this.authToken = authToken;
	}

	public String getAuthRole() {
		return authRole;
	}

	public void setAuthRole(String authRole) {
		this.authRole = authRole;
	}

	public Date getAuthDate() {
		return authDate;
	}

	public void setAuthDate(Date authDate) {
		this.authDate = authDate;
	}
}
