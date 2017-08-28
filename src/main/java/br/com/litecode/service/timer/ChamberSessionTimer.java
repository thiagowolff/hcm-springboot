package br.com.litecode.service.timer;

import br.com.litecode.domain.model.ChamberEvent;
import br.com.litecode.domain.model.Session;
import br.com.litecode.domain.model.Session.SessionStatus;
import br.com.litecode.domain.repository.SessionRepository;
import br.com.litecode.service.push.PushChannel;
import br.com.litecode.service.push.PushService;
import br.com.litecode.service.push.message.NotificationMessage;
import br.com.litecode.service.push.message.ProgressMessage;
import br.com.litecode.util.JmxUtil;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ScheduledFuture;

@Service
@Slf4j
public class ChamberSessionTimer implements SessionTimer, ChamberSessionTimerMBean {

	@Autowired
	private ActiveSessionHolder activeSessionHolder;

	@Autowired
	private SessionRepository sessionRepository;

	@Autowired
	private TaskScheduler taskScheduler;

	@Autowired
	private PushService pushService;

	@PostConstruct
	private void init() {
		JmxUtil.registerMBean(this, "SessionTimer");
		initializeAlarms();
	}

	@Override
	public void startSession(Session session) {
		if (activeSessionHolder.exists(session)) {
			log.warn("Session {} already started", session.getSessionId());
			return;
		}

		List<ChamberEvent> chamberEvents = session.getChamber().getChamberEvents();
		List<ScheduledFuture> scheduledFutures = new ArrayList<>();

		for (ChamberEvent chamberEvent : chamberEvents) {
			if (chamberEvent.getTimeout() > 0) {
				Date startTime = Date.from(Instant.now().plusSeconds(chamberEvent.getTimeout()));
				scheduledFutures.add(taskScheduler.schedule(() -> sessionTimeout(session, chamberEvent), startTime));
			}
		}
		activeSessionHolder.addSession(session, scheduledFutures);
	}

	private void sessionTimeout(Session session, ChamberEvent chamberEvent) {
		session.setStatus(chamberEvent.getEventType().getSessionStatus());
		sessionRepository.save(session);

		if (session.getStatus() == SessionStatus.FINISHED) {
			activeSessionHolder.removeSession(session);
		}

		pushService.publish(PushChannel.NOTIFY,  NotificationMessage.create(session, chamberEvent.getEventType()), session.getManagedBy());
	}

	@Override
	public void stopSession(Session session) {
		List<ScheduledFuture> scheduledTasks = activeSessionHolder.getScheduledTasks(session);

		if (scheduledTasks != null) {
			scheduledTasks.forEach(task -> task.cancel(true));
			activeSessionHolder.removeSession(session);
		}
	}

	@Scheduled(fixedRate = 1000)
	private void clockTimeout() {
		for (Session session : activeSessionHolder.getSessions()) {
			session.updateProgress();
			pushService.publish(PushChannel.PROGRESS, ProgressMessage.create(session), session.getManagedBy());
		}
	}

	@Override
	public void initializeAlarms() {

	}

	@Override
	public void pushAlarm(String name, String message) {

	}

	@Override
	public int getNumberOfActiveTimers() {
		return 0;
	}
}

