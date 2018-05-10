package br.com.litecode.controller;

import br.com.litecode.domain.model.HealthInsurance;
import br.com.litecode.domain.repository.HealthInsuranceRepository;
import br.com.litecode.domain.repository.PatientDataRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.faces.view.ViewScoped;
import java.io.Serializable;

@ViewScoped
@Component
public class HealthInsuranceController extends PatientDataController<HealthInsurance> implements Serializable {
	@Autowired
	private HealthInsuranceRepository healthInsuranceRepository;

	@Override
	protected PatientDataRepository<HealthInsurance> getRepository() {
		return healthInsuranceRepository;
	}

	@Override
	protected HealthInsurance createPatientData() {
		return new HealthInsurance();
	}
}