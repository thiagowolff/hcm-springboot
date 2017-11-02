package br.com.litecode.service.timer;

import br.com.litecode.domain.model.ChamberEvent;
import br.com.litecode.domain.model.Patient.PatientStats;
import br.com.litecode.domain.model.PatientSession;
import br.com.litecode.domain.model.Session;
import br.com.litecode.domain.model.Session.SessionStatus;
import br.com.litecode.domain.repository.PatientRepository;
import br.com.litecode.domain.repository.SessionRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
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

	private Session session;

	@Before
	public void setUp() {
		session = sessionRepository.findOne(1);
		session.setClock(getFixedClockAt(session.getScheduledTime()));
	}

	@Test
	public void playSession() {
		long numberOfAbsentPatients = session.getPatientSessions().stream().filter(PatientSession::isAbsent).count();

		chamberSessionTimer.startSession(session);
		for (ChamberEvent chamberEvent : session.getChamber().getChamberEvents()) {
			chamberSessionTimer.getSessionTimeTicker().elapseTime(session);
			assertThat(chamberEvent.getEventType().getSessionStatus()).isEqualTo(session.getStatus());

			session.setClock(getFixedClockAt(session.getScheduledTime().plusSeconds(chamberEvent.getTimeout())));
			System.out.println(session.getTimeRemaining());
			System.out.println(session.getCurrentProgress());
		}

		assertThat(session.getStatus()).isEqualTo(SessionStatus.FINISHED);
		assertThat(session.getPatientSessions().stream().filter(PatientSession::isAbsent).count()).isEqualTo(numberOfAbsentPatients);

		List<PatientStats> patientStats = patientRepository.findPatienStats(session.getSessionId(), LocalDateTime.now());
		patientStats.forEach(ps -> assertThat(ps.getCompletedSessions()).isEqualTo(1));
	}

	private Clock getFixedClockAt(LocalDateTime dateTime){
		ZoneId zoneId = ZoneId.systemDefault();
		Instant instant = dateTime.atZone(zoneId).toInstant();
		return Clock.fixed(instant, zoneId);
	}
}
