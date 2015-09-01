package de.wwu.d2s.ejb;

import java.util.ArrayList;
import java.util.List;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import de.wwu.d2s.dto.AuthAccessElement;
import de.wwu.d2s.dto.AuthLoginElement;
import de.wwu.d2s.jpa.User;

@Stateless
public class UserServiceBean implements UserService {

	@PersistenceContext
	EntityManager em;

	@Override
	public User findByUsernameAndAuthToken(String username, String authToken) {
		// return the user with the given username and auth token from the database
		return em.createQuery("SELECT u FROM User u WHERE u.username = '" + username + "' AND u.authToken = '" + authToken + "'", User.class).getSingleResult();
	}

	@Override
	public User findByUsernameAndPassword(String username, String password) {
		// find the user with the given username in the database
		User user = em.createQuery("SELECT u FROM User u WHERE u.username = '" + username + "'", User.class).getSingleResult();
		if (user != null) { // if one was found
			if (user.comparePassword(password)) // check if the given password matches
				return user; // return user object
		}
		return null;
	}

	@Override
	public User saveNew(User user) {
		// check if a user with the username of the given user already exists in the database
		List<User> results = em.createQuery("SELECT u FROM User u WHERE u.username = '" + user.getUsername() + "'", User.class).getResultList();
		if (!results.isEmpty()) // if one was found
			return null; 
		// otherwise
		user.hashOwnPassword(); // hash the new user's password
		em.persist(user); // save user in database
		return user;
	}

	@Override
	public void update(User user) {
		em.merge(user); // update given user in database
	}

	@Override
	public boolean changePassword(User user) {
		// find user in the database by the given user's name and password
		User match = findByUsernameAndPassword(user.getUsername(), user.getOldPassword());
		if (match != null) { // correct username and old password supplied
			match.setPassword(user.getPassword()); // set new password
			match.hashOwnPassword(); // hash password
			em.merge(match); // update user in the database
			return true; // success
		}
		return false; // no success
	}

	@Override
	public boolean deleteOwnAccount(AuthLoginElement user) {
		// find user in the database by the given user's name and password
		User match = findByUsernameAndPassword(user.getUsername(), user.getPassword());
		if (match != null) { // correct username and password supplied
			em.remove(match); // delete user from database
			return true; // success
		}
		return false; // no success
	}

	@Override
	public List<AuthAccessElement> getUsers() {
		List<AuthAccessElement> output = new ArrayList<AuthAccessElement>();
		// iterate through all users in the database
		for (User user : em.createQuery("from User", User.class).getResultList()) {
			// take username and auth role of each user and save them in new AuthAccessElement
			// this is done to not output whole user objects with passwords and possible auth tokens
			output.add(new AuthAccessElement(user.getUsername(), null, user.getAuthRole()));
		}
		return output;
	}

	@Override
	public boolean deleteAccount(User user) {
		// find user in the database by given user's user name
		User u = em.createQuery("SELECT u FROM User u WHERE u.username = '" + user.getUsername() + "'", User.class).getSingleResult();
		if (u != null) { // if a user was found
			try {
				em.remove(u); // remove it
				return true; // success
			} catch (Exception e) {
				return false; // no success if something went wrong
			}
		} else { // no such user found
			return false; // no success
		}
	}
}
