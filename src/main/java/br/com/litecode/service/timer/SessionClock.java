package br.com.litecode.service.timer;

import br.com.litecode.domain.model.Session;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;

@Component
@Slf4j
public class SessionClock implements Clock<Session> {
	@Autowired
	private TaskScheduler taskScheduler;

	private Map<Session, List<SessionTask>> sessionTasks = new ConcurrentHashMap<>();
	private Map<Session, List<ScheduledFuture>> activeSessions = new ConcurrentHashMap<>();

	@Override
	public void register(Session session, Runnable task, long delay) {
		sessionTasks.computeIfAbsent(session, k -> new ArrayList<>()).add(SessionTask.of(task, delay));
	}

	@Override
	public synchronized void start(Session session) {
		if (activeSessions.containsKey(session)) {
			log.warn("Session {} already started", session.getSessionId());
			return;
		}

		for (SessionTask sessionTask : sessionTasks.get(session)) {
			Date startTime = Date.from(Instant.now().plusSeconds(sessionTask.delay));
			activeSessions.computeIfAbsent(session, k -> new ArrayList<>()).add(taskScheduler.schedule(sessionTask.task, startTime));
		}

		log.info("Session {} started with {} scheduled events", session.getSessionId(), sessionTasks.size());
	}

	@Override
	public synchronized void stop(Session session) {
		if (!activeSessions.containsKey(session)) {
			log.warn("Session {} is not running", session.getSessionId());
			return;
		}

		sessionTasks.remove(session);
		activeSessions.get(session).forEach(task -> task.cancel(true));
		activeSessions.remove(session);
	}

	@Override
	public Set<Session> getActiveListeners() {
		return activeSessions.keySet();
	}

	@AllArgsConstructor(staticName = "of")
	private static class SessionTask {
		private Runnable task;
		private long delay;
	}
}