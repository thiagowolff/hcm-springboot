package br.com.litecode.service;

import br.com.litecode.domain.Alarm;
import br.com.litecode.domain.ChamberEvent.EventType;
import br.com.litecode.domain.Patient;
import br.com.litecode.domain.PatientSession;
import br.com.litecode.domain.Session;
import br.com.litecode.domain.Session.SessionStatus;
import br.com.litecode.persistence.impl.AlarmDao;
import br.com.litecode.persistence.impl.PatientDao;
import br.com.litecode.persistence.impl.SessionDao;
import org.hibernate.Hibernate;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.joda.time.LocalTime;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.Query;
import java.util.Date;
import java.util.List;

@Stateless
public class SessionService {
	@Inject	private SessionDao sessionDao;
	@Inject	private PatientDao patientDao;
	@Inject	private AlarmDao alarmDao;

	public Session getSession(Integer sessionId) {
		return sessionDao.findSessionById(sessionId);
	}

	public List<Session> getSessions(Integer chamberId, Date date) {
		List<Session> sessions = sessionDao.findChamberSessionsByDate(chamberId, date);

		for (Session session: sessions ) {
			Hibernate.initialize(session.getPatientSessions());
		}

		return sessions;
	}

	public List<Session> getSessionsByPeriod(Integer chamberId, Date sessionTime) {
		return sessionDao.findChamberSessionsByPeriod(chamberId, sessionTime);
	}

	public void createSession(Session session) {
		sessionDao.insert(session);
	}

	public void createSession(Session session, List<Patient> patients) {
		for (Patient patient : patients) {
			PatientSession patientSession = new PatientSession(patientDao.update(patient), session);
			sessionDao.insertPatientSession(patientSession);
		}
	}

	public void addPatientsToSession(Session session, List<Patient> patients) {
		session = sessionDao.findSessionById(session.getSessionId());
		for (Patient patient : patients) {
			PatientSession patientSession = new PatientSession(patientDao.update(patient), session);
			sessionDao.insertPatientSession(patientSession);
		}
	}

	public void deleteSession(Session session) {
		sessionDao.delete(session);
	}

	public void duplicateSessions(Date fromDate, Date toDate) {
		List<Session> sessions = sessionDao.findSessionsByDate(fromDate);
		sessions.forEach(s -> {
			LocalTime time = LocalDateTime.fromDateFields(s.getScheduledTime()).toLocalTime();
			LocalDateTime sessionTime = LocalDate.fromDateFields(toDate).toLocalDateTime(time);
			int sessionDuration = s.getChamber().getChamberEvent(EventType.COMPLETION).getTimeout();

			Session session = new Session();
			session.setChamber(s.getChamber());
			session.setScheduledTime(sessionTime.toDate());
			session.setStartTime(sessionTime.toDate());
			session.setEndTime(sessionTime.plusMillis(sessionDuration).toDate());
			session.setStatus(SessionStatus.CREATED);
			session.setCurrentProgress(0);

			s.getPatientSessions().forEach(ps -> {
				sessionDao.insertPatientSession(new PatientSession(ps.getPatient(), session));
			});
		});
	}

	public List<Date> loadSessionDates() {
		return sessionDao.findSessionDates();
	}

	public void updateSession(Session session) {
		sessionDao.update(session);
	}

	public void updatePatientSession(PatientSession patientSession) {
		sessionDao.updatePatientSession(patientSession);
	}

	public void deletePatientSession(PatientSession patientSession) {
		sessionDao.deletePatientSession(patientSession);
	}

	public List<PatientSession> getPatientSessions(Integer patientId, Date sessionDate) {
		return sessionDao.findPatientSessionsByDate(patientId, sessionDate);
	}

	public List<Alarm> getAlarms() {
		return alarmDao.findAll();
	}
}
