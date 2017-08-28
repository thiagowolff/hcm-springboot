package br.com.litecode.security;

import br.com.litecode.domain.model.User;
import br.com.litecode.domain.repository.UserRepository;
import br.com.litecode.service.push.PushRefresh;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.web.context.WebApplicationContext;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpSession;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Scope(WebApplicationContext.SCOPE_APPLICATION)
@Component
public class UserSessionTracker {
	@Autowired private UserRepository userRepository;

	private Map<User, HttpSession> userSessions;
	
	@PostConstruct
	public void init() {
		userSessions = new ConcurrentHashMap<>();
		userRepository.initializeUserSessions();
	}

	public HttpSession getUserSession(User user) {
		return userSessions.get(user);
	}

	public void addUserSession(User user, HttpSession session) {
		userSessions.put(user, session);
	}
	
	@PushRefresh
	public synchronized void killUserSession(User user) {
		HttpSession userSession = userSessions.get(user);
		user.setSessionId(null);
		userRepository.save(user);
		userSessions.remove(user);
		userSession.invalidate();
	}
}
