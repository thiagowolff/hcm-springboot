package br.com.litecode.controller;

import br.com.litecode.controller.SessionController.SessionInput;
import br.com.litecode.domain.model.Chamber;
import br.com.litecode.domain.model.Patient;
import br.com.litecode.domain.model.Session;
import br.com.litecode.domain.model.Session.SessionStatus;
import br.com.litecode.domain.repository.ChamberRepository;
import br.com.litecode.domain.repository.PatientRepository;
import br.com.litecode.domain.repository.SessionRepository;
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
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.junit4.SpringRunner;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

public class TestSessionController extends BaseControllerTest {
	@Autowired
	private ChamberRepository chamberRepository;

	@Autowired
	private PatientRepository patientRepository;

	@Autowired
	private SessionRepository sessionRepository;

	@Autowired
	private SessionController sessionController;

	private Chamber chamber;
	private Patient patient;

	@Before
	public void setUp() {
		chamber = chamberRepository.findOne(1);
		patient = patientRepository.findOne(1);
	}

	@Test
	public void getChamberSessions() {
		Session session = sessionRepository.findOne(1);
		SessionInput sessionInput = sessionController.getSessionInput();
		sessionInput.setSessionDate(session.getScheduledTime().toLocalDate());

		List<Session> sessions = sessionController.getSessions(chamber.getChamberId());
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
		SessionInput sessionInput = sessionController.getSessionInput();
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
		sessionController.getSessionInput().setSession(session);
		sessionController.deleteSession();

		assertThat(sessionRepository.findOne(session.getSessionId())).isNull();
	}
}
