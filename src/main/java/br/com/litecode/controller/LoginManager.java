package br.com.litecode.controller;

import br.com.litecode.domain.User;
import br.com.litecode.domain.User.Role;
import br.com.litecode.service.UserService;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.subject.Subject;
import org.omnifaces.util.Faces;
import org.omnifaces.util.Messages;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.IOException;
import java.time.Instant;
import java.util.Date;

@Named
@RequestScoped
public class LoginManager {
	@Inject private UserService userService;

	private String username;
	private String password;
	private boolean rememberMe;

	public void login() {
		Subject currentUser = SecurityUtils.getSubject();
		UsernamePasswordToken token = new UsernamePasswordToken(username, password, rememberMe);

		try {
			currentUser.login(token);
			
			User user = userService.getUserByUsername(username);
			user.setLastAccess(Date.from(Instant.now()));
			userService.updateUser(user);
			
			Faces.getSessionMap().put("loggedUser", user);
			
			Faces.redirect("index.xhtml");
		} catch (AuthenticationException e) {
			Messages.addGlobalError("error.login");
		} catch (IOException e) {
			Messages.addGlobalError(e.getMessage());
		}
	}
	
	public void logout() {
		SecurityUtils.getSubject().logout();
		Faces.invalidateSession();
        try {
        	Faces.redirect("login.xhtml");
        } catch (IOException e) {
        	Messages.addGlobalError(e.getMessage());
        }
	}

	public boolean hasAdminRights() {
		return Faces.isUserInRole(Role.ADMIN.name());
	}
	
	public String getUsername() {
		return username;
	}
	
	public void setUsername(String login) {
		this.username = login;
	}
	
	public String getPassword() {
		return password;
	}
	
	public void setPassword(String password) {
		this.password = password;
	}

	public boolean isRememberMe() {
		return rememberMe;
	}

	public void setRememberMe(boolean rememberMe) {
		this.rememberMe = rememberMe;
	}
}
