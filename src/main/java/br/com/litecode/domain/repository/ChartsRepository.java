package br.com.litecode.domain.repository;

import br.com.litecode.domain.model.Session;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;

import java.io.Serializable;
import java.util.List;

public interface ChartsRepository extends Repository<Session, Serializable> {
	@Query(value = "select cast(date_part('year', scheduled_time) as integer) as year, cast(date_part('month', scheduled_time) as integer) as month, count(*) from session s join patient_session ps using (session_id) where s.status = 'FINISHED' and not ps.absent group by year, month order by year, month", nativeQuery = true)
	List<Object[]> findMonthlyPresences();

	@Query(value = "select cast(date_part('year', scheduled_time) as integer) as year, cast(date_part('month', scheduled_time) as integer) as month, count(*) from session s join patient_session ps using (session_id) where s.status = 'FINISHED' and ps.absent group by year, month order by year, month", nativeQuery = true)
	List<Object[]> findMonthlyAbsences();

	@Query(value = "select cast(date_part('year', scheduled_time) as integer) as year, count(*) from session s join patient_session ps using (session_id) where s.status = 'FINISHED' and not ps.absent group by year order by year", nativeQuery = true)
	List<Object[]> findYearlyPresences();

	@Query(value = "select cast(date_part('year', scheduled_time) as integer) as year, count(*) from session s join patient_session ps using (session_id) where s.status = 'FINISHED' and ps.absent group by year order by year", nativeQuery = true)
	List<Object[]> findYearlyAbsences();

	@Query(value = "select cast(date_part('year', consultation_date) as integer) as year, cast(date_part('month', consultation_date) as integer) as month, count(*) from patient group by year, month order by year, month", nativeQuery = true)
	List<Object[]> findMonthlyConsultations();

	@Query(value = "select cast(date_part('year', consultation_date) as integer) as year, count(*) from patient group by year order by year", nativeQuery = true)
	List<Object[]> findYearlyConsultations();

	@Query(value = "select coalesce(hs.name, 'N/D'), count(*) from patient_session join patient using (patient_id) left join patient_data hs on patient_data_id = health_insurance_id group by hs.name order by count(*) desc", nativeQuery = true)
	List<Object[]> findSessionsPerHealthInsurance();

	@Query(value = "select case when medical_indication then 'Sim' else case when not medical_indication then 'Não' else 'N/D' end end, count(*) from patient group by medical_indication", nativeQuery = true)
	List<Object[]> findPatientsPerMedicalIndication();

	@Query(value = "select coalesce(cr.name, 'N/D') as consultation_reason, count(*) from patient left join patient_data cr on patient_data_id = consultation_reason_id group by consultation_reason order by count(*) desc", nativeQuery = true)
	List<Object[]> findPatientsPerConsultationReason();

	@Query(value = "select to_char(created_date, 'yyyy-mm') as month, count(*) from patient where created_date >= now() - interval '12 months' group by month order by month", nativeQuery = true)
	List<Object[]> findMonthlyNewPatients();

	@Query(value = "select cast(date_part('year', scheduled_time) as integer) as year, cast(date_part('week', scheduled_time) as integer) as week, sum(case when not ps.absent then 1 else 0 end) as presences, sum(case when ps.absent then 1 else 0 end) as absences from session s join patient_session ps using (session_id) where s.status = 'FINISHED' and ps.patient_id = :patientId group by year, week order by year desc, week desc limit 12", nativeQuery = true)
	List<Object[]> findPatientAttendance(Integer patientId);

	@Query(value = "select cast(date_part('day', scheduled_time) as integer) as day, sum(case when not ps.absent then 1 else 0 end) as presences, sum(case when ps.absent then 1 else 0 end) as absences from session s join patient_session ps using (session_id) where s.status = 'FINISHED' and cast(date_part('year', scheduled_time) as integer) = :year and cast(date_part('month', scheduled_time) as integer) = :month group by day order by day", nativeQuery = true)
	List<Object[]> findMonthlyAttendance(int year, int month);
}