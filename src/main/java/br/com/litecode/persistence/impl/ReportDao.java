package br.com.litecode.persistence.impl;

import br.com.litecode.persistence.AbstractDao;

import javax.persistence.Query;
import java.util.List;

public class ReportDao extends AbstractDao<Object[]> {
	public List<Object[]> findMonthlySessions() {
		String sqlString =
				"select date_format(session_time, '%Y-%m') as month, count(*) as numberOfSessions " +
				"from session " +
				"where status = 'FINISHED'" +
				"group by date_format(session_time, '%Y-%m') " +
				"order by month";

		Query query = entityManager.createNativeQuery(sqlString);
		return query.getResultList();
	}

	public List<Object[]> findSessionsPerChamber() {
		String sqlString =
				"select name, count(*) as numberOfSessions " +
				"from session join chamber using (chamber_id) " +
				"group by chamber_id, name";

		Query query = entityManager.createNativeQuery(sqlString);
		return query.getResultList();
	}

	public List<Object[]> findSessionsPerHealthInsurance() {
		String sqlString =
				"select ifnull(health_insurance, 'N/D'), count(*) as numberOfSessions " +
				"from patient_session " +
				"join patient using (patient_id) " +
				"group by health_insurance " +
				"order by count(*) desc";

		Query query = entityManager.createNativeQuery(sqlString);
		return query.getResultList();
	}
}
