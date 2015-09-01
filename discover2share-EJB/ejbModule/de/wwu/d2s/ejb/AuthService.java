package de.wwu.d2s.ejb;

import java.util.Set;

import javax.ejb.Remote;

import de.wwu.d2s.dto.AuthAccessElement;
import de.wwu.d2s.dto.AuthLoginElement;

/**
 * Provides methods for user authorization.
 * Derived from: http://www.aschua.de/blog/pairing-angularjs-and-javaee-for-authentication/ (02/07/2015)
 */
@Remote
public interface AuthService {
	
	public static final int NO_SESSION = 0;
	public static final int WRONG_ROLE = -1;
	public static final int EXPIRED = -2;
	public static final int AUTHORIZED = 1;
	
	/**
	 * Logs in the user for which valid login data is provided.
	 * 
	 * @param loginElement Login credentials
	 * @return Auth session data
	 */
	public AuthAccessElement login(AuthLoginElement loginElement);

	/**
	 * Checks if a valid auth session exists for the given username and auth token
	 * and whether it fits the given set of roles that are allowed to perform the
	 * action is question.
	 * 
	 * @param username
	 * @param authToken
	 * @param rolesAllowed Set of auth roles to check the one of the user against
	 * @return Int representing the authentication status for the given parameters
	 */
	public int isAuthorized(String username, String authToken,
			Set<String> rolesAllowed);
	
}
