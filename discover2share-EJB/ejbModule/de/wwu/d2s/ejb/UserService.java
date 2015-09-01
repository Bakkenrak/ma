package de.wwu.d2s.ejb;

import java.util.List;
import java.util.Map;

import javax.ejb.Remote;

import de.wwu.d2s.dto.AuthAccessElement;
import de.wwu.d2s.dto.AuthLoginElement;
import de.wwu.d2s.jpa.User;

@Remote
public interface UserService {

	public User findByUsernameAndAuthToken(String authId, String authToken);

	public User findByUsernameAndPassword(String username, String password);

	public User saveNew(User user);

	public void update(User user);

	public boolean changePassword(User user);

	public boolean deleteOwnAccount(AuthLoginElement user);

	public List<AuthAccessElement> getUsers();

	public boolean deleteAccount(User user);

}
