package br.com.litecode.controller;

import br.com.litecode.application.SessionTimer;
import br.com.litecode.application.push.NotificationMessage;
import br.com.litecode.domain.ChamberEvent.EventType;
import br.com.litecode.domain.Patient;
import br.com.litecode.domain.Session;
import br.com.litecode.domain.Session.SessionStatus;
import br.com.litecode.domain.Session.TimePeriod;
import br.com.litecode.service.PatientService;
import br.com.litecode.service.SessionService;
import br.com.litecode.util.MessageUtil;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.joda.time.LocalTime;
import org.omnifaces.util.Messages;
import org.primefaces.push.EventBus;
import org.primefaces.push.EventBusFactory;

import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.groupingBy;

@Named
@ViewScoped
public class SessionManager implements Serializable {
	@Inject private SessionService sessionService;
	@Inject private PatientService patientService;
	@Inject private SessionTimer sessionTimer;
	@Inject private SessionTracker sessionTracker;

	private Session session;
	private Date sessionDate;
	private Date previousDailySessionsDate;

	public SessionManager() {
		session = new Session();
		sessionDate = new Date();
	}

	public Map<TimePeriod, List<Session>> getSessions(Integer chamberId) {
		List<Session> sessions = sessionService.getSessions(chamberId, sessionDate);
		return sessions.stream().collect(groupingBy(Session::getTimePeriod, TreeMap::new, Collectors.toList()));
	}

	public void addSession() {
		int sessionDuration = session.getChamber().getChamberEvent(EventType.COMPLETION).getTimeout();
		LocalDateTime sessionTime = LocalDate.fromDateFields(sessionDate).toLocalDateTime(LocalTime.fromDateFields(session.getSessionTime()));
		Date endTime = sessionTime.plusMillis(sessionDuration).toDate();

		List<Session> sessions = sessionService.getSessionsByPeriod(session.getChamber().getChamberId(), sessionTime.toDate());
		if (!sessions.isEmpty()) {
			Messages.addGlobalError(MessageUtil.getMessage("error.sessionAlreadyCreatedForPeriod"));
			return;
		}

		session.setSessionTime(sessionTime.toDate());
		session.setStartTime(session.getSessionTime());
		session.setEndTime(endTime);
		sessionService.createSession(session);

		String messageSummary = MessageUtil.getMessage("message.sessionCreatedSummary", session.getChamber().getChamberId(), session.getSessionId());
		String messageDetail = MessageUtil.getMessage("message.sessionCreatedDetail", session.getStartTime());

		EventBus eventBus = EventBusFactory.getDefault().eventBus();
		eventBus.publish("/notify", new NotificationMessage(session.getChamber().getChamberEvent(EventType.CREATION), messageSummary, messageDetail));

		initializeSession();
	}

	public void startSession(Session session) {
		LocalDateTime startTime = LocalDateTime.now();
		session.setStartTime(startTime.toDate());
		session.setEndTime(startTime.plusMillis(session.getChamber().getChamberEvent(EventType.COMPLETION).getTimeout()).toDate());
		session.setCurrentProgress(0);
		session.setStatus(SessionStatus.RUNNING);
		sessionService.updateSession(session);
		sessionTracker.registerActiveSession(session);
		sessionTimer.startSession(session);

		String messageSummary = MessageUtil.getMessage("message.sessionStartedSummary", session.getChamber().getChamberId(), session.getSessionId());
		String messageDetail = MessageUtil.getMessage("message.sessionStartedDetail", session.getStartTime(), session.getPatient().getName());

		EventBus eventBus = EventBusFactory.getDefault().eventBus();
		eventBus.publish("/notify", new NotificationMessage(session.getChamber().getChamberEvent(EventType.INITIATION), messageSummary, messageDetail));
	}

	public void resetSession(Session session) {
		session.setSessionTime(session.getSessionTime());
		session.setStartTime(session.getSessionTime());
		session.setEndTime(LocalDateTime.fromDateFields(session.getSessionTime()).plusMillis(session.getChamber().getChamberEvent(EventType.COMPLETION).getTimeout()).toDate());
		session.setStatus(SessionStatus.CREATED);
		session.setCurrentProgress(0);
		sessionTracker.removeActiveSession(session);
		sessionService.updateSession(session);
		initializeSession();
	}

	public void deleteSession() {
		sessionTracker.removeActiveSession(session);
		sessionService.deleteSession(session);
		initializeSession();
	}

	public void duplicateSessions() {
		sessionService.duplicateSessions(previousDailySessionsDate);
		initializeSession();
	}

	public void initializePreviousDailySessionsDate() {
		previousDailySessionsDate = LocalDate.now().minusDays(1).toDate();
	}

	private void initializeSession() {
		session = new Session();
	}

	public Session getSession() {
		return session;
	}

	public void setSession(Session session) {
		this.session = session;
	}

	public Date getSessionDate() {
		return sessionDate;
	}

	public void setSessionDate(Date sessionDate) {
		this.sessionDate = sessionDate;
	}

	public Date getPreviousDailySessionsDate() {
		return previousDailySessionsDate;
	}

	public void setPreviousDailySessionsDate(Date previousDailySessionsDate) {
		this.previousDailySessionsDate = previousDailySessionsDate;
	}
}
