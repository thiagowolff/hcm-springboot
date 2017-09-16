package br.com.litecode.controller;

import br.com.litecode.domain.model.Patient;
import br.com.litecode.domain.repository.PatientRepository;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;

public class TestPatientController extends BaseControllerTest {
	@Autowired
	private PatientRepository patientRepository;

	@Autowired
	private PatientController patientController;

	private Patient patient;

	@Before
	public void setUp() {
		patient = patientRepository.findOne(1);
	}

	@Test
	public void getPatients() {
		//assertThat(patientController.getPatients()).hasSize(3);
	}

	@Test
	public void addPatient() {
		Patient newPatient  = new Patient();
		newPatient.setName("Patient A");

		patientController.setPatient(newPatient);
		patientController.savePatient();

		//assertThat(patientController.getPatients()).hasSize(4);
	}

	@Test
	public void deletePatient() {
		patientController.setPatient(patient);
		patientController.deletePatient();

		//assertThat(patientController.getPatients()).hasSize(2);
		assertThat(patient.isActive()).isFalse();
	}
}
