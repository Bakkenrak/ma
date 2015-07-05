package de.wwu.d2s.dto;

import java.io.Serializable;

/**
 * Data transfer object for a user's login data.
 * Derived from: http://www.aschua.de/blog/pairing-angularjs-and-javaee-for-authentication/ (02/07/2015)
 */
public class AuthLoginElement implements Serializable {
	private static final long serialVersionUID = 1360714136731537456L;
	
	private String username;
    private String password;
 
    public AuthLoginElement() {
	}
    
    public AuthLoginElement(String username, String password) {
        this.username = username;
        this.password = password;
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
}
