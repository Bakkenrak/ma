package de.wwu.d2s.web.api;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Context;

import de.wwu.d2s.dto.AuthAccessElement;
import de.wwu.d2s.dto.AuthLoginElement;
import de.wwu.d2s.ejb.AuthService;
import de.wwu.d2s.ejb.UserService;
import de.wwu.d2s.jpa.User;

@Stateless
public class AuthImpl implements AuthApi {
	@EJB
	AuthService authService;
	@EJB
	UserService userService;
	
	public AuthAccessElement login(@Context HttpServletRequest request, AuthLoginElement loginElement) {
        AuthAccessElement accessElement = authService.login(loginElement);
        if (accessElement != null) {
            request.getSession().setAttribute(AuthAccessElement.PARAM_AUTH_ID, accessElement.getAuthId());
            request.getSession().setAttribute(AuthAccessElement.PARAM_AUTH_TOKEN, accessElement.getAuthToken());
        }
        return accessElement;
    }

	@Override
	public User registerUser(HttpServletRequest request, User user) {
		userService.saveNew(user);
		return user;
	}
}