package br.com.litecode.controller;

import br.com.litecode.domain.model.User;
import br.com.litecode.domain.repository.UserRepository;
import br.com.litecode.security.UserSessionTracker;
import lombok.Getter;
import lombok.Setter;
import org.apache.shiro.crypto.hash.Sha256Hash;
import org.omnifaces.util.Messages;
import org.primefaces.context.RequestContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.Serializable;

@Scope("view")
@Component
public class UserController implements Serializable {
	@Autowired
	private UserRepository userRepository;

	@Autowired
	private UserSessionTracker userSessionTracker;

	@Getter
	@Setter
	private User user;

	@Setter
	private Iterable<User> users;

	@PostConstruct
	public void init() {
    	user = new User();
	}

	public Iterable<User> getUsers() {
		if (users == null) {
			users = userRepository.findAll();
		}
		return users;
	}

	public void saveUserPassword() {
		user.setPassword(createPasswordHash(user.getPassword()));
		userRepository.save(user);
	}
	
	public void saveUser() {
		if (!isUserNameUnique(user)) {
			Messages.addGlobalError("error.userAlreadyExists");
			RequestContext.getCurrentInstance().addCallbackParam("validationFailed", true);
			return;
		}

		if (user.getUserId() == null) {
			user.setPassword(createPasswordHash(user.getPassword()));
		}

		userRepository.save(user);
		users = null;
	}

	private String createPasswordHash(String password) {
		return new Sha256Hash(password).toBase64();
	}

	private boolean isUserNameUnique(User user) {
		for (User existingUser : userRepository.findAll()) {
			if (!existingUser.equals(user) && existingUser.getUsername().equalsIgnoreCase(user.getUsername())) {
				return false;
			}
		}
		return true;
	}	
	
	public void deleteUser() {
		userRepository.delete(user);
		users = null;
	}

	public void killUserSession(User user) {
		userSessionTracker.killUserSession(user);
		users = null;
	}

	public void newUser() {
		user = new User();
	}
}