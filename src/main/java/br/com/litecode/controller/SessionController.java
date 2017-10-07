package br.com.litecode.controller;

import br.com.litecode.domain.model.Chamber;
import br.com.litecode.domain.model.ChamberEvent.EventType;
import br.com.litecode.domain.model.Patient;
import br.com.litecode.domain.model.PatientSession;
import br.com.litecode.domain.model.Session;
import br.com.litecode.domain.model.Session.SessionStatus;
import br.com.litecode.domain.repository.ChamberRepository;
import br.com.litecode.domain.repository.SessionRepository;
import br.com.litecode.service.PdfService;
import br.com.litecode.service.push.PushChannel;
import br.com.litecode.service.push.PushRefresh;
import br.com.litecode.service.push.PushService;
import br.com.litecode.service.push.message.NotificationMessage;
import br.com.litecode.service.timer.SessionTimer;
import br.com.litecode.util.MessageUtil;
import com.google.common.collect.Lists;
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
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
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
	private PdfService pdfService;

	private Cache sessionCache;

	@Getter @Setter
	private SessionInput sessionInput;

	@Getter @Setter
	private LocalDate fromSessionsDate;

	@Getter @Setter
	private LocalDate toSessionsDate;

	private List<LocalDateTime> scheduledSessionDates;

	private enum ChamberPayloadStatus { AVAILABLE, OCCUPIED, ABSENT }
	private enum SessionOperationType { CREATE_SESSION, DELETE_SESSION }

	@PostConstruct
	private void init() {
		sessionInput = new SessionInput();
		scheduledSessionDates = sessionRepository.findScheduledSessionDates();
		sessionCache = cacheManager.getCache("session");
		sessionCache.clear();
	}

	@Transactional(readOnly = true)
	@Cacheable(key = "{ #chamberId, #sessionDate }", sync = true)
	public List<Session> getSessions(Integer chamberId, LocalDate sessionDate) {
		return new CopyOnWriteArrayList(sessionRepository.findSessionsByChamberAndDate(chamberId, sessionDate));
	}

	@Transactional
	public void addSession() {
		int sessionDuration = sessionInput.getChamber().getChamberEvent(EventType.COMPLETION).getTimeout();
		LocalDateTime scheduledTime = sessionInput.getSessionDate().atTime(sessionInput.getSessionTime());
		LocalTime endTime = scheduledTime.plusSeconds(sessionDuration).toLocalTime();

		boolean isScheduled = sessionRepository.isSessionScheduled(sessionInput.getChamber().getChamberId(), scheduledTime);
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
		session.setScheduledTime(scheduledTime);
		session.setStartTime(session.getScheduledTime().toLocalTime());
		session.setEndTime(endTime);
		session.getContextData().setCreatedBy((String) SecurityUtils.getSubject().getPrincipal());
		sessionInput.getPatients().forEach(session::addPatient);
		sessionRepository.save(session);
		invalidateSessionCache();
		pushService.publish(PushChannel.NOTIFY,  NotificationMessage.create(session, SessionOperationType.CREATE_SESSION.name()), session.getContextData().getCreatedBy());
	}

	@CacheEvict(cacheNames = "session", key = "{ #session.chamber.chamberId, #session.sessionDate }")
	public void startSession(Session session) {
		session = sessionRepository.findOne(session.getSessionId());
		session.init();
		session.getContextData().setStartedBy((String) SecurityUtils.getSubject().getPrincipal());
		sessionRepository.save(session);
		sessionTimer.startSession(session);
	}

	@PushRefresh
	@Transactional
	@Caching(evict = { @CacheEvict(cacheNames = "patient", allEntries = true), @CacheEvict(cacheNames = "session", key = "{ #session.chamber.chamberId, #session.sessionDate }") })
	public void resetSession(Session session) {
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

		session.setStartTime(session.getScheduledTime().toLocalTime());
		session.setEndTime(session.getScheduledTime().plus(session.getChamber().getChamberEvent(EventType.COMPLETION).getTimeout(), ChronoUnit.SECONDS).toLocalTime());
		session.setCurrentProgress(100);
		session.setStatus(SessionStatus.FINISHED);
		sessionRepository.save(session);
	}

	@PushRefresh
	@Transactional
	@Caching(evict = { @CacheEvict(cacheNames = "patient", allEntries = true), @CacheEvict(cacheNames = "session", key = "{ #session.chamber.chamberId, #session.sessionDate }") })
	public void deleteSession(Session session) {
		sessionTimer.stopSession(session);
		sessionRepository.delete(session);
		pushService.publish(PushChannel.NOTIFY,  NotificationMessage.create(sessionInput.getSession(), SessionOperationType.DELETE_SESSION.name()), sessionInput.getSession().getContextData().getCreatedBy());
	}

	@PushRefresh
	@Transactional
	public void setPatientSessionStatus(PatientSession patientSession, boolean absent) {
		patientSession.setAbsent(absent);
		sessionRepository.save(patientSession.getSession());
	}

	@PushRefresh
	@Transactional
	@Caching(evict = { @CacheEvict(cacheNames = "patient", allEntries = true), @CacheEvict(cacheNames = "session", key = "{ #session.chamber.chamberId, #session.sessionDate }") })
	public void addPatientsToSession(Session session) {
		if (sessionInput.getPatients().size() + session.getPatientSessions().size() > session.getChamber().getCapacity()) {
			Messages.addGlobalError(MessageUtil.getMessage("error.chamberPatientsLimitExceeded", session.getChamber().getCapacity()));
			return;
		}

		session = sessionRepository.findOne(session.getSessionId());
		sessionInput.getPatients().forEach(session::addPatient);
		sessionInput.setSession(sessionRepository.save(session));
	}

	@PushRefresh
	@Transactional
	@Caching(evict = {
			@CacheEvict(cacheNames = "patient", allEntries = true),
			@CacheEvict(cacheNames = "session", key = "{ #patientSession.session.chamber.chamberId, #patientSession.session.sessionDate }")
	})
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
			session.getContextData().setCreatedBy((String) SecurityUtils.getSubject().getPrincipal());
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
		List<List<Object>> keys = new ArrayList<>();
		for (Chamber chamber : chamberRepository.findAll()) {
			keys.add(Lists.newArrayList(chamber.getChamberId(), sessionInput.getSessionDate()));
		}
		keys.forEach(sessionCache::evict);
		sessionInput.reset();
	}

	public void generateDailySessionsReport() {
		byte[] sessionsReportContent;
		try {
			sessionsReportContent = pdfService.generateSessionReport(sessionInput.getSessionDate());
			Faces.sendFile(sessionsReportContent, sessionInput.getSessionDate().format(DateTimeFormatter.BASIC_ISO_DATE) + ".pdf", false);
		} catch (Exception e) {
			e.printStackTrace();
		}
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
			Collections.addAll(sessionInput.patients, patients);
			return sessionInput;
		}

		public void reset() {
			session = new Session();
			patients.clear();
		}
	}
}
