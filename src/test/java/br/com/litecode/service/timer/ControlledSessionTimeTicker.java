package br.com.litecode.service.timer;

import br.com.litecode.domain.model.Session;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

@Component
public class ControlledSessionTimeTicker implements TimeTicker<Session> {
	private List<Runnable> sessionTasks = new ArrayList<>();

	@Override
	public void register(Session session, Runnable task, long delay) {
		sessionTasks.add(task);
	}

	@Override
	public void start(Session session) {
	}

	@Override
	public void stop(Session session) {
	}

	@Override
	public Set<Session> getActiveListeners() {
		return Collections.emptySet();
	}

	@Override
	public void elapseTime(Session session, int secondsToElapse) {
		Runnable task = sessionTasks.remove(0);
		if (task != null) {
			if (session.getNextChamberEvent() != null) {
				session.setClock(getFixedClockAt(session.getScheduledTime().plusSeconds(secondsToElapse)));
			}
			task.run();
		}
	}

	private Clock getFixedClockAt(LocalDateTime dateTime){
		ZoneId zoneId = ZoneId.systemDefault();
		Instant instant = dateTime.atZone(zoneId).toInstant();
		return Clock.fixed(instant, zoneId);
	}
}