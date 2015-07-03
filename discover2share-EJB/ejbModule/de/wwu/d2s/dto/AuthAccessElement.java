package de.wwu.d2s.dto;

import java.io.Serializable;

public class AuthAccessElement implements Serializable {
	private static final long serialVersionUID = -3707972521817063145L;
	
	public static final String PARAM_AUTH_ID = "auth-id";
    public static final String PARAM_AUTH_TOKEN = "auth-token";
 
    private String username;
    private String authToken;
    private String authRole;
 
    public AuthAccessElement() {
    }
 
    public AuthAccessElement(String username, String authToken, String authRole) {
        this.username = username;
        this.authToken = authToken;
        this.authRole = authRole;
    }

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getAuthRole() {
		return authRole;
	}

	public void setAuthRole(String authRole) {
		this.authRole = authRole;
	}

	public String getAuthToken() {
		return authToken;
	}

	public void setAuthToken(String authToken) {
		this.authToken = authToken;
	}

	public static String getParamAuthId() {
		return PARAM_AUTH_ID;
	}

	public static String getParamAuthToken() {
		return PARAM_AUTH_TOKEN;
	}
}
