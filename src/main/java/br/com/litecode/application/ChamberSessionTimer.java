package br.com.litecode.application;

import br.com.litecode.application.push.NotificationMessage;
import br.com.litecode.application.push.SessionProgressMessage;
import br.com.litecode.controller.SessionTracker;
import br.com.litecode.domain.Alarm;
import br.com.litecode.domain.ChamberEvent;
import br.com.litecode.domain.ChamberEvent.EventType;
import br.com.litecode.domain.Session;
import br.com.litecode.domain.Session.SessionStatus;
import br.com.litecode.service.SessionService;
import br.com.litecode.util.JMXUtil;
import br.com.litecode.util.MessageUtil;
import com.google.common.base.CaseFormat;
import com.google.common.base.Splitter;
import org.joda.time.LocalDateTime;
import org.primefaces.push.EventBus;
import org.primefaces.push.EventBusFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ejb.*;
import javax.inject.Inject;
import java.io.Serializable;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

@Singleton
@Startup
public class ChamberSessionTimer implements SessionTimer, ChamberSessionTimerMBean {
	private static final Logger logger = LoggerFactory.getLogger("chapters.introduction.HelloWorld1");

	@Resource TimerService timerService;
	@Inject	private SessionTracker sessionTracker;
	@Inject	private SessionService sessionService;

	@PostConstruct
	private void init() {
		JMXUtil.registerMBean(this, "SessionTimer");
		initializeAlarms();
	}

	@Override
	public void initializeAlarms() {
		List<Alarm> alarms = sessionService.getAlarms();

		timerService.getTimers().forEach(timer -> {
			if (timer.getInfo() instanceof Alarm) {
				timer.cancel();
			}
		});

		for (Alarm alarm : alarms) {
			Iterable<String> expressionValues = Splitter.on(" ").split(alarm.getCronExpression());
			Iterator<String> it = expressionValues.iterator();

			ScheduleExpression scheduleExpression = new ScheduleExpression();
			scheduleExpression.second(it.next());
			scheduleExpression.minute(it.next());
			scheduleExpression.hour(it.next());
			scheduleExpression.dayOfWeek(it.hasNext() ? it.next() : "*");
			scheduleExpression.dayOfMonth(it.hasNext() ? it.next() : "*");
			scheduleExpression.month(it.hasNext() ? it.next() : "*");
			scheduleExpression.year(it.hasNext() ? it.next() : "*");

			TimerConfig timerConfig = new TimerConfig(alarm, false);
			timerService.createCalendarTimer(scheduleExpression, timerConfig);

			logger.info("Alarm {} initialized.", alarm);
		}
	}

	@Override
	public void pushAlarm(String name, String message) {
		Alarm alarm = new Alarm(name, message, "coins.mp3");

		EventBus eventBus = EventBusFactory.getDefault().eventBus();
		eventBus.publish("/alarm", alarm);
	}

	@Override
	public int getNumberOfActiveTimers() {
		return timerService.getTimers().size();
	}

	@Override
	public void startSession(Session session) {
		List<ChamberEvent> chamberEvents = session.getChamber().getChamberEvents();

		for (ChamberEvent chamberEvent : chamberEvents) {
			if (chamberEvent.getTimeout() > 0) {
				TimerConfig timerConfig = new TimerConfig(new SessionTimerConfig(session.getSessionId(), chamberEvent), false);
				timerService.createSingleActionTimer(chamberEvent.getTimeout(), timerConfig);
			}
		}
	}

	@Timeout
	private void timeout(Timer timer) {
		if (timer.isCalendarTimer()) {
			alarmTimeout(timer);
		} else {
			sessionTimeout(timer);
		}
	}

	private void alarmTimeout(Timer timer) {
		Alarm alarm = (Alarm) timer.getInfo();

		EventBus eventBus = EventBusFactory.getDefault().eventBus();
		eventBus.publish("/alarm", alarm);
	}

	private void sessionTimeout(Timer timer) {
		SessionTimerConfig timerConfig = (SessionTimerConfig) timer.getInfo();
		Session session = sessionTracker.getSession(timerConfig.getSessionId());

		if (session == null) {
			timer.cancel();
			return;
		}

		handleSessionEventNotification(timerConfig.getChamberEvent(), session);

		Session currentSession = sessionService.getSession(session.getSessionId());
		currentSession.setStatus(session.getStatus());
		sessionService.updateSession(currentSession);
	}

	private void handleSessionEventNotification(ChamberEvent chamberEvent, Session session) {
		switch (chamberEvent.getEventType()) {
			case WEAR_MASK:
				session.setStatus(SessionStatus.O2_ON);
				break;
			case REMOVE_MASK:
				session.setStatus(SessionStatus.O2_OFF);
				break;
			case SHUTDOWN:
				session.setStatus(SessionStatus.SHUTTING_DOWN);
				break;
			case COMPLETION:
				session.setStatus(SessionStatus.FINISHED);
				sessionTracker.removeActiveSession(session);
		}

		Date eventTime = LocalDateTime.fromDateFields(session.getStartTime()).plusMillis(chamberEvent.getTimeout()).toDate();
		String eventType = CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL, chamberEvent.getEventType().toString());

		String messageSummary = MessageUtil.getMessage(String.format("message.session%sSummary", eventType), session.getChamber().getChamberId(), session.getSessionId());
		String messageDetail = MessageUtil.getMessage(String.format("message.session%sDetail", eventType), eventTime);

		EventBus eventBus = EventBusFactory.getDefault().eventBus();
		eventBus.publish("/notify", new NotificationMessage(chamberEvent, messageSummary, messageDetail));
	}

	@Schedule(second = "*/1", minute = "*", hour = "*", persistent = false)
	private void clockTimeout() {
		timerService.getTimers().forEach(timer -> {
			if (!timer.isCalendarTimer()) {
				SessionTimerConfig timerConfig = (SessionTimerConfig) timer.getInfo();

				if (timerConfig.getChamberEvent().getEventType() == EventType.COMPLETION) {
					Session session = sessionTracker.getSession(timerConfig.getSessionId());

					if (session == null) {
						timer.cancel();
						return;
					}

					session.updateProgress(timer.getTimeRemaining());

					EventBus eventBus = EventBusFactory.getDefault().eventBus();
					eventBus.publish("/progress", SessionProgressMessage.create(session));
				}
			}
		});
	}

	private static class SessionTimerConfig implements Serializable {
		private Integer sessionId;
		private ChamberEvent chamberEvent;

		public SessionTimerConfig(Integer sessionId, ChamberEvent chamberEvent) {
			this.sessionId = sessionId;
			this.chamberEvent = chamberEvent;
		}

		public Integer getSessionId() {
			return sessionId;
		}

		public ChamberEvent getChamberEvent() {
			return chamberEvent;
		}
	}
}

