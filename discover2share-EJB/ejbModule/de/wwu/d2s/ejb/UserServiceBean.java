package de.wwu.d2s.ejb;

import java.util.List;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import de.wwu.d2s.jpa.User;

@Stateless
public class UserServiceBean implements UserService {
	@PersistenceContext
	private EntityManager em;
	

	@Override
	public void save(User user) {
		em.persist(user);
	}

	@Override
	public User findByUsernameAndPassword(String username, String password) {
		User user = em.find(User.class, username); //retrieve user by ID column username
		if(user.getPassword().equals(password)) //check for correct password
			return user;
		return null;
	}

	@Override
	public User findByUsernameAndAuthToken(String username, String authToken) {
		User user = em.find(User.class, username); //retrieve user by ID column username
		if(user.getAuthToken().equals(authToken)) //check for correct token
			return user;
		return null;
	}

	@Override
	public List<User> getAll() {
		Query query = em.createQuery("SELECT u FROM User u");
		return (List<User>) query.getResultList();
	}

}
