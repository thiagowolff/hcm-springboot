package br.com.litecode.controller;

import br.com.litecode.domain.model.HealthInsurance;
import br.com.litecode.domain.repository.HealthInsuranceRepository;
import br.com.litecode.util.MessageUtil;
import lombok.Getter;
import lombok.Setter;
import org.omnifaces.util.Messages;
import org.primefaces.event.RowEditEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;

import javax.faces.view.ViewScoped;
import java.io.Serializable;

@ViewScoped
@Component
public class HealthInsuranceController implements Serializable {
	@Autowired
	private HealthInsuranceRepository healthInsuranceRepository;

	@Autowired
	private PatientController patientController;

	@Getter
	@Setter
	private String healthInsuranceName;

	private Iterable<HealthInsurance> healthInsurances;

	public HealthInsuranceController() {
	}

	public Iterable<HealthInsurance> getHealthInsurances() {
		if (healthInsurances == null) {
			healthInsurances = healthInsuranceRepository.findAll();
		}
		return healthInsurances;
	}

	public void deleteHealthInsurance(HealthInsurance healthInsurance) {
		try {
			healthInsuranceRepository.delete(healthInsurance);
			refresh();
		} catch (DataIntegrityViolationException e) {
			Messages.addGlobalError(MessageUtil.getMessage("error.healthInsuranceInUse"));
		}
	}

	public void addHealthInsurance() {
		HealthInsurance healthInsurance = new HealthInsurance();
		healthInsurance.setName(healthInsuranceName);
		healthInsuranceRepository.save(healthInsurance);
		refresh();
	}

	public void onRowEdit(RowEditEvent event) {
		healthInsuranceRepository.save((HealthInsurance) event.getObject());
		refresh();
	}

	public void refresh() {
		healthInsuranceName = null;
		healthInsurances = null;
		patientController.refresh();
	}
}