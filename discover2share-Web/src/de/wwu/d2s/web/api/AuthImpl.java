package de.wwu.d2s.web.api;

import java.util.List;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.servlet.http.HttpServletRequest;

import de.wwu.d2s.dto.AuthAccessElement;
import de.wwu.d2s.dto.AuthLoginElement;
import de.wwu.d2s.ejb.AuthService;
import de.wwu.d2s.ejb.UserService;
import de.wwu.d2s.jpa.User;

@Stateless
public class AuthImpl implements AuthApi{
 
    @EJB
    AuthService authService;
    @EJB
    UserService userService;

	@Override
	public AuthAccessElement login(HttpServletRequest request,
			AuthLoginElement loginElement) {
		AuthAccessElement accessElement = authService.login(loginElement);
		if (accessElement != null) {
            request.getSession().setAttribute(AuthAccessElement.PARAM_AUTH_ID, accessElement.getAuthId());
            request.getSession().setAttribute(AuthAccessElement.PARAM_AUTH_TOKEN, accessElement.getAuthToken());
        }
        return accessElement;
	}

	@Override
	public void registerUser(HttpServletRequest request,
			AuthLoginElement loginElement) {
		userService.save(new User(loginElement.getUsername(), loginElement.getPassword()));
	}
	
	@Override
	public List<User> getAllUsers() {
		return userService.getAll();
	}
}
