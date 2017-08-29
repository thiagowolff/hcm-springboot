package br.com.litecode.controller;

import br.com.litecode.config.SecurityConfig;
import br.com.litecode.controller.SessionController.SessionInput;
import br.com.litecode.domain.model.Chamber;
import br.com.litecode.domain.model.Patient;
import br.com.litecode.domain.model.Session;
import br.com.litecode.domain.model.Session.SessionStatus;
import br.com.litecode.domain.repository.ChamberRepository;
import br.com.litecode.domain.repository.PatientRepository;
import br.com.litecode.domain.repository.SessionRepository;
import br.com.litecode.util.MessageUtil;
import org.apache.shiro.mgt.SecurityManager;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.subject.SubjectContext;
import org.apache.shiro.util.ThreadContext;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.modules.junit4.PowerMockRunnerDelegate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import javax.faces.application.FacesMessage;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@SpringBootTest
@RunWith(PowerMockRunner.class)
@PowerMockRunnerDelegate(SpringRunner.class)
@DataJpaTest
@ComponentScan(basePackages = "br.com.litecode")
@PrepareForTest({ FacesContext.class })
public class TestSessionController {
	@Autowired
	private ChamberRepository chamberRepository;

	@Autowired
	private PatientRepository patientRepository;

	@Autowired
	private SessionRepository sessionRepository;

	@Autowired
	private SessionController sessionController;

	@Mock
	private FacesContext facesContext;

	@Mock
	private ExternalContext externalContext;

	private Chamber chamber;
	private Patient patient;

	@Before
	public void setUp() {
		chamber = chamberRepository.findOne(1);
		patient = patientRepository.findOne(1);

		SecurityManager securityManger = mock(SecurityManager.class);
		Subject subject = mock(Subject.class);

		when(securityManger.createSubject(any(SubjectContext.class))).thenReturn(subject);
		when(subject.getPrincipal()).thenReturn("hcm-test");

		ThreadContext.bind(securityManger);

		PowerMockito.mockStatic(FacesContext.class);
		when(FacesContext.getCurrentInstance()).thenReturn(facesContext);
		when(facesContext.getExternalContext()).thenReturn(externalContext);
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
		SessionInput sessionInput = sessionController.getSessionInput();
		sessionInput.setChamber(chamber);
		sessionInput.getPatients().add(patient);
		sessionInput.setSessionDate(LocalDate.now());
		sessionInput.setSessionTime(LocalTime.now());

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
