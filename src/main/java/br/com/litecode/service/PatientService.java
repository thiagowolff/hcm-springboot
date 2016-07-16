package br.com.litecode.service;

import br.com.litecode.domain.Patient;
import br.com.litecode.domain.Session;
import br.com.litecode.persistence.impl.PatientDao;
import br.com.litecode.persistence.impl.SessionDao;

import javax.ejb.Stateless;
import javax.inject.Inject;
import java.util.Date;
import java.util.List;

@Stateless
public class PatientService {
	@Inject	private PatientDao patientDao;

	public Patient getPatient(Integer patientId) {
		return patientDao.findById(patientId);
	}

	public List<Patient> getPatients() {
		return patientDao.findPatients();
	}

	public void save(Patient patient) {
		patientDao.update(patient);
	}

	public void delete(Patient patient) {
		patientDao.delete(patient);
	}
}
