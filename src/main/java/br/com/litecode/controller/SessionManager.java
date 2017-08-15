package br.com.litecode.controller;

import br.com.litecode.application.SessionTimer;
import br.com.litecode.application.push.NotificationMessage;
import br.com.litecode.domain.Chamber;
import br.com.litecode.domain.ChamberEvent.EventType;
import br.com.litecode.domain.Patient;
import br.com.litecode.domain.PatientSession;
import br.com.litecode.domain.Session;
import br.com.litecode.domain.Session.SessionStatus;
import br.com.litecode.domain.Session.TimePeriod;
import br.com.litecode.service.PatientService;
import br.com.litecode.service.SessionService;
import br.com.litecode.util.MessageUtil;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.joda.time.LocalTime;
import org.omnifaces.util.Messages;
import org.primefaces.push.EventBus;
import org.primefaces.push.EventBusFactory;

import javax.annotation.PostConstruct;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.groupingBy;

@Named
@ViewScoped
public class SessionManager implements Serializable {
	@Inject private SessionService sessionService;
	@Inject private PatientService patientService;
	@Inject private SessionTimer sessionTimer;
	@Inject private SessionTracker sessionTracker;

	private Chamber selectedChamber;
	private Date selectedTime;
	private Session selectedSession;
	private List<Patient> patients;
	private Date sessionDate;
	private Date previousDailySessionsDate;
	private Date newDailySessionsDate;
	private List<Date> sessionDates;

	public SessionManager() {
		selectedChamber = new Chamber();
		patients = new ArrayList<>();
		sessionDate = new Date();
	}

	@PostConstruct
	private void init() {
		sessionDates = sessionService.loadSessionDates();
	}

	public Map<TimePeriod, List<Session>> getSessions(Integer chamberId) {
		List<Session> sessions = sessionService.getSessions(chamberId, sessionDate);
		return sessions.stream().collect(groupingBy(Session::getTimePeriod, TreeMap::new, Collectors.toList()));
	}

	public void addSession() {
		int sessionDuration = selectedChamber.getChamberEvent(EventType.COMPLETION).getTimeout();
		LocalDateTime sessionTime = LocalDate.fromDateFields(sessionDate).toLocalDateTime(LocalTime.fromDateFields(selectedTime));
		Date endTime = sessionTime.plusMillis(sessionDuration).toDate();

		List<Session> sessions = sessionService.getSessionsByPeriod(selectedChamber.getChamberId(), sessionTime.toDate());
		if (!sessions.isEmpty()) {
			Messages.addGlobalError(MessageUtil.getMessage("error.sessionAlreadyCreatedForPeriod"));
			return;
		}

		if (patients.size() > selectedChamber.getCapacity()) {
			Messages.addGlobalError(MessageUtil.getMessage("error.chamberPatientsLimitExceeded", selectedChamber.getCapacity()));
			return;
		}

		Session session = new Session();
		session.setChamber(selectedChamber);
		session.setScheduledTime(sessionTime.toDate());
		session.setStartTime(session.getScheduledTime());
		session.setEndTime(endTime);

		sessionService.createSession(session, patients);

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
		session.setStatus(SessionStatus.COMPRESSING);
		sessionService.updateSession(session);
		sessionTracker.registerActiveSession(session);
		sessionTimer.startSession(session);

		String messageSummary = MessageUtil.getMessage("message.sessionStartedSummary", session.getChamber().getChamberId(), session.getSessionId());
		String messageDetail = MessageUtil.getMessage("message.sessionStartedDetail", session.getStartTime());

		EventBusFactory.getDefault().eventBus().publish("/notify", new NotificationMessage(session.getChamber().getChamberEvent(EventType.START), messageSummary, messageDetail));
	}

	public void resetSession(Session session) {
		session.setScheduledTime(session.getScheduledTime());
		session.setStartTime(session.getScheduledTime());
		session.setEndTime(LocalDateTime.fromDateFields(session.getScheduledTime()).plusMillis(session.getChamber().getChamberEvent(EventType.COMPLETION).getTimeout()).toDate());
		session.setStatus(SessionStatus.CREATED);
		session.setCurrentProgress(0);
		sessionTracker.removeActiveSession(session);
		sessionService.updateSession(session);
		initializeSession();
		EventBusFactory.getDefault().eventBus().publish("/refresh", "{}");
	}

	public void deleteSession() {
		sessionTracker.removeActiveSession(selectedSession);
		sessionService.deleteSession(selectedSession);
		initializeSession();
		EventBusFactory.getDefault().eventBus().publish("/refresh", "{}");
	}

	public void setPatientSessionStatus(PatientSession patientSession, boolean absent) {
		patientSession.setAbsent(absent);
		sessionService.updatePatientSession(patientSession);
		EventBusFactory.getDefault().eventBus().publish("/refresh", "{}");
	}

	public void addPatientsToSession() {
		if (patients.size() + selectedSession.getPatientSessions().size() > selectedSession.getChamber().getCapacity()) {
			Messages.addGlobalError(MessageUtil.getMessage("error.chamberPatientsLimitExceeded", selectedSession.getChamber().getCapacity()));
			return;
		}

		sessionService.addPatientsToSession(selectedSession, patients);
		selectedSession = sessionService.getSession(selectedSession.getSessionId());
		patients.clear();
		EventBusFactory.getDefault().eventBus().publish("/refresh", "{}");
	}

	public void removePatientFromSession(PatientSession patientSession) {
		sessionService.deletePatientSession(patientSession);
		selectedSession = sessionService.getSession(patientSession.getSession().getSessionId());
		EventBusFactory.getDefault().eventBus().publish("/refresh", "{}");
	}

	public void duplicateSessions() {
		sessionService.duplicateSessions(previousDailySessionsDate, newDailySessionsDate);
		initializeSession();
		EventBusFactory.getDefault().eventBus().publish("/refresh", "{}");
	}

	public void previousSessionDate() {
		sessionDate = LocalDate.fromDateFields(sessionDate).minusDays(1).toDate();
		initializeSession();
	}

	public void nextSessionDate() {
		sessionDate = LocalDate.fromDateFields(sessionDate).plusDays(1).toDate();
		initializeSession();
	}

	public Long[] getChamberOccupationData(Session session) {
		if (session == null) {
			return new Long[0];
		}

		Long[] chamberOccupation = new Long[session.getChamber().getCapacity()];
		int i = 0;

		for (PatientSession patientSession : session.getPatientSessions()) {
			chamberOccupation[i] = patientSession.isAbsent() ? 2L : 1L;
			i++;
		}

		for (int j = i; j < chamberOccupation.length; j++) {
			chamberOccupation[j] = 0L;
		}

		return chamberOccupation;
	}

	public void initializePreviousDailySessionsDate() {
		previousDailySessionsDate = LocalDate.now().minusDays(1).toDate();
		newDailySessionsDate = LocalDate.now().toDate();
	}

	private void initializeSession() {
		selectedSession = new Session();
		patients.clear();
	}

	public Session getSelectedSession() {
		return selectedSession;
	}

	public void setSelectedSession(Session selectedSession) {
		this.selectedSession = selectedSession;
	}

	public Date getSelectedTime() {
		return selectedTime;
	}

	public void setSelectedTime(Date selectedTime) {
		this.selectedTime = selectedTime;
	}

	public Chamber getSelectedChamber() {
		return selectedChamber;
	}

	public void setSelectedChamber(Chamber selectedChamber) {
		this.selectedChamber = selectedChamber;
	}

	public List<Patient> getPatients() {
		return patients;
	}

	public void setPatients(List<Patient> patients) {
		this.patients = patients;
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

	public Date getNewDailySessionsDate() {
		return newDailySessionsDate;
	}

	public void setNewDailySessionsDate(Date newDailySessionsDate) {
		this.newDailySessionsDate = newDailySessionsDate;
	}

	public String getSessionDates() {
		List<String> dates = sessionDates.stream().map(d -> LocalDate.fromDateFields(d).toString()).collect(Collectors.toList());
		return new Gson().toJson(dates);
	}
}
