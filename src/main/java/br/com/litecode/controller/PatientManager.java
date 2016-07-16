package br.com.litecode.controller;

import br.com.litecode.domain.Patient;
import br.com.litecode.service.PatientService;
import br.com.litecode.service.UserService;

import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.Serializable;
import java.util.List;

@Named
@ViewScoped
public class PatientManager implements Serializable {
	private static final long serialVersionUID = 1L;

	@Inject	private PatientService patientService;
	@Inject private UserService userService;

	private Patient patient;
	private List<Patient> patients;
	private List<Patient> filteredPatients;
	
	public PatientManager() {
		patient = new Patient();
	}

	public List<Patient> getPatients() {
		if (patients == null) {
			patients = patientService.getPatients();
		}
		return patients;
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
}