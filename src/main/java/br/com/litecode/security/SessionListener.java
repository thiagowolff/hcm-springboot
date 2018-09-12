package br.com.litecode.security;

import br.com.litecode.domain.model.User;
import br.com.litecode.domain.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import javax.servlet.annotation.WebListener;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

@WebListener
@Slf4j
public class SessionListener implements HttpSessionListener {
	@Autowired
	private UserSessionTracker userSessionTracker;

	@Autowired
	private UserRepository userRepository;
	
	@Override
	public void sessionCreated(HttpSessionEvent sessionEvent) {
	}

	@Override
	public void sessionDestroyed(HttpSessionEvent sessionEvent) {
		try {
			User user = userRepository.findUserBySessionId(sessionEvent.getSession().getId());
			if (user != null && user.getSessionId() != null) {
				userSessionTracker.removeUserSession(user);
			}
		} catch(Exception e) {
			log.warn("Unable to remove tracked user", e);
		}
	}
}
