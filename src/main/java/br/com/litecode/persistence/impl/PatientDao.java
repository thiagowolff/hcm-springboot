package br.com.litecode.persistence.impl;

import br.com.litecode.domain.Patient;
import br.com.litecode.persistence.AbstractDao;

import javax.persistence.TypedQuery;
import java.util.List;

public class PatientDao extends AbstractDao<Patient> {
	public List<Patient> findPatients() {
		String qlString = "select distinct p from Patient p left join fetch p.patientSessions ps where p.active = true order by p.name, p.patientId";
		TypedQuery<Patient> query = entityManager.createQuery(qlString, Patient.class);
		return query.getResultList();
	}

	public List<Patient> findPatientsNotInSession(Integer sessionId) {
		String qlString = "select p from Patient p where p not in (select p from Patient p join p.patientSessions ps where ps.session.sessionId = :sessionId) order by p.name, p.patientId";
		TypedQuery<Patient> query = entityManager.createQuery(qlString, Patient.class);
		query.setParameter("sessionId", sessionId);
		return query.getResultList();
	}
}
