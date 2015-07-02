package de.wwu.d2s.ejb;

import java.util.List;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import de.wwu.d2s.jpa.Person;
import de.wwu.d2s.jpa.User;

@Stateless
public class UserServiceBean implements UserService {
	
	@PersistenceContext
	EntityManager em;
	
	//Session session = em.unwrap(org.hibernate.Session.class);

	@Override
	public User findByUsernameAndAuthToken(String username, String authToken) {
		User user = em.find(User.class, username);
		if(user != null && user.getAuthToken().equals(authToken))
			return user;
		return null;
	}

	@Override
	public User findByUsernameAndPassword(String username, String password) {
		User user = em.find(User.class, username);
		if(user != null && user.getPassword().equals(password))
			return user;
		return null;
	}

	@Override
	public void saveNew(User user) {
		em.persist(user);
	}

	@Override
	public void update(User user) {
		
	}
}
