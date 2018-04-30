package br.com.litecode.controller;

import br.com.litecode.domain.model.PhysicianAssistant;
import br.com.litecode.domain.repository.PhysicianAssistantRepository;
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
public class PhysicianAssistantController implements Serializable {
	@Autowired
	private PhysicianAssistantRepository physicianAssistantRepository;

	@Autowired
	private PatientController patientController;

	@Getter
	@Setter
	private String physicianAssistantName;

	private Iterable<PhysicianAssistant> physicianAssistants;

	public PhysicianAssistantController() {
	}

	public Iterable<PhysicianAssistant> getPhysicianAssistants() {
		if (physicianAssistants == null) {
			physicianAssistants = physicianAssistantRepository.findAllByOrderByNameAsc();
		}
		return physicianAssistants;
	}

	public void deletePhysicianAssistant(PhysicianAssistant physicianAssistant) {
		try {
			physicianAssistantRepository.delete(physicianAssistant);
			refresh();
		} catch (DataIntegrityViolationException e) {
			Messages.addGlobalError(MessageUtil.getMessage("error.registerInUse"));
		}
	}

	public void addPhysicianAssistant() {
		PhysicianAssistant physicianAssistant = new PhysicianAssistant();
		physicianAssistant.setName(physicianAssistantName);
		physicianAssistantRepository.save(physicianAssistant);
		refresh();
	}

	public void onRowEdit(RowEditEvent event) {
		physicianAssistantRepository.save((PhysicianAssistant) event.getObject());
		refresh();
	}

	public void refresh() {
		physicianAssistantName = null;
		physicianAssistants = null;
		patientController.refresh();
	}
}