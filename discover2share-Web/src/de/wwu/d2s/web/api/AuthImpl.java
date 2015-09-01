package de.wwu.d2s.web.api;

import java.io.IOException;
import java.util.List;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

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
	
	@Context HttpServletRequest request;
	@Context HttpServletResponse response;
	
	/**
	 * API method for user login. Checks the given credentials and in case
	 * of a successful login the username and authentication token will be added
	 * to the request's header. Otherwise a 401 HTTP error will be given.
	 * Derived from: http://www.aschua.de/blog/pairing-angularjs-and-javaee-for-authentication/ (02/07/2015)
	 * 
	 * @return Object with authentication information
	 */
	public AuthAccessElement login(AuthLoginElement loginElement) {
        AuthAccessElement accessElement = authService.login(loginElement);
        if (accessElement != null) {
            request.getSession().setAttribute(AuthAccessElement.PARAM_AUTH_ID, accessElement.getUsername());
            request.getSession().setAttribute(AuthAccessElement.PARAM_AUTH_TOKEN, accessElement.getAuthToken());
        }else{
        	response.setStatus(HttpServletResponse.SC_UNAUTHORIZED); //return 401 on failed login
        	try{
        		response.flushBuffer();
        	}catch(IOException e){}
        }
        return accessElement;
    }

	@Override
	public Response registerUser(User user) {
		if (userService.saveNew(user) == null)
			return Response.status(Response.Status.CONFLICT).entity("{\"error\":\"A user with the name '" + user.getUsername() + "' already exists.\"}").build();
		return Response.status(Response.Status.OK).build();
	}

	@Override
	public Response changePassword(User user) {
		if(userService.changePassword(user))
			return Response.status(Response.Status.NO_CONTENT).build();
		else 
			return Response.status(Response.Status.UNAUTHORIZED).build();
	}

	@Override
	public Response deleteOwnAccount(AuthLoginElement user) {
		if(userService.deleteOwnAccount(user))
			return Response.status(Response.Status.NO_CONTENT).build();
		else 
			return Response.status(Response.Status.UNAUTHORIZED).build();
	}

	@Override
	public List<AuthAccessElement> getUsers() {
		return userService.getUsers();
	}

	@Override
	public Response deleteAccount(User user) {
		if (userService.deleteAccount(user))
			return Response.status(Response.Status.NO_CONTENT).build();
		else 
			return Response.status(Response.Status.UNAUTHORIZED).build();
	}
}
