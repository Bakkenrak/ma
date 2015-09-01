package de.wwu.d2s.ejb;


import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
		List<User> results = em.createQuery("SELECT u FROM User u WHERE u.username = '" + username + "' AND u.authToken = '" + authToken + "'", User.class).getResultList();
		if(!results.isEmpty())
			return results.get(0);
		return null;
	}

	@Override
	public User findByUsernameAndPassword(String username, String password) {
		List<User> results = em.createQuery("SELECT u FROM User u WHERE u.username = '" + username + "'", User.class).getResultList();
		if(!results.isEmpty()){
			User user = results.get(0);
			if(user.comparePassword(password))
				return user;
		}
		return null;
	}

	@Override
	public User saveNew(User user) {
		List<User> results = em.createQuery("SELECT u FROM User u WHERE u.username = '" + user.getUsername() + "'", User.class).getResultList();
		if(!results.isEmpty())
			return null;
		user.hashOwnPassword();
		em.persist(user);
		return user;
	}

	@Override
	public void update(User user) {
		em.merge(user);
	}

	@Override
	public boolean changePassword(User user) {
		User match = findByUsernameAndPassword(user.getUsername(), user.getOldPassword());
		if (match != null) { // correct username and old password supplied
			match.setPassword(user.getPassword());
			match.hashOwnPassword();
			em.merge(match);
			return true;
		}
		return false;
	}

	@Override
	public boolean deleteOwnAccount(AuthLoginElement user) {
		User match = findByUsernameAndPassword(user.getUsername(), user.getPassword());
		if (match != null) { // correct username and old password supplied
			em.remove(match);
			return true;
		}
		return false;
	}

	@Override
	public List<AuthAccessElement> getUsers() {
		List<AuthAccessElement> output = new ArrayList<AuthAccessElement>();
		for(User user : em.createQuery("from User", User.class).getResultList()) {
			output.add(new AuthAccessElement(user.getUsername(), null, user.getAuthRole()));
		}
		return output;
	}

	@Override
	public boolean deleteAccount(User user) {
		User u = em.createQuery("SELECT u FROM User u WHERE u.username = '" + user.getUsername() + "'", User.class).getSingleResult();
		if (u != null) {
			try {
				em.remove(u);
				return true;
			} catch (Exception e) {
				return false;
			}
		} else {
			return false;
		}
	}
}
