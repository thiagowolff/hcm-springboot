package br.com.litecode.persistence.impl;

import br.com.litecode.domain.Chamber;
import br.com.litecode.domain.Patient;
import br.com.litecode.domain.Session;
import br.com.litecode.persistence.AbstractDao;

import javax.persistence.TypedQuery;
import java.util.Date;
import java.util.List;

public class PatientDao extends AbstractDao<Patient> {
	public List<Patient> findPatients() {
		String qlString = "select p from Patient p order by p.name, p.patientId";
		TypedQuery<Patient> query = entityManager.createQuery(qlString, Patient.class);
		return query.getResultList();
	}

}
