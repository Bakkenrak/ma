package de.wwu.d2s.web.api;

import java.util.List;
import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import de.wwu.d2s.dto.AuthAccessElement;
import de.wwu.d2s.dto.AuthLoginElement;
import de.wwu.d2s.jpa.User;

/**
 * API section providing all necessary methods regarding user authentication and management.
 */
@Path("/auth")
@Produces("application/json")
@Consumes("application/json")
public interface AuthApi {

	/**
	 * API method for user login. Checks the given credentials and in case of a successful login the username and authentication token will be added to the
	 * request's header. Otherwise a 401 HTTP error will be given. 
	 * Derived from: http://www.aschua.de/blog/pairing-angularjs-and-javaee-for-authentication/ (02/07/2015)
	 *
	 * @param loginElement
	 *            Login credentials
	 * @return Object with authentication information
	 */
	@POST
	@Path("login")
	@PermitAll
	public AuthAccessElement login(AuthLoginElement loginElement);

	/**
	 * API method to register a new user account with the given information.
	 * Can only be executed by authenticated administrators.
	 * 
	 * @param user
	 * 			User information for new account.
	 * @return HTTP Response with an success/error code and a possible error message
	 */
	@POST
	@Path("register")
	@RolesAllowed(value = { "admin" })
	public Response registerUser(User user);

	/**
	 * API method to change the given user's password.
	 * Can only be executed by authenticated moderators or administrators.
	 * 
	 * @param user
	 * 			User object containing username, old password and new password
	 * @return HTTP Response with an success/error code
	 */
	@POST
	@Path("changePassword")
	@RolesAllowed(value = { "admin", "mod" })
	public Response changePassword(User user);

	/**
	 * API method to delete the account of the executing user.
	 * Can only be executed by authenticated moderators or administrators.
	 * 
	 * @param user
	 * 			Object containing the user's password to confirm the operation
	 * @return HTTP Response with an success/error code
	 */
	@POST
	@Path("deleteOwnAccount")
	@RolesAllowed(value = { "admin", "mod" })
	public Response deleteOwnAccount(AuthLoginElement user);

	/**
	 * API method to delete another user's account.
	 * Can only be executed by authenticated administrators.
	 * 
	 * @param user
	 * 			Object containing the information about the user account to remove
	 * @return HTTP Response with an success/error code
	 */
	@POST
	@Path("deleteAccount")
	@RolesAllowed(value = { "admin" })
	public Response deleteAccount(User user);

	/**
	 * API method returning a list of all user accounts in the system.
	 * Can only be executed by authenticated administrators.
	 * 
	 * @return A list of all user accounts in the system.
	 */
	@GET
	@Path("users")
	@RolesAllowed(value = { "admin" })
	public List<AuthAccessElement> getUsers();
}