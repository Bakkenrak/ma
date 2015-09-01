package de.wwu.d2s.ejb;

import java.util.Date;
import java.util.Set;
import java.util.UUID;

import javax.ejb.EJB;
import javax.ejb.Stateless;

import de.wwu.d2s.dto.AuthAccessElement;
import de.wwu.d2s.dto.AuthLoginElement;
import de.wwu.d2s.jpa.User;

@Stateless
public class AuthServiceBean implements AuthService {
	 
	@EJB
    UserService userService;
	
	@Override
    public int isAuthorized(String username, String authToken, Set<String> rolesAllowed) {
    	if(username == null || authToken == null)
    		return NO_SESSION; //no token provided
    	
    	// find user by username and auth token
    	User user = userService.findByUsernameAndAuthToken(username, authToken);
        if (user == null)
        	return NO_SESSION; //no session with the given user and token at all
        if(!rolesAllowed.contains(user.getAuthRole()))
        	return WRONG_ROLE; //session exists but user has insufficient role
        if(!user.hasValidAuth())
        	return EXPIRED; //the session has expired
        
        return AUTHORIZED; //user is fully authorized
    }
	
    @Override
    public AuthAccessElement login(AuthLoginElement loginElement) {
    	// find user by username and password
    	User user = userService.findByUsernameAndPassword(loginElement.getUsername(), loginElement.getPassword());
        if (user != null) { // if a user is found
            user.setAuthToken(UUID.randomUUID().toString()); // generate random auth token
            user.setAuthDate(new Date());
            userService.update(user); // save changed user object to database
            // return auth info
            return new AuthAccessElement(loginElement.getUsername(), user.getAuthToken(), user.getAuthRole());
        }
        return null; // no user found
    }
}
