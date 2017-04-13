package br.com.litecode.controller;

import br.com.litecode.service.ReportService;
import com.google.gson.Gson;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.inject.Named;
import java.util.List;
import java.util.function.Supplier;

@Named
@RequestScoped
public class InsightsManager {
	@Inject private ReportService reportService;

	public String getMonthlySessionsModel() {
		return loadChartData(reportService::getMonthlySessions, "Mês", "Sessōes");
	}

	public String getSessionsPerChamberModel() {
		return loadChartData(reportService::getSessionsPerChamber, "Câmara", "Sessōes");
	}

	public String getSessionsPerHealthInsuranceModel() {
		return loadChartData(reportService::getSessionsPerHealthInsurance, "Câmara", "Sessōes");
	}

	private String loadChartData(Supplier<List<Object[]>> dao, String... headers) {
		List<Object[]> results = dao.get();

		if (results.isEmpty()) {
			return null;
		}

		results.add(0, headers);
		return new Gson().toJson(results);
	}
}
