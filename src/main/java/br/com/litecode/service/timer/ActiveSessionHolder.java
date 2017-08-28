package br.com.litecode.service.timer;

import br.com.litecode.domain.model.Session;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ScheduledFuture;

@Component
public class ActiveSessionHolder {
	private ConcurrentMap<Session, List<ScheduledFuture>> activeSessions;

	@PostConstruct
	private void init() {
		activeSessions = new ConcurrentHashMap<>();
	}

	public boolean exists(Session session) {
		return activeSessions.get(session) != null;
	}

	public Set<Session> getSessions() {
		return activeSessions.keySet();
	}

	public void addSession(Session session, List<ScheduledFuture> scheduledFutures) {
		activeSessions.put(session, scheduledFutures);
	}

	public void removeSession(Session session) {
		activeSessions.remove(session);
	}

	public List<ScheduledFuture> getScheduledTasks(Session session) {
		return activeSessions.get(session);
	}
}
