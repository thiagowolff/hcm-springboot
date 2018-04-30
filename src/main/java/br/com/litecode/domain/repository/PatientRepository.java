package br.com.litecode.domain.repository;

import br.com.litecode.domain.model.Patient;
import br.com.litecode.domain.model.Patient.PatientStats;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface PatientRepository extends PagingAndSortingRepository<Patient, Integer> {
	@Query("select p from Patient p where active = true order by name")
	List<Patient> findActivePatients();

	@Query(value = "select ps.patient.patientId as patientId, " +
			"sum(case when ps.session.status = 'FINISHED' and ps.absent = false then 1 else 0 end) as completedSessions, " +
			"sum(case when ps.session.status = 'FINISHED' and ps.absent = true then 1 else 0 end) as absentSessions, " +
			"min(ps.session.scheduledTime) as initialSessionDate " +
			"from PatientSession ps where ps.session.scheduledTime < :sessionDate and ps.patient.patientId in (select patient.patientId from PatientSession where session.sessionId = :sessionId) " +
			"group by ps.patient.patientId")
	List<PatientStats> findPatienStats(Integer sessionId, LocalDateTime sessionDate);

	@Query(value = "select ps.patient.patientId as patientId, " +
			"sum(case when ps.session.status = 'FINISHED' and ps.absent = false then 1 else 0 end) as completedSessions, " +
			"sum(case when ps.session.status = 'FINISHED' and ps.absent = true then 1 else 0 end) as absentSessions, " +
			"min(ps.session.scheduledTime) as initialSessionDate " +
			"from PatientSession ps where ps.patient.patientId = :patientId " +
			"group by ps.patient.patientId")
	PatientStats findPatienStats(Integer patientId);

	@Query("select p from Patient p where p not in (select p from Patient p join p.patientSessions ps where ps.session.sessionId = :sessionId) order by p.name, p.patientId")
	List<Patient> findPatientsNotInSession(Integer sessionId);
}