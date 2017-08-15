package br.com.litecode.service;

import br.com.litecode.domain.User;
import br.com.litecode.persistence.impl.UserDao;
import org.apache.shiro.crypto.hash.Sha256Hash;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.Query;
import java.util.List;

@Stateless
public class UserService {
	@Inject private UserDao userDao;

	public List<User> getUsers() {
		return userDao.findAll();
	}
	
	public User getUserByUsername(String username) {
		return userDao.findByUsername(username);
	}

	public User getUserBySessionId(String sessionId) {
		return userDao.findBySessionId(sessionId);
	}

	public void initializeUserSessions() {
		userDao.initializeUserSessions();
	}

	public User getUser(Integer userId) {
		return userDao.findById(userId);
	}	
	
	public void createUser(User user) {
		user.setPassword(createPasswordHash(user.getPassword()));
		userDao.insert(user);
	}

	public void updateUser(User user) {
		User currentUser = userDao.findById(user.getUserId());
		if (!currentUser.getPassword().equals(user.getPassword())) {
			String passHash = createPasswordHash(user.getPassword());
			user.setPassword(passHash);
		}
		userDao.update(user);
	}
	
	public void deleteUser(User user) {
		userDao.delete(user);
	}
	
	private String createPasswordHash(String password) {
		return new Sha256Hash(password).toBase64();		
	}
}
