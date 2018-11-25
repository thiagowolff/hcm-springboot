package br.com.litecode.controller;

import br.com.litecode.domain.model.PatientData;
import br.com.litecode.domain.repository.PatientDataRepository;
import br.com.litecode.util.MessageUtil;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.exception.ConstraintViolationException;
import org.omnifaces.util.Messages;
import org.primefaces.event.RowEditEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;

@Getter
@Setter
public abstract class PatientDataController<T extends PatientData> {
	@Autowired
	protected PatientController patientController;

	private String name;
	private Iterable<T> patientData;

	protected abstract PatientDataRepository<T> getRepository();
	protected abstract T createPatientData();

	public PatientDataController() {
	}

	public Iterable<T> getPatientData() {
		if (patientData == null) {
			patientData = getRepository().findAllByOrderByNameAsc();
		}
		return patientData;
	}

	public void deletePatientData(T patientData) {
		try {
			getRepository().delete(patientData);
			refresh();
		} catch (DataIntegrityViolationException e) {
			Messages.addGlobalError(MessageUtil.getMessage("error.registerInUse"));
		}
	}

	public void addPatientData() {
		T patientData = createPatientData();
		patientData.setName(name);
		try {
            getRepository().save(patientData);
			refresh();
        } catch (DataIntegrityViolationException e) {
            Messages.addGlobalError(MessageUtil.getMessage("error.dataAlreadyExists"));
        }
	}

	public void onRowEdit(RowEditEvent event) {
		getRepository().save((T) event.getObject());
		refresh();
	}

	public void refresh() {
		name = null;
		patientData = null;
		patientController.refresh();
	}
}