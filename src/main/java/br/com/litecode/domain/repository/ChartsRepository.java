package br.com.litecode.domain.repository;

import br.com.litecode.domain.model.Session;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;

import java.io.Serializable;
import java.util.List;

public interface ChartsRepository extends Repository<Session, Serializable> {
	@Query(value = "select date_format(scheduled_time, '%Y-%m') month, count(*) from session s join patient_session ps using (session_id) where s.status = 'FINISHED' and ps.absent = 0 group by month order by month", nativeQuery = true)
	List<Object[]> findMonthlyPresences();

	@Query(value = "select date_format(scheduled_time, '%Y') year, count(*) from session s join patient_session ps using (session_id) where s.status = 'FINISHED' and ps.absent = 0 group by year order by year", nativeQuery = true)
	List<Object[]> findYearlyPresences();

	@Query(value = "select date_format(scheduled_time, '%Y-%m') month, count(*) from session s join patient_session ps using (session_id) where s.status = 'FINISHED' and ps.absent = 1 group by month order by month", nativeQuery = true)
	List<Object[]> findMonthlyAbsences();

	@Query(value = "select date_format(scheduled_time, '%Y') year, count(*) from session s join patient_session ps using (session_id) where s.status = 'FINISHED' and ps.absent = 1 group by year order by year", nativeQuery = true)
	List<Object[]> findYearlyAbsences();

	@Query(value = "select date_format(consultation_date, '%Y-%m') month, count(*) from patient group by month order by month;", nativeQuery = true)
	List<Object[]> findMonthlyConsultations();

	@Query(value = "select date_format(consultation_date, '%Y') year, count(*) from patient group by year order by year;", nativeQuery = true)
	List<Object[]> findYearlyConsultations();

	@Query(value = "select ifnull(hs.name, 'N/D'), count(*) from patient_session join patient using (patient_id) left join patient_data hs on patient_data_id = health_insurance_id group by hs.name order by count(*) desc", nativeQuery = true)
	List<Object[]> findSessionsPerHealthInsurance();

	@Query(value = "select case when medical_indication = 1 then 'Sim' else case when medical_indication = 0 then 'Não' else 'N/D' end end, count(*) from patient group by medical_indication", nativeQuery = true)
	List<Object[]> findPatientsPerMedicalIndication();

	@Query(value = "select ifnull(cr.name, 'N/D'), count(*) from patient left join patient_data cr on patient_data_id = consultation_reason_id group by consultation_reason_id order by count(*) desc", nativeQuery = true)
	List<Object[]> findPatientsPerConsultationReason();

	@Query(value = "select date_format(created_date, '%Y-%m') month, count(*) from patient where created_date >= now() - interval 12 month group by date_format(created_date, '%Y-%m') order by month", nativeQuery = true)
	List<Object[]> findMonthlyNewPatients();

	@Query(value = "select date_format(scheduled_time, '%Y-%m-%d') date, sum(case when ps.absent = 0 then 1 else 0 end) from session s join patient_session ps using (session_id) where s.status = 'FINISHED' and ps.patient_id = :patientId group by date order by date", nativeQuery = true)
	List<Object[]> findPatientAttendance(Integer patientId);
}