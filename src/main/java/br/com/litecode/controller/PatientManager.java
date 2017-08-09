package br.com.litecode.controller;

import br.com.litecode.domain.Patient;
import br.com.litecode.domain.PatientSession;
import br.com.litecode.domain.Session.SessionStatus;
import br.com.litecode.service.PatientService;
import br.com.litecode.service.SessionService;
import br.com.litecode.service.UserService;

import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Named
@ViewScoped
public class PatientManager implements Serializable {
	private static final long serialVersionUID = 1L;

	@Inject	private PatientService patientService;
	@Inject	private SessionService sessionService;
	@Inject private UserService userService;

	private Patient patient;
	private List<Patient> patients;
	private List<Patient> filteredPatients;
	private Map<Integer, SessionStats> patientSessionStats;

	public PatientManager() {
		patient = new Patient();
	}

	public List<Patient> getPatients() {
		if (patients == null) {
			patients = patientService.getPatients();

			patientSessionStats = new HashMap<>();

			for (Patient patient : patients) {
				int numberOfSessions = patient.getPatientSessions().size();
				int completedSessions = 0;
				int absentSessions = 0;

				for (PatientSession patientSession : patient.getPatientSessions()) {
					if (patientSession.isAbsent()) {
						absentSessions++;
						continue;
					}

					if (patientSession.getSession().getStatus() == SessionStatus.FINISHED) {
						completedSessions++;
					}
				}

				patientSessionStats.put(patient.getPatientId(), new SessionStats(completedSessions, absentSessions, numberOfSessions));
			}
		}
		return patients;
	}

	public List<Patient> getAvailabePatientsForSession(Integer sessionId) {
		return patientService.getPatientsNotInSession(sessionId);
	}
	
	public void deletePatient() {
		patientService.delete(patient);
		patients = null;
	}
	
	public void savePatient() {
		patientService.save(patient);
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

	public Map<Integer, SessionStats> getPatientSessionStats() {
		return patientSessionStats;
	}

	public SessionStats getPatientSessionStats(Integer patientId, Date sessionDate) {
		List<PatientSession> patientSessions = sessionService.getPatientSessions(patientId, sessionDate);

		int numberOfSessions = patientSessions.size();
		int completedSessions = 0;
		int absentSessions = 0;

		for (PatientSession patientSession : patientSessions) {
			if (patientSession.isAbsent()) {
				absentSessions++;
				continue;
			}

			if (patientSession.getSession().getStatus() == SessionStatus.FINISHED) {
				completedSessions++;
			}
		}

		return new SessionStats(completedSessions, absentSessions, numberOfSessions);
	}

	public static class SessionStats {
		private int numberOfCompletedSessions;
		private int numberOfAbsentSessions;
		private int totalNumberOfSessions;

		public SessionStats(int numberOfCompletedSessions, int numberOfAbsentSessions, int totalNumberOfSessions) {
			this.numberOfCompletedSessions = numberOfCompletedSessions;
			this.numberOfAbsentSessions = numberOfAbsentSessions;
			this.totalNumberOfSessions = totalNumberOfSessions;
		}

		public int getNumberOfCompletedSessions() {
			return numberOfCompletedSessions;
		}

		public int getNumberOfAbsentSessions() {
			return numberOfAbsentSessions;
		}

		public int getTotalNumberOfSessions() {
			return totalNumberOfSessions;
		}
	}
}