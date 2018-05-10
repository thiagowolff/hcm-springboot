package br.com.litecode.controller;

import br.com.litecode.domain.model.PatientStatus;
import br.com.litecode.domain.repository.PatientDataRepository;
import br.com.litecode.domain.repository.PatientStatusRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.faces.view.ViewScoped;
import java.io.Serializable;

@ViewScoped
@Component
public class PatientStatusController extends PatientDataController<PatientStatus> implements Serializable {
	@Autowired
	private PatientStatusRepository patientStatusRepository;

	@Override
	protected PatientDataRepository<PatientStatus> getRepository() {
		return patientStatusRepository;
	}

	@Override
	protected PatientStatus createPatientData() {
		return new PatientStatus();
	}
}