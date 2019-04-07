package br.com.litecode.controller;

import br.com.litecode.domain.model.*;
import br.com.litecode.domain.model.Session.SessionStatus;
import br.com.litecode.domain.repository.ChamberRepository;
import br.com.litecode.domain.repository.SessionRepository;
import br.com.litecode.security.UserPrincipal;
import br.com.litecode.service.SessionReportService;
import br.com.litecode.service.cache.SessionCacheEvict;
import br.com.litecode.service.push.NotificationMessage;
import br.com.litecode.service.push.PushChannel;
import br.com.litecode.service.push.PushRefresh;
import br.com.litecode.service.push.PushService;
import br.com.litecode.service.timer.SessionTimer;
import br.com.litecode.util.MessageUtil;
import com.google.common.base.Strings;
import com.itextpdf.text.DocumentException;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.omnifaces.util.Faces;
import org.omnifaces.util.Messages;
import org.primefaces.event.RowEditEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.interceptor.SimpleKey;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import javax.enterprise.context.SessionScoped;
import java.io.IOException;
import java.io.Serializable;
import java.time.*;
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

	private Session.MonthlyStats sessionMonthlyStats;

	@PostConstruct
	private void init() {
		sessionData = new SessionData();
		scheduledSessionDates = sessionRepository.findScheduledSessionDates();
		sessionCache = cacheManager.getCache("session");
		patientCache = cacheManager.getCache("patient");
		sessionCache.clear();
		patientCache.clear();
	}

	@Cacheable
	public List<Session> getSessions(Integer chamberId, LocalDate sessionDate) {
		return sessionRepository.findSessionsByChamberAndDate(chamberId, sessionDate);
	}

	@Transactional
	@SessionCacheEvict
	@CacheEvict(key = "'getScheduledSessionDates'")
	public void addSession() {
		int sessionDuration = sessionData.getChamber().getLastEvent().getTimeout();
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
		session.setCreatedBy(UserPrincipal.getLoggedUser());
		session.setCreatedOn(Instant.now());

		sessionData.getPatients().forEach(session::addPatient);
		sessionRepository.save(session);

		NotificationMessage notificationMessage = NotificationMessage.create(session, new EventType("create_session"));
		pushService.publish(PushChannel.NOTIFY, notificationMessage, UserPrincipal.getLoggedUser());
	}

	@CacheEvict(cacheNames = "session", key = "{ #session.chamber.chamberId, #session.sessionDate }")
	public void startSession(Session session) {
		session = sessionRepository.findOne(session.getSessionId());

		if (session.isPaused()) {
			session.resume();
		} else {
			session.init();
		}

		session.getExecutionMetadata().setStartedBy(UserPrincipal.getLoggedUser().getUsername());
		session = sessionRepository.save(session);
		sessionTimer.startSession(session);
	}

	@PushRefresh
	@Transactional
	public void stopSession(Session session) {
		session = sessionRepository.findOne(session.getSessionId());
		sessionTimer.stopSession(session);
		session.reset();
		sessionRepository.save(session);
	}

	@PushRefresh
	@Transactional
	public void finishSession(Session session) {
		session = sessionRepository.findOne(session.getSessionId());
		sessionTimer.stopSession(session);

		ChamberEvent completionEvent = session.getChamber().getLastEvent();
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
	public void pauseSession(Session session) {
		session = sessionRepository.findOne(session.getSessionId());
		sessionTimer.stopSession(session);
		session.pause();
		sessionRepository.save(session);
	}

	@PushRefresh
	@Transactional
	@CacheEvict(key = "'getScheduledSessionDates'")
	public void deleteSession(Session session) {
		sessionTimer.stopSession(session);
		sessionRepository.delete(session);

		NotificationMessage notificationMessage = NotificationMessage.create(session, new EventType("delete_session"));
		pushService.publish(PushChannel.NOTIFY,  notificationMessage, UserPrincipal.getLoggedUser());
	}

	@PushRefresh
	@Transactional
	@SessionCacheEvict
	public void setPatientSessionStatus(PatientSession patientSession, boolean absent) {
		patientSession.setAbsent(absent);
		sessionRepository.save(patientSession.getSession());
	}

	@PushRefresh
	@Transactional
	@CacheEvict(cacheNames = "patient", allEntries = true)
	public void updatePatientSession(PatientSession patientSession) {
		sessionRepository.save(patientSession.getSession());
	}

    @PushRefresh
    @Transactional
    @CacheEvict(cacheNames = "patient", allEntries = true)
    public void onPatientVitalSignsEdit(RowEditEvent event) {
	    PatientSession patientSession = (PatientSession) event.getObject();
        sessionRepository.save(patientSession.getSession());
    }

	@PushRefresh
	@Transactional
	@SessionCacheEvict
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
	@SessionCacheEvict
	public void removePatientFromSession(PatientSession patientSession) {
		sessionData.getSession().getPatientSessions().remove(patientSession);
		sessionData.setSession(sessionRepository.save(patientSession.getSession()));
	}

	@PushRefresh
	@Transactional
	@CacheEvict(cacheNames = "session", key = "{ #session.chamber.chamberId, #session.sessionDate }")
	public void resetVitalSigns(Session session) {
		session = sessionRepository.findOne(session.getSessionId());
		for (PatientSession patientSession : session.getPatientSessions()) {
			patientSession.setTemperature(null);
			patientSession.setBloodPressure(null);
		}
		sessionData.setSession(sessionRepository.save(session));
	}

	@PushRefresh
	@Transactional
	@CacheEvict(key = "'getScheduledSessionDates'")
	public void duplicateSessions() {
		List<Session> fromSessions = sessionRepository.findSessionsByDate(fromSessionsDate);
		for (Session fromSession : fromSessions) {
			LocalTime time = fromSession.getScheduledTime().toLocalTime();
			LocalDateTime sessionTime = toSessionsDate.atTime(time);
			int sessionDuration = fromSession.getDuration();

			Session session = new Session();
			session.setChamber(fromSession.getChamber());
			session.setScheduledTime(sessionTime);
			session.setStartTime(sessionTime.toLocalTime());
			session.setEndTime(sessionTime.plusSeconds(sessionDuration).toLocalTime());
			session.setCreatedBy(UserPrincipal.getLoggedUser());
			session.setCreatedOn(Instant.now());
			fromSession.getPatientSessions().forEach(ps -> session.addPatient(ps.getPatient()));
			sessionRepository.save(session);
		}
		invalidateSessionCache();
	}

	public void switchSessionChamber() {
	    Chamber destChamber = sessionData.getChamber();

	    if (sessionData.getSession().getPatientSessions().size() > destChamber.getCapacity()) {
            Messages.addGlobalError(MessageUtil.getMessage("error.chamberPatientsLimitExceeded", destChamber.getCapacity()));
            Faces.validationFailed();
	        return;
        }

		sessionData.getSession().setChamber(destChamber);
		sessionRepository.save(sessionData.getSession());
		invalidateSessionCache();
	}

	public void previousSessionDate() {
		LocalDate sessionDate = sessionData.getSessionDate();
		int daysToSubtract = sessionDate.getDayOfWeek() == DayOfWeek.MONDAY ? 2 : 1;
		sessionData.setSessionDate(sessionDate.minusDays(daysToSubtract));
	}

	public void nextSessionDate() {
		LocalDate sessionDate = sessionData.getSessionDate();
		int daysToAdd = sessionDate.getDayOfWeek() == DayOfWeek.SATURDAY ? 2 : 1;
		sessionData.setSessionDate(sessionDate.plusDays(daysToAdd));
	}

	public Session.MonthlyStats getSessionMonthlyStats() {
		LocalDate sessionDate = sessionData.getSessionDate();

		sessionMonthlyStats = sessionRepository.findMonthlyStats(sessionDate.getYear(), sessionDate.getMonthValue());
		if (sessionMonthlyStats == null) {
			sessionMonthlyStats = Session.getEmptyMonthlyStats(sessionDate);
		}

		return sessionMonthlyStats;
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

		if (!Strings.isNullOrEmpty(sessionId)) {
			patientCache.evict(new SimpleKey(Integer.valueOf(sessionId), sessionData.getSessionDate()));
		}

		sessionData.reset();
	}

	private void evictSessionCacheByDate(LocalDate date) {
		List<SimpleKey> keys = new ArrayList<>();
		for (Chamber chamber : chamberRepository.findAll()) {
			keys.add(new SimpleKey(chamber.getChamberId(), date));
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

	@Cacheable(key = "#root.methodName")
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
