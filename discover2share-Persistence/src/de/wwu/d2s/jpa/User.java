package de.wwu.d2s.jpa;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class User implements java.io.Serializable {
	private static final long serialVersionUID = -7399509240684000311L;

	@Id
	private String username;
	
	private String password;
	private String authToken;
	private String authRole;
	
	public User(){}
	
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
}