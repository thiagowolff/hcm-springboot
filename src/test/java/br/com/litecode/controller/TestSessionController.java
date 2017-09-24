package br.com.litecode.controller;

import br.com.litecode.Application;
import br.com.litecode.controller.SessionController.SessionInput;
import br.com.litecode.domain.model.Chamber;
import br.com.litecode.domain.model.Patient;
import br.com.litecode.domain.model.Session;
import br.com.litecode.domain.model.Session.SessionStatus;
import br.com.litecode.domain.repository.ChamberRepository;
import br.com.litecode.domain.repository.PatientRepository;
import br.com.litecode.domain.repository.SessionRepository;
import br.com.litecode.service.timer.ChamberSessionTimer;
import br.com.litecode.service.timer.Clock;
import br.com.litecode.service.timer.FakeSessionClock;
import br.com.litecode.util.MessageUtil;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.modules.junit4.PowerMockRunnerDelegate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit4.SpringRunner;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

public class TestSessionController extends BaseControllerTest {
	@Configuration
	@Import(Application.class)
	public static class TestConfig {
		@Bean
		public Clock sessionClock() {
			return new FakeSessionClock();
		}
	}

	@Autowired
	private ChamberRepository chamberRepository;

	@Autowired
	private PatientRepository patientRepository;

	@Autowired
	private SessionRepository sessionRepository;

	@Autowired
	private SessionController sessionController;

	@Autowired
	private ChamberSessionTimer chamberSessionTimer;


	private Chamber chamber;
	private Patient patient;

	private SessionInput sessionInput;

	@Before
	public void setUp() {
		chamber = chamberRepository.findOne(1);
		patient = patientRepository.findOne(1);
		sessionInput = sessionController.getSessionInput();
	}

	@Test
	public void getChamberSessions() {
		Session session = sessionRepository.findOne(1);
		List<Session> sessions = sessionController.getSessions(chamber.getChamberId(), session.getScheduledTime().toLocalDate());
		assertThat(sessions).hasSize(1);
	}

	@Test
	public void addSession() {
		SessionInput sessionInput = SessionInput.of(chamber, LocalDate.now(), LocalTime.now(), patient);
		sessionController.setSessionInput(sessionInput);
		sessionController.addSession();

		Session session = sessionRepository.findSessionsByChamberAndDate(chamber.getChamberId(), LocalDate.now()).get(0);

		assertThat(session.getStatus()).isEqualTo(SessionStatus.CREATED);
		assertThat(session.getPatientSessions()).hasSize(1);
	}

	@Test
	public void addSessionExceededCapacity() {
		chamber.setCapacity(0);
		sessionInput.setChamber(chamber);
		sessionInput.getPatients().add(patient);
		sessionInput.setSessionDate(LocalDate.now());
		sessionInput.setSessionTime(LocalTime.now());

		sessionController.addSession();

		ArgumentCaptor<FacesMessage> facesMessageCaptor = ArgumentCaptor.forClass(FacesMessage.class);
		verify(facesContext).addMessage(Mockito.nullable(String.class), facesMessageCaptor.capture());

		FacesMessage message = facesMessageCaptor.getValue();
		assertThat(message.getSeverity()).isEqualTo(FacesMessage.SEVERITY_ERROR);
		assertThat(message.getDetail()).isEqualTo(MessageUtil.getMessage("error.chamberPatientsLimitExceeded", sessionInput.getChamber().getCapacity()));
	}

	@Test
	public void deleteSession() {
		Session session = sessionRepository.findOne(1);
		sessionController.deleteSession(session);

		assertThat(sessionRepository.findOne(session.getSessionId())).isNull();
	}

	@Test
	public void addPatientsToSession() {
		Patient patientA = patientRepository.save(new Patient("Patient A"));
		Patient patientB = patientRepository.save(new Patient("Patient B"));
		Session session = sessionRepository.findOne(1);
		sessionInput.setSession(session);
		sessionInput.getPatients().add(patientA);
		sessionInput.getPatients().add(patientB);

		sessionController.addPatientsToSession(session);

		assertThat(session.getPatientSessions()).hasSize(4);
	}

	@Test
	public void removePatientFromSession() {
		Session session = sessionRepository.findOne(1);
		sessionInput.setSession(session);

		sessionController.removePatientFromSession(sessionInput.getSession().getPatientSessions().first());

		assertThat(session.getPatientSessions()).hasSize(1);
	}

	@Test
	public void updatePatientSessionStatus() {
		Session session = sessionRepository.findOne(1);
		sessionInput.setSession(session);

		sessionController.setPatientSessionStatus(sessionInput.getSession().getPatientSessions().first(), true);

		assertThat(session.getPatientSessions()).hasSize(2);
		assertThat(session.getPatientSessions().first().isAbsent()).isTrue();
	}

	@Test
	public void duplicateSessions() {
		Session session = sessionRepository.findOne(1);
		sessionController.setFromSessionsDate(session.getScheduledTime().toLocalDate());
		sessionController.setToSessionsDate(LocalDate.now());

		sessionController.duplicateSessions();

		List<Session> sessions = sessionRepository.findSessionsByDate(LocalDate.now());

		assertThat(sessions).hasSize(1);
		assertThat(sessions.get(0).getScheduledTime().toLocalTime()).isEqualTo(session.getScheduledTime().toLocalTime());
		assertThat(sessions.get(0).getPatientSessions()).isEqualTo(session.getPatientSessions());
		assertThat(sessions.get(0).getStatus()).isEqualTo(SessionStatus.CREATED);
	}

	@Test
	public void startSession() {
		Session session = sessionRepository.findOne(1);
		sessionController.startSession(session);
		chamberSessionTimer.getSessionClock().elapseTime(session);

		Session startedSession = sessionRepository.findOne(session.getSessionId());
		assertThat(startedSession.isRunning()).isTrue();
	}

	@Test
	public void finishSession() {
		Session session = sessionRepository.findOne(1);

		sessionController.finishSession(session);

		assertThat(session.isRunning()).isFalse();
		assertThat(session.getCurrentProgress()).isEqualTo(100);
		assertThat(session.getStatus()).isEqualTo(SessionStatus.FINISHED);
	}

	@Test
	public void resetSession() {
		Session session = sessionRepository.findOne(1);
		sessionController.finishSession(session);
		assertThat(session.getStatus()).isEqualTo(SessionStatus.FINISHED);

		sessionController.resetSession(session);
		assertThat(session.isRunning()).isFalse();
		assertThat(session.getCurrentProgress()).isEqualTo(0);
		assertThat(session.getStatus()).isEqualTo(SessionStatus.CREATED);
	}
}

