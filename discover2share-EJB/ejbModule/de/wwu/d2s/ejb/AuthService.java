package de.wwu.d2s.ejb;

import java.util.Set;

import javax.ejb.Remote;

import de.wwu.d2s.dto.AuthAccessElement;
import de.wwu.d2s.dto.AuthLoginElement;

@Remote
public interface AuthService {

	public AuthAccessElement login(AuthLoginElement loginElement);

	public boolean isAuthorized(String authId, String authToken,
			Set<String> rolesAllowed);
	
}
