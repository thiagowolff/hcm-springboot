package br.com.litecode.security;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.servlet.http.HttpSession;

import br.com.litecode.domain.User;
import br.com.litecode.service.UserService;
import org.primefaces.push.EventBusFactory;

@ApplicationScoped
public class UserSessionTracker {
	@Inject private UserService userService;
	
	private Map<User, HttpSession> userSessions;
	
	@PostConstruct
	public void init() {
		userSessions = new ConcurrentHashMap<>();
		userService.initializeUserSessions();
	}

	public HttpSession getUserSession(User user) {
		return userSessions.get(user);
	}

	public void addUserSession(User user, HttpSession session) {
		userSessions.put(user, session);
	}
	
	public synchronized void killUserSession(User user) {
		HttpSession userSession = userSessions.get(user);
		user.setSessionId(null);
		userService.updateUser(user);
		userSessions.remove(user);
		userSession.invalidate();
		EventBusFactory.getDefault().eventBus().publish("/refresh", "{}");
	}
}
