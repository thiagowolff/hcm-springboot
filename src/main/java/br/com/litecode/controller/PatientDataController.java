package br.com.litecode.controller;

import br.com.litecode.domain.model.PatientData;
import br.com.litecode.domain.repository.PatientDataRepository;
import br.com.litecode.util.MessageUtil;
import com.google.common.base.Strings;
import lombok.Getter;
import lombok.Setter;
import org.omnifaces.util.Messages;
import org.primefaces.event.RowEditEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.dao.DataIntegrityViolationException;

@Getter
@Setter
@CacheConfig(cacheNames = "patientData")
public abstract class PatientDataController<T extends PatientData> {
	@Autowired
	protected PatientController patientController;

	private String name;

	protected abstract PatientDataRepository<T> getRepository();
	protected abstract T createPatientData();

	public PatientDataController() {
	}

	@Cacheable(key = "#root.targetClass")
	public Iterable<T> getPatientData() {
		return getRepository().findAllByOrderByNameAsc();
	}

	@CacheEvict(key = "#root.targetClass")
	public void deletePatientData(T patientData) {
		try {
			getRepository().delete(patientData);
		} catch (DataIntegrityViolationException e) {
			Messages.addGlobalError(MessageUtil.getMessage("error.registerInUse"));
		}
	}

	@CacheEvict(key = "#root.targetClass")
	public void addPatientData() {
		T patientData = createPatientData();

		if (!Strings.isNullOrEmpty(name)) {
			name = name.trim();
		}

		patientData.setName(name);
		try {
            getRepository().save(patientData);
            name = null;
        } catch (DataIntegrityViolationException e) {
            Messages.addGlobalError(MessageUtil.getMessage("error.dataAlreadyExists"));
        }
	}

	@CacheEvict(key = "#root.targetClass")
	public void onRowEdit(RowEditEvent event) {
		getRepository().save((T) event.getObject());
	}
}