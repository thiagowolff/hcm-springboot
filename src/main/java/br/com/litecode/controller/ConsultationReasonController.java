package br.com.litecode.controller;

import br.com.litecode.domain.model.PatientData;
import br.com.litecode.domain.repository.ConsultationReasonRepository;
import br.com.litecode.domain.repository.PatientDataRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.faces.view.ViewScoped;
import java.io.Serializable;

@ViewScoped
@Component
public class ConsultationReasonController extends PatientDataController<PatientData.ConsultationReason> implements Serializable {
	@Autowired
	private ConsultationReasonRepository consultationReasonRepository;

	@Override
	protected PatientDataRepository<PatientData.ConsultationReason> getRepository() {
		return consultationReasonRepository;
	}

	@Override
	protected PatientData.ConsultationReason createPatientData() {
		return new PatientData.ConsultationReason();
	}
}