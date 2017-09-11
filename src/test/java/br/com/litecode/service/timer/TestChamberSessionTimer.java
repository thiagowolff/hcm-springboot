package br.com.litecode.service.timer;

import br.com.litecode.Application;
import br.com.litecode.domain.model.ChamberEvent;
import br.com.litecode.domain.model.Session;
import br.com.litecode.domain.repository.SessionRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit4.SpringRunner;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest
@DataJpaTest
@ComponentScan(basePackages = "br.com.litecode")
public class TestChamberSessionTimer {

	@Configuration
	@Import(Application.class)
	public static class TestConfig {
		@Bean
		public Clock sessionClock() {
			return new FakeSessionClock();
		}
	}

	@Autowired
	private SessionRepository sessionRepository;

	@Autowired
	private ChamberSessionTimer chamberSessionTimer;

	@Before
	public void setUp() {
	}

	@Test
	public void playSession() {
		Session session = sessionRepository.findOne(1);
		chamberSessionTimer.startSession(session);

		for (ChamberEvent chamberEvent : session.getChamber().getChamberEvents()) {
			chamberSessionTimer.getSessionClock().elapseTime(session);
			assertThat(chamberEvent.getEventType().getSessionStatus()).isEqualTo(session.getStatus());
		}
	}
}
