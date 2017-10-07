package br.com.litecode.controller;

import br.com.litecode.domain.model.Patient;
import br.com.litecode.domain.model.Patient.PatientStats;
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

	@Cacheable(key = "{ #sessionId, #sessionDate }", sync = true)
	public List<PatientStats> getPatientStats(Integer sessionId, LocalDate sessionDate) {
		return patientRepository.findPatienStats(sessionId, sessionDate.plusDays(1).atStartOfDay());
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