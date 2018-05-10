package br.com.litecode.controller;

import br.com.litecode.domain.model.PhysicianAssistant;
import br.com.litecode.domain.repository.PatientDataRepository;
import br.com.litecode.domain.repository.PhysicianAssistantRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.faces.view.ViewScoped;
import java.io.Serializable;

@ViewScoped
@Component
public class PhysicianAssistantController extends PatientDataController<PhysicianAssistant> implements Serializable {
	@Autowired
	private PhysicianAssistantRepository physicianAssistantRepository;

	@Override
	protected PatientDataRepository<PhysicianAssistant> getRepository() {
		return physicianAssistantRepository;
	}

	@Override
	protected PhysicianAssistant createPatientData() {
		return new PhysicianAssistant();
	}
}