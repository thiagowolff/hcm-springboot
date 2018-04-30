package br.com.litecode.controller;

import br.com.litecode.domain.model.*;
import br.com.litecode.domain.model.ChamberEvent.EventType;
import br.com.litecode.domain.model.Session.SessionStatus;
import br.com.litecode.domain.repository.ChamberRepository;
import br.com.litecode.domain.repository.SessionRepository;
import br.com.litecode.service.SessionReportService;
import br.com.litecode.service.push.PushChannel;
import br.com.litecode.service.push.PushRefresh;
import br.com.litecode.service.push.PushService;
import br.com.litecode.service.push.message.NotificationMessage;
import br.com.litecode.service.timer.SessionTimer;
import br.com.litecode.util.MessageUtil;
import com.google.common.collect.Lists;
import com.itextpdf.text.DocumentException;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.SecurityUtils;
import org.omnifaces.util.Faces;
import org.omnifaces.util.Messages;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import javax.enterprise.context.SessionScoped;
import java.io.IOException;
import java.io.Serializable;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@SessionScoped
@Component
@CacheConfig(cacheNames = "session")
@Slf4j
public class SessionController implements Serializable {
	@Autowired
	private SessionRepository sessionRepository;

	@Autowired
	private ChamberRepository chamberRepository;

	@Autowired
	private SessionTimer sessionTimer;

	@Autowired
	private PushService pushService;

	@Autowired
	private CacheManager cacheManager;

	@Autowired
	private SessionReportService sessionReportService;

	private Cache sessionCache;
	private Cache patientCache;

	@Getter @Setter
	private SessionData sessionData;

	@Getter @Setter
	private LocalDate fromSessionsDate;

	@Getter @Setter
	private LocalDate toSessionsDate;

	private List<LocalDateTime> scheduledSessionDates;

	private enum ChamberPayloadStatus { AVAILABLE, OCCUPIED, ABSENT }
	private enum SessionOperationType { CREATE_SESSION, DELETE_SESSION }

	@PostConstruct
	private void init() {
		sessionData = new SessionData();
		scheduledSessionDates = sessionRepository.findScheduledSessionDates();
		sessionCache = cacheManager.getCache("session");
		patientCache = cacheManager.getCache("patient");
		sessionCache.clear();
		patientCache.clear();
	}

	@Transactional(readOnly = true)
	@Cacheable(key = "{ #chamberId, #sessionDate }", sync = true)
	public List<Session> getSessions(Integer chamberId, LocalDate sessionDate) {
		return sessionRepository.findSessionsByChamberAndDate(chamberId, sessionDate);
	}

	@Transactional
	public void addSession() {
		int sessionDuration = sessionData.getChamber().getChamberEvent(EventType.COMPLETION).getTimeout();
		LocalDateTime scheduledTime = sessionData.getSessionDate().atTime(sessionData.getSessionTime());
		LocalTime startTime = scheduledTime.toLocalTime();
		LocalTime endTime = scheduledTime.plusSeconds(sessionDuration).toLocalTime();

		List<Session> chamberSessions = sessionRepository.findSessionsByChamberAndDate(sessionData.getChamber().getChamberId(), scheduledTime.toLocalDate());
		boolean isScheduled = chamberSessions.stream().anyMatch(session -> !session.getStartTime().isAfter(startTime) && !session.getEndTime().isBefore(startTime));
		if (isScheduled) {
			Messages.addGlobalError(MessageUtil.getMessage("error.sessionAlreadyCreatedForPeriod"));
			return;
		}

		if (sessionData.getPatients().size() > sessionData.getChamber().getCapacity()) {
			Messages.addGlobalError(MessageUtil.getMessage("error.chamberPatientsLimitExceeded", sessionData.getChamber().getCapacity()));
			return;
		}

		Session session = new Session();
		session.setChamber(sessionData.getChamber());
		session.setScheduledTime(scheduledTime);
		session.setStartTime(startTime);
		session.setEndTime(endTime);
		session.setCreatedBy(User.getLoggedUser());
		session.setCreatedOn(Instant.now());

		sessionData.getPatients().forEach(session::addPatient);
		sessionRepository.save(session);
		invalidateSessionCache();

		NotificationMessage notificationMessage = NotificationMessage.create(session, SessionOperationType.CREATE_SESSION.name(), User.getLoggedUser().getUserSettings());
		pushService.publish(PushChannel.NOTIFY, notificationMessage, User.getLoggedUser());
	}

	@CacheEvict(cacheNames = "session", key = "{ #session.chamber.chamberId, #session.sessionDate }")
	public void startSession(Session session) {
		session = sessionRepository.findOne(session.getSessionId());

		if (session.isPaused()) {
			session.resume();
		} else {
			session.init();
		}

		session.getExecutionMetadata().setStartedBy((String) SecurityUtils.getSubject().getPrincipal());
		sessionRepository.save(session);
		sessionTimer.startSession(session);
	}

	@PushRefresh
	@Transactional
	@Caching(evict = { @CacheEvict(cacheNames = "patient", allEntries = true), @CacheEvict(cacheNames = "session", key = "{ #session.chamber.chamberId, #session.sessionDate }") })
	public void stopSession(Session session) {
		session = sessionRepository.findOne(session.getSessionId());
		sessionTimer.stopSession(session);
		session.reset();
		sessionRepository.save(session);
	}

	@PushRefresh
	@Transactional
	@Caching(evict = { @CacheEvict(cacheNames = "patient", allEntries = true), @CacheEvict(cacheNames = "session", key = "{ #session.chamber.chamberId, #session.sessionDate }") })
	public void finishSession(Session session) {
		session = sessionRepository.findOne(session.getSessionId());
		sessionTimer.stopSession(session);

		ChamberEvent completionEvent = session.getChamber().getChamberEvent(EventType.COMPLETION);
		session.setStartTime(session.getScheduledTime().toLocalTime());
		session.setEndTime(session.getScheduledTime().plus(completionEvent.getTimeout(), ChronoUnit.SECONDS).toLocalTime());
		session.setStatus(SessionStatus.FINISHED);
		session.getExecutionMetadata().setCurrentProgress(100);
		session.getExecutionMetadata().setPaused(false);
		session.getExecutionMetadata().setCurrentEvent(completionEvent);
		sessionRepository.save(session);
	}

	@PushRefresh
	@Transactional
	@CacheEvict(cacheNames = "session", key = "{ #session.chamber.chamberId, #session.sessionDate }")
	public void pauseSession(Session session) {
		session = sessionRepository.findOne(session.getSessionId());
		sessionTimer.stopSession(session);
		session.pause();
		sessionRepository.save(session);
	}

	@PushRefresh
	@Transactional
	@Caching(evict = { @CacheEvict(cacheNames = "patient", allEntries = true), @CacheEvict(cacheNames = "session", key = "{ #session.chamber.chamberId, #session.sessionDate }") })
	public void deleteSession(Session session) {
		sessionTimer.stopSession(session);
		sessionRepository.delete(session);

		NotificationMessage notificationMessage = NotificationMessage.create(session, SessionOperationType.DELETE_SESSION.name(), User.getLoggedUser() != null ? User.getLoggedUser().getUserSettings() : null);
		pushService.publish(PushChannel.NOTIFY,  notificationMessage, User.getLoggedUser());
	}

	@PushRefresh
	@Transactional
	@CacheEvict(cacheNames = "patient", allEntries = true)
	public void setPatientSessionStatus(PatientSession patientSession, boolean absent) {
		patientSession.setAbsent(absent);
		sessionRepository.save(patientSession.getSession());
	}

	@PushRefresh
	@Transactional
	@Caching(evict = { @CacheEvict(cacheNames = "patient", allEntries = true), @CacheEvict(cacheNames = "session", key = "{ #session.chamber.chamberId, #session.sessionDate }") })
	public void addPatientsToSession(Session session) {
		if (sessionData.getPatients().size() + session.getPatientSessions().size() > session.getChamber().getCapacity()) {
			Messages.addGlobalError(MessageUtil.getMessage("error.chamberPatientsLimitExceeded", session.getChamber().getCapacity()));
			return;
		}

		session = sessionRepository.findOne(session.getSessionId());
		sessionData.getPatients().forEach(session::addPatient);
		sessionData.setSession(sessionRepository.save(session));
	}

	@PushRefresh
	@Transactional
	@Caching(evict = {
			@CacheEvict(cacheNames = "patient", allEntries = true),
			@CacheEvict(cacheNames = "session", key = "{ #patientSession.session.chamber.chamberId, #patientSession.session.sessionDate }")
	})
	public void removePatientFromSession(PatientSession patientSession) {
		sessionData.getSession().getPatientSessions().remove(patientSession);
		sessionData.setSession(sessionRepository.save(patientSession.getSession()));
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
			session.setCreatedBy(User.getLoggedUser());
			session.setCreatedOn(Instant.now());
			fromSession.getPatientSessions().forEach(ps -> session.addPatient(ps.getPatient()));
			sessionRepository.save(session);
		}
		evictSessionCacheByDate(toSessionsDate);
		invalidateSessionCache();
	}

	public void switchSessionChamber() {
		sessionData.getSession().setChamber(sessionData.getChamber());
		sessionRepository.save(sessionData.getSession());
		invalidateSessionCache();
	}

	public void previousSessionDate() {
		sessionData.setSessionDate(sessionData.getSessionDate().minusDays(1));
	}

	public void nextSessionDate() {
		sessionData.setSessionDate(sessionData.getSessionDate().plusDays(1));
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
		evictSessionCacheByDate(sessionData.getSessionDate());

		String sessionId = Faces.getRequestParameter("sessionId");

		if (sessionId != null) {
			patientCache.evict(Arrays.asList(Integer.valueOf(sessionId), sessionData.getSessionDate()));
		}

		sessionData.reset();
	}

	private void evictSessionCacheByDate(LocalDate date) {
		List<List<Object>> keys = new ArrayList<>();
		for (Chamber chamber : chamberRepository.findAll()) {
			keys.add(Lists.newArrayList(chamber.getChamberId(), date));
		}
		keys.forEach(sessionCache::evict);
	}

	public void generateDailySessionsReport() {
		try {
			byte[] pdfData = sessionReportService.generateSessionReport(sessionData.getSessionDate());
			String fileName = sessionData.getSessionDate().format(DateTimeFormatter.BASIC_ISO_DATE) + ".pdf";
			Faces.sendFile(pdfData, fileName, false);
		} catch (DocumentException | IOException e) {
			log.error("Unable to generate PDF: {}", e);
		}
	}

	public String getScheduledSessionDates() {
		Set<LocalDate> sessionDates = scheduledSessionDates.stream().map(LocalDateTime::toLocalDate).collect(Collectors.toSet());
		return sessionDates.stream().map(date -> "'" + date + "'").collect(Collectors.joining(","));
	}

	@Getter
	@Setter
	public static class SessionData {
		private Chamber chamber;
		private Session session;
		private List<Patient> patients;
		private LocalDate sessionDate;
		private LocalTime sessionTime;

		public SessionData() {
			chamber = new Chamber();
			session = new Session();
			patients = new ArrayList<>();
			sessionDate = LocalDate.now();
			sessionTime = LocalTime.parse("09:30");
		}

		public static SessionData of(Chamber chamber, LocalDate sessionDate, LocalTime sessionTime, Patient... patients) {
			SessionData sessionData = new SessionData();
			sessionData.chamber = chamber;
			sessionData.sessionDate = sessionDate;
			sessionData.sessionTime = sessionTime;
			Collections.addAll(sessionData.patients, patients);
			return sessionData;
		}

		public void reset() {
			session = new Session();
			patients.clear();
		}


		public void today() {
			sessionDate = LocalDate.now();
		}
	}
}
