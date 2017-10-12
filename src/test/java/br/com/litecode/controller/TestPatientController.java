package br.com.litecode.controller;

import br.com.litecode.Application;
import br.com.litecode.domain.model.Patient;
import br.com.litecode.domain.model.Patient.PatientStats;
import br.com.litecode.domain.model.Session;
import br.com.litecode.domain.repository.PatientRepository;
import br.com.litecode.domain.repository.SessionRepository;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.cache.support.NoOpCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import java.time.LocalDate;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class TestPatientController extends BaseControllerTest {
	@Autowired
	private PatientRepository patientRepository;

	@Autowired
	private SessionRepository sessionRepository;

	@Autowired
	private PatientController patientController;

	@Autowired
	private SessionController sessionController;

	@Configuration
	@Import(Application.class)
	public static class TestConfig {
		@Bean
		public CacheManager cacheManager() {
			return new NoOpCacheManager();
		}
	}

	@Test
	public void addPatient() {
		Patient newPatient  = new Patient();
		newPatient.setName("Patient A");

		patientController.setPatient(newPatient);
		patientController.savePatient();

		assertThat(patientController.getPatients()).hasSize(4);
	}

	@Test
	public void deletePatient() {
		Patient patientToDelete = patientRepository.findOne(1);
		patientController.setPatient(patientToDelete);
		patientController.deletePatient();

		assertThat(patientController.getPatients()).hasSize(2);
		assertThat(patientToDelete.isActive()).isFalse();
	}

	@Test
	public void patientStats() {
		Patient patient = patientRepository.findOne(1);
		assertThat(patientController.getPatientStats(patient.getPatientId()).getCompletedSessions()).isEqualTo(0);

		Session session = sessionRepository.findOne(1);
		sessionController.finishSession(session);

		assertThat(patientController.getPatientStats(patient.getPatientId()).getCompletedSessions()).isEqualTo(1);
	}

	@Test
	public void sessionPatientStats() {
		Session session = sessionRepository.findOne(1);

		Map<Integer, PatientStats> patientStatsBefore = patientController.getPatientStats(session, LocalDate.now());
		sessionController.finishSession(session);
		Map<Integer, PatientStats> patientStatsAfter = patientController.getPatientStats(session, LocalDate.now());

		for (PatientStats patientStats : patientStatsBefore.values()) {
			assertThat(patientStatsAfter.get(patientStats.getPatientId()).getCompletedSessions()).isEqualTo(patientStats.getCompletedSessions() + 1);
		}
	}
}
