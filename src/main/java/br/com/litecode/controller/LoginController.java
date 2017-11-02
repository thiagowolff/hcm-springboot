package br.com.litecode.controller;

import br.com.litecode.domain.model.User;
import br.com.litecode.domain.model.User.Role;
import br.com.litecode.domain.repository.UserRepository;
import br.com.litecode.security.UserSessionTracker;
import com.google.common.io.ByteStreams;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.subject.Subject;
import org.omnifaces.util.Faces;
import org.omnifaces.util.Messages;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.enterprise.context.RequestScoped;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

@RequestScoped
@Component
@Slf4j
public class LoginController {
	@Autowired
	private UserRepository userRepository;

	@Autowired
	private UserSessionTracker userSessionTracker;

	private String username;
	private String password;

	public void login() {
		Subject currentUser = SecurityUtils.getSubject();
		UsernamePasswordToken token = new UsernamePasswordToken(username, password);

		try {
			currentUser.login(token);
			User user = userRepository.findUserByUsername(username);

			HttpSession userSession = userSessionTracker.getUserSession(user);
			if (user.getRole() != Role.DEV && userSession != null && !userSession.getId().equals(Faces.getSessionId())) {
				userSessionTracker.killUserSession(user);
			}

			user.setLastAccess(Instant.now());
			user.setSessionId(Faces.getSessionId());

			String lastAccessLocation = getLastAccessLocation();
			if (lastAccessLocation != null) {
				user.setLastAccessLocation(lastAccessLocation);
				log.debug("Last access location: {}", user.getLastAccessLocation());
			}

			ZoneId timeZone = Faces.getSessionAttribute("timeZone");
			if (timeZone != null) {
				user.setTimeZone(timeZone.getId());
			} else {
				user.setTimeZone(ZoneId.systemDefault().getId());
			}

			userRepository.save(user);
			userSessionTracker.addUserSession(user, Faces.getSession());
			Faces.getSessionMap().put("loggedUser", user);
			Faces.redirect("");

			log.info("User {} logged in successfully", user.getUsername());
		} catch (AuthenticationException e) {
			Messages.addGlobalError("error.login");
			log.warn("Unable to authenticate", e);
		} catch (IOException e) {
			Messages.addGlobalError(e.getMessage());
		}
	}

	private String getLastAccessLocation() {
		try {
			URL url = new URL("https://ipinfo.io/" + Faces.getRemoteAddr() + "/json");
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			connection.setRequestMethod("GET");
			return new String(ByteStreams.toByteArray(connection.getInputStream()));
		} catch (Exception e) {
			log.warn("Unable to retrieve IP geo location for {}", Faces.getRemoteAddr());
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

	public void setClientTimeZone() {
		String clientTimeZone = Faces.getRequestParameter("clientTimeZone");

		if (clientTimeZone == null) {
			return;
		}

		int timeZoneOffset = Integer.parseInt(clientTimeZone);
		if (timeZoneOffset >= 0) {
			clientTimeZone = "+" + timeZoneOffset;
		}

		ZoneId timeZone = ZoneId.of("GMT" + clientTimeZone);
		Faces.getSessionMap().put("timeZone", timeZone);
		log.info("Client time zone: {}", timeZone);
	}

	public boolean hasDevRights() {
		return Faces.isUserInRole(Role.DEV.name());
	}

	public boolean hasAdminRights() {
		return Faces.isUserInRole(Role.ADMIN.name()) || hasDevRights();
	}

	public boolean isNewVersion() {
		LocalDate versionDate = Faces.getApplicationAttribute("versionDate");
		return LocalDate.now().isBefore(versionDate.plusDays(3));
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
}
