package de.wwu.d2s.ejb;


import java.util.List;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
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
}
