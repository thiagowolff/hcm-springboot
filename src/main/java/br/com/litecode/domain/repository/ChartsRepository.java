package br.com.litecode.domain.repository;

import br.com.litecode.domain.model.Session;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;

import java.io.Serializable;
import java.util.List;

public interface ChartsRepository extends Repository<Session, Serializable> {
	@Query(value = "select date_format(scheduled_time, '%y-%m'), count(*) from session s join patient_session ps using (session_id) where s.status = 'FINISHED' and ps.absent = 0 group by date_format(scheduled_time, '%Y-%m') order by 1", nativeQuery = true)
	List<Object[]> findMonthlyPresences();

	@Query(value = "select date_format(scheduled_time, '%y-%m'), count(*) from session where status = 'FINISHED' group by date_format(scheduled_time, '%Y-%m') order by 1", nativeQuery = true)
	List<Object[]> findMonthlySessions();

	@Query(value = "select name, count(*) from session join chamber using (chamber_id) group by chamber_id, name", nativeQuery = true)
	List<Object[]> findSessionsPerChamber();

	@Query(value = "select ifnull(health_insurance, 'N/D'), count(*) from patient_session join patient using (patient_id) group by health_insurance order by count(*) desc", nativeQuery = true)
	List<Object[]> findSessionsPerHealthInsurance();
}