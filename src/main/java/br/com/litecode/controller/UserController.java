package br.com.litecode.controller;

import br.com.litecode.domain.model.User;
import br.com.litecode.domain.model.UserSettings;
import br.com.litecode.domain.repository.UserRepository;
import br.com.litecode.security.UserPrincipal;
import br.com.litecode.security.UserSessionTracker;
import br.com.litecode.service.MailService;
import com.google.common.base.Strings;
import com.google.gson.Gson;
import lombok.Getter;
import lombok.Setter;
import org.omnifaces.util.Messages;
import org.primefaces.PrimeFaces;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.faces.view.ViewScoped;
import java.io.Serializable;

@ViewScoped
@Component
public class UserController implements Serializable {
	@Autowired
	private UserRepository userRepository;

	@Autowired
	private UserSessionTracker userSessionTracker;

	@Autowired
	private MailService mailService;

	@Autowired
	private PasswordEncoder passwordEncoder;

	@Getter
	@Setter
	private User user;

	@Setter
	private Iterable<User> users;

	@Getter
	@Setter
	private String helpMessage;

	@PostConstruct
	public void init() {
    	user = new User();
	}

	public Iterable<User> getUsers() {
		if (users == null) {
			users = userRepository.findActiveUsers();
		}
		return users;
	}

	public void saveUserPassword() {
		user.setPassword(createPasswordHash(user.getPassword()));
		userRepository.save(user);
	}
	
	public void saveUser() {
		if (!isUsernameUnique(user)) {
			Messages.addGlobalError("error.userAlreadyExists");
			PrimeFaces.current().ajax().addCallbackParam("validationFailed", true);
			return;
		}

		if (user.getUserId() == null) {
			user.setPassword(createPasswordHash(user.getPassword()));
		}

		userRepository.save(user);
		users = null;
	}

	private String createPasswordHash(String password) {
		return passwordEncoder.encode(password);
	}

	private boolean isUsernameUnique(User user) {
		for (User existingUser : userRepository.findAll()) {
			if (!existingUser.equals(user) && existingUser.getUsername().equalsIgnoreCase(user.getUsername())) {
				return false;
			}
		}
		return true;
	}	
	
	public void deleteUser() {
		user.setActive(false);
		user.setUsername(user.getUsername() + "[" + user.getUserId() + "]");
		userRepository.save(user);
		users = null;
	}

	public void toggleNotificationMessages() {
		user.getUserSettings().setNotificationMessages(!user.getUserSettings().isNotificationMessages());
		userRepository.save(user);
	}

	public void toggleNotificationSounds() {
		user.getUserSettings().setNotificationSounds(!user.getUserSettings().isNotificationSounds());
		userRepository.save(user);
	}

	public void killUserSession(User user) {
		userSessionTracker.killUserSession(user);
		users = null;
	}

	public void getUserSettings() {
		if (UserPrincipal.getLoggedUser() == null) {
			PrimeFaces.current().ajax().addCallbackParam("userSettings", null);
			return;
		}

		UserSettings userSettings = UserPrincipal.getLoggedUser().getUserSettings();
		PrimeFaces.current().ajax().addCallbackParam("userSettings", new Gson().toJson(userSettings));
	}

	public int getOnlineUsers() {
		return userSessionTracker.getOnlineUsers();
	}

	public void sendHelpMessageEmail() {
		if (Strings.isNullOrEmpty(helpMessage)) {
			return;
		}

		mailService.sendEmail("thiago.wolff@gmail.com", "HCM - Report Issue [" + UserPrincipal.getLoggedUser().getUsername() + "]", helpMessage);
	}

	public void newUser() {
		user = new User();
	}
}