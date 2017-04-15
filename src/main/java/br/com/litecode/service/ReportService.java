package br.com.litecode.service;

import br.com.litecode.persistence.impl.ReportDao;

import javax.ejb.Stateless;
import javax.inject.Inject;
import java.util.List;

@Stateless
public class ReportService {
	@Inject private ReportDao reportDao;
	
	public List<Object[]> getMonthlySessions() {
		return reportDao.findMonthlySessions();
	}

	public List<Object[]> getSessionsPerChamber() {
		return reportDao.findSessionsPerChamber();
	}

	public List<Object[]> getSessionsPerHealthInsurance() {
		return reportDao.findSessionsPerHealthInsurance();
	}

	public List<Object[]> getPresencesPerMonth() {
		return reportDao.findPresencesPerMonth();
	}
}
