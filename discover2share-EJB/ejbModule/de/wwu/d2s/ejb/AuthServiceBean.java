package de.wwu.d2s.ejb;

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
    public boolean isAuthorized(String authId, String authToken, Set<String> rolesAllowed) {
    	if(authId == null || authToken == null)
    		return false;
    				
    	User user = userService.findByUsernameAndAuthToken(authId, authToken);
        if (user != null) {
            return rolesAllowed.contains(user.getAuthRole());
        } else {
            return false;
        }
    }
	
    @Override
    public AuthAccessElement login(AuthLoginElement loginElement) {	
    	User user = userService.findByUsernameAndPassword(loginElement.getUsername(), loginElement.getPassword());
        if (user != null) {
            user.setAuthToken(UUID.randomUUID().toString());
            userService.update(user);
            return new AuthAccessElement(loginElement.getUsername(), user.getAuthToken(), user.getAuthRole());
        }
        return null;
    }
}
