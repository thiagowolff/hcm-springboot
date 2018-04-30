package br.com.litecode.domain.repository;

import br.com.litecode.domain.model.Session;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;

import java.io.Serializable;
import java.util.List;

public interface ChartsRepository extends Repository<Session, Serializable> {
	@Query(value = "select date_format(scheduled_time, '%Y-%m'), count(*) from session s join patient_session ps using (session_id) where s.status = 'FINISHED' and ps.absent = 0 group by date_format(scheduled_time, '%Y-%m') order by 1", nativeQuery = true)
	List<Object[]> findMonthlyPresences();

	@Query(value = "select date_format(scheduled_time, '%Y-%m'), count(*) from session where status = 'FINISHED' group by date_format(scheduled_time, '%Y-%m') order by 1", nativeQuery = true)
	List<Object[]> findMonthlySessions();

	@Query(value = "select name, count(*) from session join chamber using (chamber_id) group by name", nativeQuery = true)
	List<Object[]> findSessionsPerChamber();

	@Query(value = "select ifnull(hs.name, 'N/D'), count(*) from patient_session join patient using (patient_id) left join health_insurance hs using (health_insurance_id) group by hs.name order by count(*) desc", nativeQuery = true)
	List<Object[]> findSessionsPerHealthInsurance();

	@Query(value = "select case when medical_indication = 1 then 'Sim' else case when medical_indication = 0 then 'Não' else 'N/D' end end, count(*) from patient group by medical_indication", nativeQuery = true)
	List<Object[]> findPatientsPerMedicalIndication();

	@Query(value = "select ifnull(cr.name, 'N/D'), count(*) from patient left join consultation_reason cr using (consultation_reason_id) group by consultation_reason_id order by count(*) desc", nativeQuery = true)
	List<Object[]> findPatientsPerConsultationReason();
}