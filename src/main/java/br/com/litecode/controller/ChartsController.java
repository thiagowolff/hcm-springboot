package br.com.litecode.controller;

import br.com.litecode.domain.repository.ChartsRepository;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import javax.faces.view.ViewScoped;
import java.io.Serializable;
import java.time.Month;
import java.time.Year;
import java.time.format.TextStyle;
import java.util.*;
import java.util.Map.Entry;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@ViewScoped
@Component
@Cacheable(cacheNames = "chart", key = "#root.methodName")
public class ChartsController implements Serializable {
	@Autowired
	private ChartsRepository chartsRepository;

	public String getPresencesPerMonthModel() {
		return loadMonthlyChartData(chartsRepository::findMonthlyPresences);
	}

	public String getPresencesPerYearModel() {
		return loadSingleCountChartData(chartsRepository::findYearlyPresences);
	}

	public String getAbsencesPerMonthModel() {
		return loadMonthlyChartData(chartsRepository::findMonthlyAbsences);
	}

	public String getAbsencesPerYearModel() {
		return loadSingleCountChartData(chartsRepository::findYearlyAbsences);
	}

	public String getConsultationsPerMonthModel() {
		return loadMonthlyChartData(chartsRepository::findMonthlyConsultations);
	}

	public String getConsultationsPerYearModel() {
		return loadSingleCountChartData(chartsRepository::findYearlyConsultations);
	}

	public String getSessionsPerHealthInsuranceModel() {
		return loadSingleCountChartData(chartsRepository::findSessionsPerHealthInsurance);
	}

	public String getPatientsPerMedicalIndicationModel() {
		return loadSingleCountChartData(chartsRepository::findPatientsPerMedicalIndication);
	}

	public String getPatientsPerConsultationReasonModel() {
		return loadSingleCountChartData(chartsRepository::findPatientsPerConsultationReason);
	}
	public String getPatientsPerTreatmentStatusModel() {
		return loadSingleCountChartData(chartsRepository::findPatientsPerTreatmentStatus);
	}

	public String getMonthlyNewPatients() {
		return loadSingleCountChartData(chartsRepository::findMonthlyNewPatients);
	}

	@Cacheable(cacheNames = "chart", key = "#patientId")
	public String getPatientAttendance(Integer patientId) {
		List<Object[]> rows = chartsRepository.findPatientAttendance(patientId);
		List<String> weeklyAttendanceData = rows.stream().map(row -> row[2] + ":" + row[3]).collect(Collectors.toList());
		Collections.reverse(weeklyAttendanceData);
		return "0:0," + String.join(",", weeklyAttendanceData);
	}

	@Cacheable(cacheNames = "chart", key = "{ #year, #month, #absences }")
	public String getMonthlyAttendance(int year, int month, boolean absences) {
		List<Object[]> rows = chartsRepository.findMonthlyAttendance(year, month);

		Map<Integer, String> attendanceMap = new TreeMap<>();
		for (Object[] row : rows) {
		    String attendance = absences ? row[2].toString() : row[1].toString();
		    attendanceMap.put((Integer) row[0], attendance);
        }

		String data = "";
		for (int day = 1; day <= Month.of(month).length(Year.isLeap(year)); day++) {
		    data += "," + attendanceMap.getOrDefault(day, "0");
        }

		return data.substring(1);
	}

	private String loadMonthlyChartData(Supplier<List<Object[]>> dao) {
		return getMonthlyChartData(dao.get());
	}

	private String loadSingleCountChartData(Supplier<List<Object[]>> dao) {
		return getSingleCountChartData(dao.get());
	}

	private String getMonthlyChartData(List<Object[]> dataRows) {
		if (dataRows.isEmpty()) {
			return null;
		}

        Map<Number, Map<Number, Number>> chartData = new LinkedHashMap<>();

		for (Object[] row : dataRows) {
		    Number year = (Number) row[0];
		    Number month = (Number) row[1];
		    Number count = (Number) row[2];

            chartData.computeIfAbsent(year, key -> new LinkedHashMap<>()).put(month, count);
        }

        JsonArray labelArray = new JsonArray();
        for (int month = 1; month <= 12; month++) {
            labelArray.add(Month.of(month).getDisplayName(TextStyle.FULL, Locale.getDefault()));
        }

        JsonArray datasetArray = new JsonArray();

		for (Entry<Number, Map<Number, Number>> entry : chartData.entrySet()) {
            JsonObject dataset = new JsonObject();
            dataset.addProperty("label", entry.getKey());

            JsonArray dataArray = new JsonArray();
            for (int month = 1; month <= 12; month++) {
                dataArray.add(entry.getValue().getOrDefault(month, 0));
            }
            dataset.add("data", dataArray);
            datasetArray.add(dataset);
        }

        JsonObject data = new JsonObject();

		data.add("labels", labelArray);
        data.add("datasets", datasetArray);

		return data.toString();
	}

    private String getSingleCountChartData(List<Object[]> dataRows) {
        if (dataRows.isEmpty()) {
            return "{}";
        }

        Map<Object, Number> chartData = new LinkedHashMap<>();

        for (Object[] row : dataRows) {
            Object field = row[0];
            Number count = (Number) row[1];
            chartData.put(field, count);
        }

        JsonArray labelArray = new JsonArray();
        for (Object field : chartData.keySet()) {
            labelArray.add(field.toString());
        }

		JsonArray dataArray = new JsonArray();
        for (Entry<Object, Number> entry : chartData.entrySet()) {
            dataArray.add(entry.getValue());
        }

		JsonObject dataset = new JsonObject();
		dataset.add("data", dataArray);

		JsonArray datasetArray = new JsonArray();
        datasetArray.add(dataset);

        JsonObject data = new JsonObject();
        data.add("labels", labelArray);
        data.add("datasets", datasetArray);

        return data.toString();
    }
}
