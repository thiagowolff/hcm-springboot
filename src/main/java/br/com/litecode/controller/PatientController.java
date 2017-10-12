package br.com.litecode.controller;

import br.com.litecode.domain.model.Patient;
import br.com.litecode.domain.model.Patient.PatientStats;
import br.com.litecode.domain.model.PatientSession;
import br.com.litecode.domain.model.Session;
import br.com.litecode.domain.repository.PatientRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.faces.view.ViewScoped;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@ViewScoped
@Component
@CacheConfig(cacheNames = "patient")
public class PatientController implements Serializable {
	@Autowired
	private PatientRepository patientRepository;

	private Patient patient;
	private List<Patient> patients;
	private List<Patient> filteredPatients;

	public PatientController() {
		patient = new Patient();
	}

	@Transactional
	public List<Patient> getPatients() {
		if (patients == null) {
			patients = patientRepository.findActivePatients();
		}
		return patients;
	}

	@Cacheable(key = "{ #session.sessionId, #date }", sync = true)
	public Map<Integer, PatientStats> getPatientStats(Session session, LocalDate date) {
		List<PatientStats> stats = patientRepository.findPatienStats(session.getSessionId(), date.plusDays(1).atStartOfDay());

		Map<Integer, PatientStats> patientStats = stats.stream().collect(Collectors.toMap(PatientStats::getPatientId, Function.identity()));
		for (PatientSession patientSession : session.getPatientSessions()) {
			Integer patientId = patientSession.getPatient().getPatientId();
			patientStats.putIfAbsent(patientId, Patient.getEmptyPatientStats(patientId));
		}

		return patientStats;
	}

	@Cacheable(key = "#patientId", sync = true)
	public PatientStats getPatientStats(Integer patientId) {
		return patientRepository.findPatienStats(patientId);
	}

	public List<Patient> getAvailabePatientsForSession(Integer sessionId) {
		return patientRepository.findPatientsNotInSession(sessionId);
	}
	
	public void deletePatient() {
		patient.setActive(false);
		patientRepository.save(patient);
		patients = null;
	}
	
	public void savePatient() {
		patientRepository.save(patient);
		patients = null;
	}

	public Patient getPatient() {
		return patient;
	}

	public void setPatient(Patient patient) {
		this.patient = patient;
	}

	public void newPatient() {
		patient = new Patient();
	}

	public List<Patient> getFilteredPatients() {
		return filteredPatients;
	}

	public void setFilteredPatients(List<Patient> filteredPatients) {
		this.filteredPatients = filteredPatients;
	}
}