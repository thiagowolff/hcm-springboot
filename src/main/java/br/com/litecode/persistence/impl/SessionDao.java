package br.com.litecode.persistence.impl;

import br.com.litecode.domain.PatientSession;
import br.com.litecode.domain.Session;
import br.com.litecode.persistence.AbstractDao;
import org.joda.time.LocalDateTime;

import javax.persistence.Query;
import javax.persistence.TypedQuery;
import java.util.Date;
import java.util.List;

public class SessionDao extends AbstractDao<Session> {
	public List<Session> findChamberSessionsByDate(Integer chamberId, Date date) {
		TypedQuery<Session> query = entityManager.createNamedQuery("findChamberSessionsByDate", Session.class);
		query.setParameter("chamberId", chamberId);
		query.setParameter("startOfDay", LocalDateTime.fromDateFields(date).withTime(0, 0, 0, 0).toDate());
		query.setParameter("endOfDay", LocalDateTime.fromDateFields(date).withTime(23, 59, 59, 999).toDate());

		return query.getResultList();
	}

	public List<Session> findSessionsByDate(Date date) {
		String qlString = "select s from Session s where s.scheduledTime between :startOfDay and :endOfDay order by s.scheduledTime";

		TypedQuery<Session> query = entityManager.createQuery(qlString, Session.class);
		query.setParameter("startOfDay", LocalDateTime.fromDateFields(date).withTime(0, 0, 0, 0).toDate());
		query.setParameter("endOfDay", LocalDateTime.fromDateFields(date).withTime(23, 59, 59, 999).toDate());

		return query.getResultList();
	}

	public List<Session> findChamberSessionsByPeriod(Integer chamberId, Date sessionTime) {
		String qlString = "select s from Session s where s.chamber.chamberId = :chamberId and s.scheduledTime = :sessionTime";

		TypedQuery<Session> query = entityManager.createQuery(qlString, Session.class);
		query.setParameter("chamberId", chamberId);
		query.setParameter("sessionTime", sessionTime);

		return query.getResultList();
	}

	public List<PatientSession> findPatientSessionsByDate(Integer patientId, Date sessionDate) {
		TypedQuery<PatientSession> query = entityManager.createNamedQuery("findPatientSessionsByDate", PatientSession.class);
		query.setParameter("patientId", patientId);
		query.setParameter("sessionDate", LocalDateTime.fromDateFields(sessionDate).withTime(23, 59, 59, 999).toDate());

		return query.getResultList();
	}

	public Session findSessionById(Integer sessionId) {
		String qlString = "select distinct s from Session s left join fetch s.patientSessions where s.sessionId = :sessionId";
		TypedQuery<Session> query = entityManager.createQuery(qlString, Session.class);
		query.setParameter("sessionId", sessionId);
		return query.getSingleResult();
	}

	public List<Date> findSessionDates() {
		String sqlString = "select distinct date(scheduled_time) from session";
		Query query = entityManager.createNativeQuery(sqlString);
		return query.getResultList();
	}

	public void insertPatientSession(PatientSession patientSession) {
		entityManager.persist(patientSession);
	}

	public void updatePatientSession(PatientSession patientSession) {
		entityManager.merge(patientSession);
	}

	public void deletePatientSession(PatientSession patientSession) {
		entityManager.remove(entityManager.find(PatientSession.class, patientSession.getId()));
	}
}
