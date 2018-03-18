package br.com.litecode.controller;

import br.com.litecode.domain.model.ConsultationReason;
import br.com.litecode.domain.repository.ConsultationReasonRepository;
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
public class ConsultationReasonController implements Serializable {
	@Autowired
	private ConsultationReasonRepository consultationReasonRepository;

	@Autowired
	private PatientController patientController;

	@Getter
	@Setter
	private String consultationReasonDescription;

	private Iterable<ConsultationReason> consultationReasons;

	public ConsultationReasonController() {
	}

	public Iterable<ConsultationReason> getConsultationReasons() {
		if (consultationReasons == null) {
			consultationReasons = consultationReasonRepository.findAllByOrderByDescriptionAsc();
		}
		return consultationReasons;
	}

	public void deleteConsultationReason(ConsultationReason healthInsurance) {
		try {
			consultationReasonRepository.delete(healthInsurance);
			refresh();
		} catch (DataIntegrityViolationException e) {
			Messages.addGlobalError(MessageUtil.getMessage("error.healthInsuranceInUse"));
		}
	}

	public void addConsultationReason() {
		ConsultationReason healthInsurance = new ConsultationReason();
		healthInsurance.setDescription(consultationReasonDescription);
		consultationReasonRepository.save(healthInsurance);
		refresh();
	}

	public void onRowEdit(RowEditEvent event) {
		consultationReasonRepository.save((ConsultationReason) event.getObject());
		refresh();
	}

	public void refresh() {
		consultationReasonDescription = null;
		consultationReasons = null;
		patientController.refresh();
	}
}