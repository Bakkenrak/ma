package de.wwu.d2s.jpa;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class User {
	@Id
	private String username;
	private String authToken;
	private String authRole;
	private String password;

	public User() {

	}

	public User(String authToken, String authRole, String username,
			String password) {
		super();
		this.authToken = authToken;
		this.authRole = authRole;
		this.username = username;
		setPassword(password);
	}

	public User(String username, String password) {
		this.username = username;
		setPassword(password);
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
		// TODO: hash password
		this.password = password;
	}

}
