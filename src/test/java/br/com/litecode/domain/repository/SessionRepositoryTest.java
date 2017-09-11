package br.com.litecode.domain.repository;

import br.com.litecode.domain.model.Chamber;
import br.com.litecode.domain.model.Patient;
import br.com.litecode.domain.model.PatientSession;
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
        chamber = chamberRepository.findOne(1);
        session = sessionRepository.findOne(1);
        patientX = patientRepository.findOne(1);
        patientY = patientRepository.findOne(2);
    }

    @Test
    public void createEmptySession() {
        Session session = new Session();
        session.setChamber(chamber);
        session.setScheduledTime(LocalDateTime.now());
        sessionRepository.save(session);

        assertThat(session.getPatientSessions()).hasSize(0);
    }

    @Test
    public void createSession() {
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
    public void addPatientToSession() {
        session.addPatient(patientX);
        session.addPatient(patientY);
        sessionRepository.save(session);

        assertThat(session.getPatientSessions()).hasSize(2);
    }

    @Test
    public void updatePatientSession() {
        session.addPatient(patientX);
        session.addPatient(patientY);
        session = sessionRepository.save(session);

        for (PatientSession patientSession : session.getPatientSessions()) {
            assertThat(patientSession.isAbsent()).isFalse();
        }

        session.getPatientSessions().first().setAbsent(true);
        session = sessionRepository.save(session);

        assertThat(session.getPatientSessions().first().isAbsent()).isTrue();
        assertThat(session.getPatientSessions().last().isAbsent()).isFalse();
    }

    @Test
    public void removePatientFromSession() {
        session.addPatient(patientX);
        session.addPatient(patientY);
        sessionRepository.save(session);

        session.getPatientSessions().remove(session.getPatientSessions().first());
        sessionRepository.save(session);

        assertThat(session.getPatientSessions()).hasSize(1);
    }

    @Test
    public void deleteSession() {
        sessionRepository.delete(session);
        Session deletedSession = sessionRepository.findOne(session.getSessionId());
        assertThat(deletedSession).isNull();
    }
}