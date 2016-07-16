package br.com.litecode.controller;

import br.com.litecode.domain.User;
import br.com.litecode.service.UserService;
import org.apache.shiro.SecurityUtils;
import org.omnifaces.util.Faces;
import org.omnifaces.util.Messages;
import org.primefaces.context.RequestContext;

import javax.annotation.PostConstruct;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.Serializable;
import java.time.Instant;
import java.util.Date;
import java.util.List;

@Named
@ViewScoped
public class UserManager implements Serializable {
	private static final long serialVersionUID = 1L;
	
	@Inject private UserService userService;

	private User user;
	private User currentUser;
	private List<User> users;
	private Integer selectedClientId;

	@PostConstruct
	public void init() {
		currentUser = Faces.getSessionAttribute("loggedUser");

		if (currentUser == null) {
			currentUser = userService.getUserByUsername((String) SecurityUtils.getSubject().getPrincipal());
			currentUser.setLastAccess(Date.from(Instant.now()));
			userService.updateUser(currentUser);
			Faces.getSessionMap().put("loggedUser", currentUser);
		}
    	user = new User();
	}

	public List<User> getUsers() {
		if (users == null) {
			users = userService.getUsers();
		}
		return users;
	}
	
	public User getCurrentUser() {
		return currentUser;
	}

	public void saveUserPassword() {
		userService.updateUser(user);
	}
	
	public void saveUser() {
		if (!isUserNameUnique(user)) {
			Messages.addGlobalError("error.userAlreadyExists");
			RequestContext.getCurrentInstance().addCallbackParam("validationFailed", true);
			return;
		}
		
		if (user.getUserId() == null) {
			userService.createUser(user);
		} else {
			userService.updateUser(user);
		}
		users = null;
	}
	
	private boolean isUserNameUnique(User user) {
		for (User existingUser : userService.getUsers()) {
			if (!existingUser.equals(user) && existingUser.getUsername().equalsIgnoreCase(user.getUsername())) {
				return false;
			}
		}
		return true;
	}	
	
	public void deleteUser() {
		userService.deleteUser(user);
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