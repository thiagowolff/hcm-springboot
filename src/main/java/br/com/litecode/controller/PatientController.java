package br.com.litecode.controller;

import br.com.litecode.domain.model.Patient;
import br.com.litecode.domain.model.Patient.PatientStats;
import br.com.litecode.domain.model.PatientSession;
import br.com.litecode.domain.model.Session;
import br.com.litecode.domain.model.User;
import br.com.litecode.domain.repository.PatientRepository;
import br.com.litecode.util.MessageUtil;
import org.omnifaces.component.output.cache.Cache;
import org.omnifaces.component.output.cache.CacheFactory;
import org.omnifaces.util.Faces;
import org.omnifaces.util.Messages;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.dao.DataIntegrityViolationException;
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
		patient.audit(User.getLoggedUser());
		patientRepository.save(patient);
		refresh();
	}

	public void savePatient() {
		try {
			if (patient.getAge() != null && patient.getAge() < 10) {
				Faces.validationFailed();
				Messages.addGlobalError(MessageUtil.getMessage("error.patientInvalid"));
				return;
			}

			patient.setName(patient.getName().trim());
			patient.audit(User.getLoggedUser());
			patientRepository.save(patient);
		} catch (DataIntegrityViolationException e) {
			Faces.validationFailed();
			Messages.addGlobalError(MessageUtil.getMessage("error.patientRecord"));
		}
		refresh();
	}

	public Patient getPatient() {
		return patient;
	}

	public void setPatient(Patient patient) {
		this.patient = patient;
	}

	public void refresh() {
		this.patients = null;
		Cache cache = CacheFactory.getCache(Faces.getContext(), "session");
		if (cache != null) {
			cache.remove("patientsCache");
		}
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