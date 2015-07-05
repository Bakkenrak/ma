package de.wwu.d2s.ejb;

import java.util.Date;
import java.util.Set;
import java.util.UUID;

import javax.ejb.EJB;
import javax.ejb.Stateless;

import de.wwu.d2s.dto.AuthAccessElement;
import de.wwu.d2s.dto.AuthLoginElement;
import de.wwu.d2s.jpa.User;

/**
 * Provides methods for user authorization.
 * Derived from: http://www.aschua.de/blog/pairing-angularjs-and-javaee-for-authentication/ (02/07/2015)
 */
@Stateless
public class AuthServiceBean implements AuthService {
	 
	@EJB
    UserService userService;
	
	@Override
    public int isAuthorized(String username, String authToken, Set<String> rolesAllowed) {
    	if(username == null || authToken == null)
    		return NO_SESSION; //no token provided
    				
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
    	User user = userService.findByUsernameAndPassword(loginElement.getUsername(), loginElement.getPassword());
        if (user != null) {
            user.setAuthToken(UUID.randomUUID().toString());
            user.setAuthDate(new Date());
            userService.update(user);
            return new AuthAccessElement(loginElement.getUsername(), user.getAuthToken(), user.getAuthRole());
        }
        return null;
    }
}
