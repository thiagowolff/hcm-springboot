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
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.time.LocalTime;
import java.util.List;

@Service
@Slf4j
public class ChamberSessionTimer implements SessionTimer, ChamberSessionTimerMBean {
	@Autowired
	private SessionRepository sessionRepository;

	@Autowired
	private PushService pushService;

	@Autowired
	@Getter
	private Clock<Session> sessionClock;

	@PostConstruct
	private void init() {
		JmxUtil.registerMBean(this, "SessionTimer");
		initializeAlarms();
	}

	@Override
	public void startSession(Session session) {
		List<ChamberEvent> chamberEvents = session.getChamber().getChamberEvents();

		for (ChamberEvent chamberEvent : chamberEvents) {
			if (session.getElapsedSeconds() > chamberEvent.getTimeout()) {
				continue;
			}

			long delay = chamberEvent.getTimeout() - session.getElapsedSeconds();
			sessionClock.register(session, () -> sessionTimeout(session, chamberEvent), delay);
			log.debug("Chamber event {} scheduled: {}s", chamberEvent, delay);
		}

		sessionClock.start(session);
	}

	private void sessionTimeout(Session session, ChamberEvent chamberEvent) {
		session = sessionRepository.findOne(session.getSessionId());
		session.setStatus(chamberEvent.getEventType().getSessionStatus());
		sessionRepository.save(session);

		if (session.getStatus() == SessionStatus.FINISHED) {
			sessionClock.stop(session);
		}

		pushService.publish(PushChannel.NOTIFY,  NotificationMessage.create(session, chamberEvent.toString()), session.getContextData().getStartedBy());
	}

	@Override
	public void stopSession(Session session) {
		sessionClock.stop(session);
	}

	@Scheduled(fixedRate = 1000)
	private void clockTimeout() {
		for (Session session : sessionClock.getActiveListeners()) {
			session.updateProgress();
			pushService.publish(PushChannel.PROGRESS, ProgressMessage.create(session), session.getContextData().getStartedBy());
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

