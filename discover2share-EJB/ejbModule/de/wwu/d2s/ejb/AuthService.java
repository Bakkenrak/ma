package de.wwu.d2s.ejb;

import java.util.Set;

import javax.ejb.Remote;

import de.wwu.d2s.dto.AuthAccessElement;
import de.wwu.d2s.dto.AuthLoginElement;

@Remote
public interface AuthService {
	
	public static final int NO_SESSION = 0;
	public static final int WRONG_ROLE = -1;
	public static final int EXPIRED = -2;
	public static final int AUTHORIZED = 1;
	
	public AuthAccessElement login(AuthLoginElement loginElement);

	public int isAuthorized(String authId, String authToken,
			Set<String> rolesAllowed);
	
}
