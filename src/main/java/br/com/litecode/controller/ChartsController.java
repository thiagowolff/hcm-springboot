package br.com.litecode.controller;

import br.com.litecode.domain.repository.ChartsRepository;
import com.google.gson.Gson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import javax.faces.view.ViewScoped;
import java.util.List;
import java.util.function.Supplier;

@ViewScoped
@Component
@Cacheable(cacheNames = "chart", key = "#root.methodName")
public class ChartsController {
	@Autowired
	private ChartsRepository chartsRepository;

	public String getMonthlySessionsModel() {
		return loadChartData(chartsRepository::findMonthlySessions, "Mês", "Sessōes");
	}

	public String getSessionsPerChamberModel() {
		return loadChartData(chartsRepository::findSessionsPerChamber, "Câmara", "Sessōes");
	}

	public String getSessionsPerHealthInsuranceModel() {
		return loadChartData(chartsRepository::findSessionsPerHealthInsurance, "Plano de saúde", "Sessōes");
	}

	public String getPresencesPerMonthModel() {
		return loadChartData(chartsRepository::findMonthlyPresences, "Mês", "Presenças");
	}

	public String getPatientsPerMedicalIndicationModel() {
		return loadChartData(chartsRepository::findPatientsPerMedicalIndication, "Indicação médica", "Pacientes");
	}

	public String getPatientsPerConsultationReasonModel() {
		return loadChartData(chartsRepository::findPatientsPerConsultationReason, "Motivo consulta", "Pacientes");
	}

	public String getMonthlyNewPatients() {
		return loadChartData(chartsRepository::findMonthlyNewPatients, "Mês", "Pacientes");
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
