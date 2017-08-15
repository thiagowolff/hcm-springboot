package br.com.litecode.security;

import javax.inject.Inject;
import javax.servlet.annotation.WebListener;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

import br.com.litecode.domain.User;
import br.com.litecode.service.UserService;

@WebListener
public class UserSessionListener implements HttpSessionListener {
	@Inject private UserSessionTracker userSessionTracker;
	@Inject private UserService userService;
	
	@Override
	public void sessionCreated(HttpSessionEvent sessionEvent) {
	}

	@Override
	public void sessionDestroyed(HttpSessionEvent sessionEvent) {
		try {
			User user = userService.getUserBySessionId(sessionEvent.getSession().getId());
			if (user != null) {
				userSessionTracker.killUserSession(user);
			}	
		} catch(Exception e) {
			//ignore
		}
	}
}
