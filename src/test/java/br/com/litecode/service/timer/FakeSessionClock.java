package br.com.litecode.service.timer;

import br.com.litecode.domain.model.Session;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

@Component
public class FakeSessionClock implements Clock<Session> {
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
	public void elapseTime(Session session) {
		Runnable task = sessionTasks.remove(0);
		if (task != null) {
			task.run();
		}
	}
}