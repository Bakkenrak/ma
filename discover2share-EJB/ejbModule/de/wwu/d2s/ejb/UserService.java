package de.wwu.d2s.ejb;

import java.util.List;

import javax.ejb.Remote;

import de.wwu.d2s.dto.AuthAccessElement;
import de.wwu.d2s.dto.AuthLoginElement;
import de.wwu.d2s.jpa.User;

@Remote
public interface UserService {

	/**
	 * @param username
	 * @param authToken
	 * @return The user with the given name and a session with the given auth token
	 */
	public User findByUsernameAndAuthToken(String username, String authToken);

	/**
	 * @param username
	 * @param password
	 * @return The user with the given username and password
	 */
	public User findByUsernameAndPassword(String username, String password);

	/**
	 * Saves the given new user in the database
	 * 
	 * @param user
	 * @return The newly saved user
	 */
	public User saveNew(User user);

	/**
	 * Updates the given user in the database
	 * 
	 * @param user
	 */
	public void update(User user);

	/**
	 * Changes the password for the given user. New and old password should be contained in the user object.
	 * 
	 * @param user
	 * @return true, if update was successful, otherwise false
	 */
	public boolean changePassword(User user);

	/**
	 * Deletes the account with the information in the given AuthLoginElement.
	 * 
	 * @param user
	 *            AuthLoginElement containing the username and password
	 * @return true, if delete was successful, otherwise false
	 */
	public boolean deleteOwnAccount(AuthLoginElement user);

	/**
	 * @return A list of AuthAccessElement (to only give out relevant information) for each user in the database.
	 */
	public List<AuthAccessElement> getUsers();

	/**
	 * Deletes the user from the database that matches the given one.
	 * 
	 * @param user
	 *            Object containing the username of the user to delete
	 * @return true, if delete was successful, otherwise false
	 */
	public boolean deleteAccount(User user);

}
