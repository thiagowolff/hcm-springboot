package br.com.litecode.domain.repository;

import br.com.litecode.domain.model.Session;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;

import java.io.Serializable;
import java.util.List;

public interface ChartsRepository extends Repository<Session, Serializable> {
	int STARTING_YEAR = 2017;

	String CTE_SESSIONS =
			"with sessions as (" +
			"    select " +
			"	     cast(date_part('year', scheduled_time) as integer) as year," +
			"        cast(date_part('month', scheduled_time) as integer) as month," +
			"        cast(date_part('day', scheduled_time) as integer) as day," +
			"        session_id, " +
			"		 chamber_id, " +
			"		 patient_id, " +
			"		 scheduled_time, " +
			"		 status, " +
			"		 absent" +
			"    from session s" +
			"    join patient_session ps using (session_id)" +
			"	 where cast(date_part('year', scheduled_time) as integer) >= " + STARTING_YEAR +
			") ";

	@Query(value = CTE_SESSIONS + "select year, month, count(*) from sessions where status = 'FINISHED' and not absent group by year, month order by year, month", nativeQuery = true)
	List<Object[]> findMonthlyPresences();

	@Query(value = CTE_SESSIONS + "select year, month, count(*) from sessions where status = 'FINISHED' and absent group by year, month order by year, month", nativeQuery = true)
	List<Object[]> findMonthlyAbsences();

	@Query(value = CTE_SESSIONS + "select year, count(*) from sessions where status = 'FINISHED' and not absent group by year order by year", nativeQuery = true)
	List<Object[]> findYearlyPresences();

	@Query(value = CTE_SESSIONS + "select year, count(*) from sessions where status = 'FINISHED' and absent group by year order by year", nativeQuery = true)
	List<Object[]> findYearlyAbsences();

	@Query(value = CTE_SESSIONS + "select year, cast(date_part('week', scheduled_time) as integer) as week, sum(case when not absent then 1 else 0 end) as presences, sum(case when absent then 1 else 0 end) as absences from sessions where status = 'FINISHED' and patient_id = :patientId group by year, week order by year desc, week desc limit 12", nativeQuery = true)
	List<Object[]> findPatientAttendance(Integer patientId);

	@Query(value = CTE_SESSIONS + "select day, sum(case when not absent then 1 else 0 end) as presences, sum(case when absent then 1 else 0 end) as absences from sessions where status = 'FINISHED' and year = :year and month = :month group by day order by day", nativeQuery = true)
	List<Object[]> findMonthlyAttendance(int year, int month);

	@Query(value = "select cast(date_part('year', consultation_date) as integer) as year, cast(date_part('month', consultation_date) as integer) as month, count(*) from patient where cast(date_part('year', consultation_date) as integer) >= " + STARTING_YEAR + " group by year, month order by year, month", nativeQuery = true)
	List<Object[]> findMonthlyConsultations();

	@Query(value = "select cast(date_part('year', consultation_date) as integer) as year, count(*) from patient where cast(date_part('year', consultation_date) as integer) >= " + STARTING_YEAR + " group by year order by year", nativeQuery = true)
	List<Object[]> findYearlyConsultations();

	@Query(value = "select coalesce(hs.name, 'N/D'), count(*) from patient_session join patient using (patient_id) left join patient_data hs on patient_data_id = health_insurance_id group by hs.name order by count(*) desc", nativeQuery = true)
	List<Object[]> findSessionsPerHealthInsurance();

	@Query(value = "select case when medical_indication then 'Sim' else case when not medical_indication then 'Não' else 'N/D' end end, count(*) from patient group by medical_indication", nativeQuery = true)
	List<Object[]> findPatientsPerMedicalIndication();

	@Query(value = "select coalesce(cr.name, 'N/D') as consultation_reason, count(*) from patient left join patient_data cr on patient_data_id = consultation_reason_id group by consultation_reason order by count(*) desc", nativeQuery = true)
	List<Object[]> findPatientsPerConsultationReason();

	@Query(value = "select coalesce(cr.name, 'N/D') as treatment_status, count(*) from patient left join patient_data cr on patient_data_id = patient_status_id group by treatment_status order by count(*) desc", nativeQuery = true)
	List<Object[]> findPatientsPerTreatmentStatus();

	@Query(value = "select to_char(created_date, 'yyyy-mm') as month, count(*) from patient where created_date >= now() - interval '12 months' group by month order by month", nativeQuery = true)
	List<Object[]> findMonthlyNewPatients();
}