package br.com.litecode.security;

import br.com.litecode.domain.model.User;
import br.com.litecode.domain.repository.UserRepository;
import br.com.litecode.service.push.PushChannel;
import br.com.litecode.service.push.PushService;
import lombok.extern.java.Log;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.web.authentication.rememberme.PersistentTokenRepository;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import java.io.Serializable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@ApplicationScoped
@Component
public class UserSessionTracker implements Serializable {
	@Autowired
	private UserRepository userRepository;

    @Autowired
    private PersistentTokenRepository persistentTokenRepository;

	@Autowired
	private PushService pushService;

    @Autowired
    private SessionRegistry sessionRegistry;

	private Map<User, String> userSessions;

	@PostConstruct
	public void init() {
		userSessions = new ConcurrentHashMap<>();
		userRepository.initializeUserSessions();
	}

	public String getUserSession(User user) {
		return userSessions.get(user);
	}

	public void addUserSession(User user, String sessionId) {
		userSessions.put(user, sessionId);
		pushService.publish(PushChannel.UPDATE, "", null);
	}

	public void removeUserSession(User user) {
        user.setSessionId(null);
        userRepository.save(user);
        userSessions.remove(user);
	}

	public int getOnlineUsers() {
		return userSessions.size();
	}
	
	public synchronized void killUserSession(User user) {
        String sessionId = userSessions.get(user);

        for (Object principal : sessionRegistry.getAllPrincipals()) {
            UserPrincipal userPrincipal = (UserPrincipal) principal;

            if (userPrincipal.getUser().getSessionId() == null) {
                continue;
            }

            if (userPrincipal.getUser().getSessionId().equals(sessionId)) {
                sessionRegistry.getSessionInformation(sessionId).expireNow();
            }
        }

        removeUserSession(user);
		persistentTokenRepository.removeUserTokens(user.getUsername());
        pushService.publish(PushChannel.UPDATE, "", null);

        log.info("User session killed: {}", user.getUsername());
    }
}
