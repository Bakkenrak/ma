package de.wwu.d2s.util;

import java.util.List;

import javax.annotation.PostConstruct;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import de.wwu.d2s.jpa.User;

/**
 * Seeds the database at application startup.
 */
@Singleton
@Startup
public class SeedDatabase {
	
	@PersistenceContext
	EntityManager em;
	
	@PostConstruct
	public void init(){
		//check whether the admin user exists
		List<User> results = em.createQuery("SELECT u FROM User u WHERE u.username = 'sa'", User.class).getResultList();
		if(results.isEmpty()){ //if not, create
			User u = new User();
			u.setUsername("sa");
			u.setAuthRole("admin");
			u.setPassword(User.ROLE_ADMIN);
			u.hashOwnPassword();
			em.persist(u);
		}
			
	}
}
