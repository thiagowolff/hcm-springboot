package br.com.litecode.controller;

import br.com.litecode.domain.model.User;
import br.com.litecode.domain.model.User.Role;
import br.com.litecode.domain.repository.UserRepository;
import br.com.litecode.security.UserSessionTracker;
import br.com.litecode.service.PushoverService;
import com.google.common.io.ByteStreams;
import lombok.extern.slf4j.Slf4j;
import net.pushover.client.PushoverClient;
import net.pushover.client.PushoverException;
import net.pushover.client.PushoverMessage;
import net.pushover.client.PushoverRestClient;
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
import java.util.TimeZone;

@RequestScoped
@Component
@Slf4j
public class LoginController {
	@Autowired
	private UserRepository userRepository;

	@Autowired
	private UserSessionTracker userSessionTracker;

	@Autowired
	private PushoverService pushoverService;

	private String username;
	private String password;
	private boolean rememberMe;

	public void login() {
		Subject currentUser = SecurityUtils.getSubject();
		UsernamePasswordToken token = new UsernamePasswordToken(username, password, rememberMe);

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
				user.setTimeZone(getDefaultTimeZone());
			}

			sendLoginNotification(user);

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

	public void checkIfAlreadyLoggedIn() throws IOException {
        Subject loggedUser = SecurityUtils.getSubject();
        if (loggedUser != null && (loggedUser.isAuthenticated() || loggedUser.isRemembered())) {
            Faces.redirect("");
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

	private void sendLoginNotification(User user) {
		try {
			pushoverService.sendNotification("User " + user.getUsername() + " logged in" + (rememberMe ? " [rememberMe] " : ""));
		} catch (PushoverException e) {
			log.error("Unable to send login notification", e);
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

	private String getDefaultTimeZone() {
		int offsetInMillis = TimeZone.getDefault().getRawOffset();
		String offset = String.format("%02d:%02d", Math.abs(offsetInMillis / 3600000), Math.abs((offsetInMillis / 60000) % 60));
		return "GMT" + (offsetInMillis >= 0 ? "+" : "-") + offset;
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

    public boolean isRememberMe() {
        return rememberMe;
    }

    public void setRememberMe(boolean rememberMe) {
        this.rememberMe = rememberMe;
    }
}
