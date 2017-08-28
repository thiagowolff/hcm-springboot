package br.com.litecode.controller;

import br.com.litecode.controller.SessionController.SessionInput;
import br.com.litecode.domain.model.Chamber;
import br.com.litecode.domain.model.Patient;
import br.com.litecode.domain.repository.ChamberRepository;
import br.com.litecode.domain.repository.PatientRepository;
import br.com.litecode.domain.repository.SessionRepository;
import org.apache.shiro.mgt.SecurityManager;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.subject.SubjectContext;
import org.apache.shiro.util.ThreadContext;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@RunWith(SpringRunner.class)
@SpringBootTest
//@WebAppConfiguration
@DataJpaTest
@ComponentScan(basePackages = "br.com.litecode")
public class TestSessionController {
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

	private SessionInput sessionInput;

	@Before
	public void setUp() {
		chamber = chamberRepository.findOne(1);
		patient = patientRepository.findOne(1);

		SecurityManager securityManger = mock(SecurityManager.class);
		Subject subject = mock(Subject.class);

		when(securityManger.createSubject(any(SubjectContext.class))).thenReturn(subject);
		when(subject.getPrincipal()).thenReturn("admin");

		ThreadContext.bind(securityManger);
	}

	@Test
	public void addSession() {
		sessionController.getSessionInput().setChamber(chamber);
		sessionController.getSessionInput().getPatients().add(patient);
		sessionController.getSessionInput().setSessionDate(LocalDate.now());
		sessionController.getSessionInput().setSessionTime(LocalTime.now());

		sessionController.addSession();

		assertThat(sessionRepository.findOne(1).getSessionId()).isEqualTo(1);
	}
}
