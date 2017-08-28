package br.com.litecode.domain.repository;

import br.com.litecode.domain.model.Chamber;
import br.com.litecode.domain.model.Patient;
import br.com.litecode.domain.model.Session;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest
@DataJpaTest
public class SessionRepositoryTest {
    @Autowired
    private ChamberRepository chamberRepository;

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private SessionRepository sessionRepository;

    private Chamber chamber;
    private Session session;
    private Patient patientX;
    private Patient patientY;

    @Before
    public void setUp() {
        chamber = new Chamber();
        chamber.setName("Chamber");
        chamber = chamberRepository.save(chamber);

        patientX = new Patient();
        patientX.setName("Patient X");
        patientX = patientRepository.save(patientX);

        patientY = new Patient();
        patientY.setName("Patient Y");
        patientY = patientRepository.save(patientY);

        session = new Session();
        session.setChamber(chamber);
        session = sessionRepository.save(session);
    }

    @Test
    public void createEmptySession() throws Exception {
        Session session = new Session();
        session.setChamber(chamber);
        session.setScheduledTime(LocalDateTime.now());
        sessionRepository.save(session);

        assertThat(session.getPatientSessions()).hasSize(0);
    }

    @Test
    public void createSession() throws Exception {
        Session session = new Session();
        session.setChamber(chamber);
		session.addPatient(patientX);
		session.addPatient(patientY);
        session = sessionRepository.save(session);

        Session createdSession = sessionRepository.findOne(session.getSessionId());

        assertThat(createdSession.getChamber().getChamberId()).isEqualTo(chamber.getChamberId());
        assertThat(createdSession.getPatientSessions()).hasSize(2);
    }

    @Test
    public void addPatientToSession() throws Exception {
        session.addPatient(patientX);
        session.addPatient(patientY);
        sessionRepository.save(session);

        assertThat(session.getPatientSessions()).hasSize(2);
    }

    @Test
    public void removePatientFromSession() throws Exception {
        session.addPatient(patientX);
        session.addPatient(patientY);
        sessionRepository.save(session);

        session.getPatientSessions().remove(session.getPatientSessions().iterator().next());
        sessionRepository.save(session);

        assertThat(session.getPatientSessions()).hasSize(1);
    }
}