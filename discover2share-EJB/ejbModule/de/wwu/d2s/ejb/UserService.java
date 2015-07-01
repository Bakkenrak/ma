package de.wwu.d2s.ejb;

import java.util.List;

import javax.ejb.Stateless;

import de.wwu.d2s.jpa.User;

@Stateless
public interface UserService {

	public void save(User user);

	public User findByUsernameAndPassword(String username, String password);

	public User findByUsernameAndAuthToken(String authId, String authToken);

	public List<User> getAll();

}
