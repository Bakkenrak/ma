package de.wwu.d2s.ejb;

import javax.ejb.Remote;

import de.wwu.d2s.jpa.User;

@Remote
public interface UserService {

	public User findByUsernameAndAuthToken(String authId, String authToken);

	public User findByUsernameAndPassword(String username, String password);

	public User saveNew(User user);

	public void update(User user);

}
