package br.com.litecode.controller;

import br.com.litecode.domain.Session;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;
import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Named
@ApplicationScoped
public class SessionTracker {
	private ConcurrentMap<Integer, Session> activeSessions;

	@PostConstruct
	public void init() {
		activeSessions = new ConcurrentHashMap<>();
	}

	public void registerActiveSession(Session session) {
		activeSessions.putIfAbsent(session.getSessionId(), session);
	}

	public void removeActiveSession(Session session) {
		activeSessions.remove(session.getSessionId());
	}

	public Session getSession(Integer sessionId) {
		return activeSessions.get(sessionId);
	}
}
