package br.com.litecode.controller;

import br.com.litecode.Application;
import br.com.litecode.domain.model.Patient;
import br.com.litecode.domain.model.Session;
import br.com.litecode.domain.repository.PatientRepository;
import br.com.litecode.domain.repository.SessionRepository;
import br.com.litecode.service.timer.Clock;
import br.com.litecode.service.timer.FakeSessionClock;
import com.github.benmanes.caffeine.cache.Ticker;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.cache.support.NoOpCacheManager;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

public class TestPatientController extends BaseControllerTest {
	@Autowired
	private PatientRepository patientRepository;

	@Autowired
	private SessionRepository sessionRepository;

	@Autowired
	private PatientController patientController;

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
		session.setStatus(Session.SessionStatus.FINISHED);
		sessionRepository.save(session);

		assertThat(patientController.getPatientStats(patient.getPatientId()).getCompletedSessions()).isEqualTo(1);
	}
}
