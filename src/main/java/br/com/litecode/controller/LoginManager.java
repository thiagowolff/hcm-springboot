package br.com.litecode.controller;

import br.com.litecode.domain.User;
import br.com.litecode.domain.User.Role;
import br.com.litecode.security.UserSessionTracker;
import br.com.litecode.service.UserService;
import br.com.litecode.util.MessageUtil;
import com.google.common.base.Strings;
import com.google.common.io.ByteStreams;
import com.google.common.io.CharSource;
import com.google.common.io.CharStreams;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.subject.Subject;
import org.omnifaces.util.Faces;
import org.omnifaces.util.Messages;

import javax.enterprise.context.RequestScoped;
import javax.faces.application.FacesMessage;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.Instant;
import java.util.Date;

@Named
@RequestScoped
public class LoginManager {
	@Inject private UserService userService;
	@Inject private UserSessionTracker userSessionTracker;

	private String username;
	private String password;
	private boolean rememberMe;

	public void login() {
		Subject currentUser = SecurityUtils.getSubject();
		UsernamePasswordToken token = new UsernamePasswordToken(username, password, rememberMe);

		try {
			currentUser.login(token);
			User user = userService.getUserByUsername(username);

			HttpSession userSession = userSessionTracker.getUserSession(user);
			if (userSession != null && !userSession.getId().equals(Faces.getSessionId())) {
				userSessionTracker.killUserSession(user);
			}

			user.setLastAccess(Date.from(Instant.now()));
			user.setSessionId(Faces.getSessionId());
			String lastAccessLocation = getLastAccessLocation();
			if (lastAccessLocation != null) {
				user.setLastAccessLocation(lastAccessLocation);
			}

			userService.updateUser(user);
			userSessionTracker.addUserSession(user, Faces.getSession());

			Faces.getSessionMap().put("loggedUser", user);
			
			Faces.redirect("index.xhtml");
		} catch (AuthenticationException e) {
			Messages.addGlobalError("error.login");
		} catch (IOException e) {
			Messages.addGlobalError(e.getMessage());
		}
	}

	private String getLastAccessLocation() {
		try {
			URL url = new URL("http://ipinfo.io/" + Faces.getRemoteAddr() + "/json");
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			connection.setRequestMethod("GET");
			return new String(ByteStreams.toByteArray(connection.getInputStream()));
		} catch (Exception e) {
			return null;
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
