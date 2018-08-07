package br.com.litecode.security;

import br.com.litecode.domain.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;

import javax.servlet.annotation.WebListener;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

@WebListener
public class UserSessionListener implements HttpSessionListener {
	@Autowired
	private UserSessionTracker userSessionTracker;

	@Autowired
	private UserRepository userRepository;
	
	@Override
	public void sessionCreated(HttpSessionEvent sessionEvent) {
	}

	@Override
	public void sessionDestroyed(HttpSessionEvent sessionEvent) {
//		try {
//			User user = userRepository.findUserBySessionId(sessionEvent.getSession().getId());
//			if (user != null) {
//				userSessionTracker.getUserSession(user).getSession().stop();
//			}
//		} catch(Exception e) {
//			//ignore
//		}
	}
}
