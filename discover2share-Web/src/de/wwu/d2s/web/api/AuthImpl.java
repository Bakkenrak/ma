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
	
	
	public AuthAccessElement login(AuthLoginElement loginElement) {
        AuthAccessElement accessElement = authService.login(loginElement); // try login with given credentials
        if (accessElement != null) { // if authentication details were returned from login attempt
        	// set them in the requests session
            request.getSession().setAttribute(AuthAccessElement.PARAM_AUTH_ID, accessElement.getUsername());
            request.getSession().setAttribute(AuthAccessElement.PARAM_AUTH_TOKEN, accessElement.getAuthToken());
        }else{
        	response.setStatus(HttpServletResponse.SC_UNAUTHORIZED); //return 401 on failed login
        	try{
        		response.flushBuffer(); // apply
        	}catch(IOException e){}
        }
        return accessElement; // return authentication details
    }

	
	@Override
	public Response registerUser(User user) {
		if (userService.saveNew(user) == null) // if the attempt to save the new user failed
			// set error status and message to response
			return Response.status(Response.Status.CONFLICT).entity("{\"error\":\"A user with the name '" + user.getUsername() + "' already exists.\"}").build();
		return Response.status(Response.Status.OK).build(); // status 200 response
	}

	@Override
	public Response changePassword(User user) {
		if(userService.changePassword(user)) // if password change succeeded
			return Response.status(Response.Status.NO_CONTENT).build(); // status 204 response
		else 
			return Response.status(Response.Status.UNAUTHORIZED).build(); // error status unauthorized
	}

	@Override
	public Response deleteOwnAccount(AuthLoginElement user) {
		if(userService.deleteOwnAccount(user)) // if deletion of the account succeeded
			return Response.status(Response.Status.NO_CONTENT).build(); // success status 204 response
		else 
			return Response.status(Response.Status.UNAUTHORIZED).build(); // error status unauthorized
	}

	@Override
	public List<AuthAccessElement> getUsers() {
		return userService.getUsers();
	}

	@Override
	public Response deleteAccount(User user) {
		if (userService.deleteAccount(user)) // if deletion of the account succeeded
			return Response.status(Response.Status.NO_CONTENT).build(); // success status 204 response
		else 
			return Response.status(Response.Status.UNAUTHORIZED).build(); // error status unauthorized
	}
}
