package br.com.litecode.controller;

import br.com.litecode.annotation.ScopeSession;
import br.com.litecode.domain.model.Chamber;
import br.com.litecode.domain.model.ChamberEvent.EventType;
import br.com.litecode.domain.model.Patient;
import br.com.litecode.domain.model.PatientSession;
import br.com.litecode.domain.model.Session;
import br.com.litecode.domain.model.Session.SessionStatus;
import br.com.litecode.domain.repository.SessionRepository;
import br.com.litecode.service.push.PushChannel;
import br.com.litecode.service.push.PushRefresh;
import br.com.litecode.service.push.PushService;
import br.com.litecode.service.push.message.NotificationMessage;
import br.com.litecode.service.timer.SessionTimer;
import br.com.litecode.util.MessageUtil;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.SecurityUtils;
import org.hibernate.Hibernate;
import org.omnifaces.util.Messages;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@ScopeSession
@Component
@Slf4j
public class SessionController implements Serializable {
	@Autowired
	private SessionRepository sessionRepository;

	@Autowired
	private SessionTimer sessionTimer;

	@Autowired
	private PushService pushService;

	private Map<String, List<Session>> chamberSessions;

	@Getter @Setter
	private SessionInput sessionInput;

	@Getter @Setter
	private LocalDate fromSessionsDate;

	@Getter @Setter
	private LocalDate toSessionsDate;

	private List<LocalDateTime> scheduledSessionDates;

	private enum ChamberPayloadStatus { AVAILABLE, OCCUPIED, ABSENT }

	@PostConstruct
	private void init() {
		sessionInput = new SessionInput();
		scheduledSessionDates = sessionRepository.findScheduledSessionDates();
		chamberSessions = new HashMap<>();
	}

	@Transactional(readOnly = true)
	public List<Session> getSessions(Integer chamberId) {
		String key = getSessionKey(chamberId);
		List<Session> cachedSessions = chamberSessions.get(key);
		if (cachedSessions == null || cachedSessions.isEmpty()) {
			log.debug("Cache miss for key {}, cache size: {}", key, chamberSessions.size());

			List<Session> sessions = sessionRepository.findSessionsByChamberAndDate(chamberId, sessionInput.getSessionDate());
			chamberSessions.put(key, sessions);
			sessions.forEach(s -> Hibernate.initialize(s.getPatientSessions()));
		}

		return chamberSessions.get(key);
	}

	private String getSessionKey(Integer chamberId) {
		return chamberId + "." + sessionInput.getSessionDate().toEpochDay();
	}

	@Transactional
	public void addSession() {
		int sessionDuration = sessionInput.getChamber().getChamberEvent(EventType.COMPLETION).getTimeout();
		LocalDateTime sessionTime = sessionInput.getSessionDate().atTime(sessionInput.getSessionTime());
		LocalTime endTime = sessionTime.plusSeconds(sessionDuration).toLocalTime();

		boolean isScheduled = sessionRepository.isSessionScheduled(sessionInput.getChamber().getChamberId(), sessionTime);
		if (isScheduled) {
			Messages.addGlobalError(MessageUtil.getMessage("error.sessionAlreadyCreatedForPeriod"));
			return;
		}

		if (sessionInput.getPatients().size() > sessionInput.getChamber().getCapacity()) {
			Messages.addGlobalError(MessageUtil.getMessage("error.chamberPatientsLimitExceeded", sessionInput.getChamber().getCapacity()));
			return;
		}

		Session session = new Session();
		session.setChamber(sessionInput.getChamber());
		session.setScheduledTime(sessionTime);
		session.setStartTime(session.getScheduledTime().toLocalTime());
		session.setEndTime(endTime);
		session.setManagedBy((String) SecurityUtils.getSubject().getPrincipal());
		sessionInput.getPatients().forEach(session::addPatient);
		sessionRepository.save(session);
		invalidateSessionCache();
		pushService.publish(PushChannel.NOTIFY,  NotificationMessage.create(session, EventType.CREATION), session.getManagedBy());
	}

	@Transactional
	public void startSession(Session session) {
		session = sessionRepository.findOne(session.getSessionId());
		session.reset();
		session.setStatus(SessionStatus.COMPRESSING);
		session.setManagedBy((String) SecurityUtils.getSubject().getPrincipal());

		sessionRepository.save(session);
		sessionTimer.startSession(session);
		chamberSessions.remove(getSessionKey(session.getChamber().getChamberId()));
		pushService.publish(PushChannel.NOTIFY,  NotificationMessage.create(session, EventType.START), session.getManagedBy());
	}

	@PushRefresh
	@Transactional
	@CacheEvict(cacheNames = "patientStats", allEntries = true)
	public void resetSession(Session session) {
		session = sessionRepository.findOne(session.getSessionId());
		sessionTimer.stopSession(session);
		session.reset();
		sessionRepository.save(session);
		chamberSessions.remove(getSessionKey(session.getChamber().getChamberId()));
	}

	@PushRefresh
	@Transactional
	@CacheEvict(cacheNames = "patientStats", allEntries = true)
	public void finishSession(Session session) {
		session = sessionRepository.findOne(session.getSessionId());
		sessionTimer.stopSession(session);
		session.reset();
		session.setStatus(SessionStatus.FINISHED);
		sessionRepository.save(session);
		chamberSessions.remove(getSessionKey(session.getChamber().getChamberId()));
	}

	@PushRefresh
	@Transactional
	@CacheEvict(cacheNames = "patientStats", allEntries = true)
	public void deleteSession() {
		sessionTimer.stopSession(sessionInput.getSession());
		sessionRepository.delete(sessionInput.getSession());
		chamberSessions.remove(getSessionKey(sessionInput.getSession().getChamber().getChamberId()));
	}

	@PushRefresh
	@Transactional
	public void setPatientSessionStatus(PatientSession patientSession, boolean absent) {
		patientSession.setAbsent(absent);
		sessionRepository.save(patientSession.getSession());
	}

	@PushRefresh
	@Transactional
	@CacheEvict(cacheNames = "patientStats", allEntries = true)
	public void addPatientsToSession() {
		if (sessionInput.getPatients().size() + sessionInput.getSession().getPatientSessions().size() > sessionInput.getSession().getChamber().getCapacity()) {
			Messages.addGlobalError(MessageUtil.getMessage("error.chamberPatientsLimitExceeded", sessionInput.getSession().getChamber().getCapacity()));
			return;
		}

		Session session = sessionRepository.findOne(sessionInput.getSession().getSessionId());
		sessionInput.getPatients().forEach(session::addPatient);
		sessionInput.setSession(sessionRepository.save(session));
		chamberSessions.remove(getSessionKey(session.getChamber().getChamberId()));
	}

	@PushRefresh
	@Transactional
	@CacheEvict(cacheNames = "patientStats", allEntries = true)
	public void removePatientFromSession(PatientSession patientSession) {
		sessionInput.getSession().getPatientSessions().remove(patientSession);
		sessionInput.setSession(sessionRepository.save(patientSession.getSession()));
	}

	@PushRefresh
	@Transactional
	public void duplicateSessions() {
		List<Session> fromSessions = sessionRepository.findSessionsByDate(fromSessionsDate);
		for (Session fromSession : fromSessions) {
			LocalTime time = fromSession.getScheduledTime().toLocalTime();
			LocalDateTime sessionTime = toSessionsDate.atTime(time);
			int sessionDuration = fromSession.getChamber().getChamberEvent(EventType.COMPLETION).getTimeout();

			Session session = new Session();
			session.setChamber(fromSession.getChamber());
			session.setScheduledTime(sessionTime);
			session.setStartTime(sessionTime.toLocalTime());
			session.setEndTime(sessionTime.plusSeconds(sessionDuration).toLocalTime());
			fromSession.getPatientSessions().forEach(ps -> session.addPatient(ps.getPatient()));
			sessionRepository.save(session);
		}
		invalidateSessionCache();
	}

	public void previousSessionDate() {
		sessionInput.setSessionDate(sessionInput.getSessionDate().minusDays(1));
	}

	public void nextSessionDate() {
		sessionInput.setSessionDate(sessionInput.getSessionDate().plusDays(1));
	}

	public ChamberPayloadStatus[] getChamberPayload(Session session) {
		if (session == null) {
			return new ChamberPayloadStatus[0];
		}

		ChamberPayloadStatus[] chamberPayload = new ChamberPayloadStatus[session.getChamber().getCapacity()];
		Arrays.fill(chamberPayload, ChamberPayloadStatus.AVAILABLE);

		int i = 0;
		for (PatientSession patientSession : session.getPatientSessions()) {
			chamberPayload[i] = patientSession.isAbsent() ? ChamberPayloadStatus.ABSENT : ChamberPayloadStatus.OCCUPIED;
			i++;
		}

		return chamberPayload;
	}

	public void initializeDuplicateSessionDates() {
		fromSessionsDate = LocalDate.now().minusDays(1);
		toSessionsDate = LocalDate.now();
	}

	public void invalidateSessionCache() {
		List<String> keys = chamberSessions.keySet().stream().filter(k -> k.endsWith(String.valueOf(sessionInput.sessionDate.toEpochDay()))).collect(Collectors.toList());
		keys.forEach(chamberSessions::remove);
		sessionInput.reset();
	}

	public String getScheduledSessionDates() {
		Set<LocalDate> sessionDates = scheduledSessionDates.stream().map(LocalDateTime::toLocalDate).collect(Collectors.toSet());
		return sessionDates.stream().map(date -> "'" + date + "'").collect(Collectors.joining(","));
	}

	@Getter
	@Setter
	public static class SessionInput {
		private Chamber chamber;
		private Session session;
		private List<Patient> patients;
		private LocalDate sessionDate;
		private LocalTime sessionTime;

		public SessionInput() {
			chamber = new Chamber();
			session = new Session();
			patients = new ArrayList<>();
			sessionDate = LocalDate.now();
		}

		public static SessionInput of(Chamber chamber, LocalDate sessionDate, LocalTime sessionTime, Patient... patients) {
			SessionInput sessionInput = new SessionInput();
			sessionInput.chamber = chamber;
			sessionInput.sessionDate = sessionDate;
			sessionInput.sessionTime = sessionTime;

			for (Patient patient : patients) {
				sessionInput.patients.add(patient);
			}
			return sessionInput;
		}

		public void reset() {
			session = new Session();
			patients.clear();
		}
	}
}
