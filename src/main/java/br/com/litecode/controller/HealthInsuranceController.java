package br.com.litecode.controller;

import br.com.litecode.domain.model.PatientData;
import br.com.litecode.domain.repository.HealthInsuranceRepository;
import br.com.litecode.domain.repository.PatientDataRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.faces.view.ViewScoped;
import java.io.Serializable;

@ViewScoped
@Component
public class HealthInsuranceController extends PatientDataController<PatientData.HealthInsurance> implements Serializable {
	@Autowired
	private HealthInsuranceRepository healthInsuranceRepository;

	@Override
	protected PatientDataRepository<PatientData.HealthInsurance> getRepository() {
		return healthInsuranceRepository;
	}

	@Override
	protected PatientData.HealthInsurance createPatientData() {
		return new PatientData.HealthInsurance();
	}
}