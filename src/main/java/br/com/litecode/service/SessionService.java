package br.com.litecode.service;

import br.com.litecode.domain.Alarm;
import br.com.litecode.domain.ChamberEvent.EventType;
import br.com.litecode.domain.Patient;
import br.com.litecode.domain.Session;
import br.com.litecode.domain.Session.SessionStatus;
import br.com.litecode.persistence.impl.AlarmDao;
import br.com.litecode.persistence.impl.PatientDao;
import br.com.litecode.persistence.impl.SessionDao;
import br.com.litecode.util.MessageUtil;
import org.joda.time.*;
import org.omnifaces.util.Messages;

import javax.ejb.Stateless;
import javax.inject.Inject;
import java.util.Date;
import java.util.List;

@Stateless
public class SessionService {
	@Inject	private SessionDao sessionDao;
	@Inject	private PatientDao patientDao;
	@Inject	private AlarmDao alarmDao;

	public List<Session> getSessions(Integer chamberId, Date date) {
		return sessionDao.findChamberSessionsByDate(chamberId, date);
	}

	public List<Session> getSessionsByPeriod(Integer chamberId, Date sessionTime) {
		return sessionDao.findChamberSessionsByPeriod(chamberId, sessionTime);
	}

	public Long getNumberOfPatientSessions(Integer patientId) {
		return sessionDao.countSessionsPerPatient(patientId);
	}

	public void createSession(Session session) {
		Patient patient = patientDao.update(session.getPatient());
		session.setPatient(patient);
		sessionDao.insert(session);
	}

	public void deleteSession(Session session) {
		sessionDao.delete(session);
	}

	public void duplicateSessions(Date sourceDate) {
		List<Session> sessions = sessionDao.findSessionsByDate(sourceDate);
		sessions.forEach(s -> {
			LocalTime time = LocalDateTime.fromDateFields(s.getSessionTime()).toLocalTime();
			LocalDateTime sessionTime = time.toDateTimeToday().toLocalDateTime();
			int sessionDuration = s.getChamber().getChamberEvent(EventType.COMPLETION).getTimeout();

			Session session = new Session();
			session.setChamber(s.getChamber());
			session.setPatient(s.getPatient());
			session.setSessionTime(sessionTime.toDate());
			session.setStartTime(sessionTime.toDate());
			session.setEndTime(sessionTime.plusMillis(sessionDuration).toDate());
			session.setStatus(SessionStatus.CREATED);
			session.setCurrentProgress(0);

			Long numberOfPatientSessions = getNumberOfPatientSessions(s.getPatient().getPatientId());
			if (numberOfPatientSessions == null) {
				numberOfPatientSessions = 0L;
			}
			session.setNumberOfPatientSessions(numberOfPatientSessions);

			createSession(session);
		});
	}

	public void updateSession(Session session) {
		sessionDao.update(session);
	}

	public List<Alarm> getAlarms() {
		return alarmDao.findAll();
	}
}
