package br.com.litecode.controller;

import br.com.litecode.annotation.ScopeView;
import br.com.litecode.domain.model.User;
import br.com.litecode.domain.repository.UserRepository;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.crypto.hash.Sha256Hash;
import org.omnifaces.util.Faces;
import org.omnifaces.util.Messages;
import org.primefaces.context.RequestContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.Serializable;
import java.time.Instant;
import java.util.List;

@ScopeView
@Component
public class UserController implements Serializable {
	private static final long serialVersionUID = 1L;
	
	@Autowired
	private UserRepository userRepository;

	private User user;
	private User currentUser;
	private Iterable<User> users;
	private Integer selectedClientId;

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
		} else {
			String passwordHash = createPasswordHash(user.getPassword());
			user.setPassword(passwordHash);
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
	
	public void newUser() {
		user = new User();
	}
	
	public User getUser() {
		return user;
	}
	
	public void setUser(User user) {
		this.user = user;
	}
	
	public void setUsers(List<User> users) {
		this.users = users;
	}

	public Integer getSelectedClientId() {
		return selectedClientId;
	}

	public void setSelectedClientId(Integer selectedClientId) {
		this.selectedClientId = selectedClientId;
	}
}