package br.com.litecode.security;

import br.com.litecode.domain.model.User;
import br.com.litecode.domain.repository.UserRepository;
import br.com.litecode.service.push.PushChannel;
import br.com.litecode.service.push.PushService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.servlet.http.HttpSession;
import java.io.Serializable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@ApplicationScoped
@Component
public class UserSessionTracker implements Serializable {
	@Autowired
	private UserRepository userRepository;

	@Autowired
	private PushService pushService;

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
		pushService.publish(PushChannel.UPDATE, "", null);
	}

	public void removeUserSession(User user) {
		userSessions.remove(user);
		pushService.publish(PushChannel.UPDATE, "", null);
	}

	public int getOnlineUsers() {
		return userSessions.size();
	}
	
	public synchronized void killUserSession(User user) {
//		HttpSession session = userSessions.get(user);
//		user.setSessionId(null);
//		userRepository.save(user);
//		removeUserSession(user);
//		subject.logout();
	}
}
