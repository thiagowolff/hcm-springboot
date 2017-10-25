package br.com.litecode.service.timer;

import br.com.litecode.domain.model.ChamberEvent;
import br.com.litecode.domain.model.Patient.PatientStats;
import br.com.litecode.domain.model.PatientSession;
import br.com.litecode.domain.model.Session;
import br.com.litecode.domain.model.Session.SessionStatus;
import br.com.litecode.domain.repository.PatientRepository;
import br.com.litecode.domain.repository.SessionRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest
@DataJpaTest
@EnableAutoConfiguration
@ComponentScan(basePackages = "br.com.litecode")
public class TestChamberSessionTimer {
	@Autowired
	private SessionRepository sessionRepository;

	@Autowired
	private PatientRepository patientRepository;

	@Autowired
	private SessionTimer chamberSessionTimer;

	@Test
	public void playSession() {
		Session session = sessionRepository.findOne(1);
		chamberSessionTimer.startSession(session);

		long numberOfAbsentPatients = session.getPatientSessions().stream().filter(PatientSession::isAbsent).count();

		for (ChamberEvent chamberEvent : session.getChamber().getChamberEvents()) {
			chamberSessionTimer.getSessionClock().elapseTime(session);
			assertThat(chamberEvent.getEventType().getSessionStatus()).isEqualTo(session.getStatus());
		}

		assertThat(session.getStatus()).isEqualTo(SessionStatus.FINISHED);
		assertThat(session.getPatientSessions().stream().filter(PatientSession::isAbsent).count()).isEqualTo(numberOfAbsentPatients);

		List<PatientStats> patientStats = patientRepository.findPatienStats(session.getSessionId(), LocalDateTime.now());
		patientStats.forEach(ps -> assertThat(ps.getCompletedSessions()).isEqualTo(1));
	}
}
