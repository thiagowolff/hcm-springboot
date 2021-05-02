package br.com.litecode.controller;

import br.com.litecode.domain.model.Patient;
import br.com.litecode.domain.model.Patient.PatientStats;
import br.com.litecode.domain.model.PatientSession;
import br.com.litecode.domain.model.Session;
import br.com.litecode.domain.repository.PatientRepository;
import br.com.litecode.security.UserPrincipal;
import br.com.litecode.service.AlarmService;
import br.com.litecode.util.MessageUtil;
import com.google.common.collect.ImmutableMap;
import lombok.Getter;
import lombok.Setter;
import org.omnifaces.util.Faces;
import org.omnifaces.util.Messages;
import org.primefaces.model.FilterMeta;
import org.primefaces.model.LazyDataModel;
import org.primefaces.model.SortMeta;
import org.primefaces.model.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import javax.faces.view.ViewScoped;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@ViewScoped
@Component
@CacheConfig(cacheNames = "patient")
public class PatientController extends LazyDataModel<Patient> implements Serializable {
	@Autowired
	private PatientRepository patientRepository;

	@Autowired
	private AlarmService alarmService;

	@Getter	@Setter
	private Patient patient;

	public PatientController() {
		patient = new Patient();
	}

	@Override
	public List<Patient> load(int offset, int pageSize, Map<String, SortMeta> sortBy, Map<String, FilterMeta> filterBy) {
		Page<Patient> patients;
		int page = offset / pageSize;

		Sort sort;
		if (!sortBy.isEmpty()) {
			sort = Sort.by(sortBy.values().stream().map(sortMeta -> new Sort.Order(sortMeta.getSortOrder() == SortOrder.ASCENDING ? Sort.Direction.ASC : Sort.Direction.DESC, sortMeta.getSortField())).collect(Collectors.toList()));
		} else {
			sort = Sort.by(Sort.Direction.DESC, "auditLog.createdDate");
		}

		PageRequest pageRequest = PageRequest.of(page, pageSize, sort);
		if (filterBy.isEmpty() || filterBy.getOrDefault("globalFilter", new FilterMeta()).getFilterValue() == null) {
			patients = patientRepository.findActivePatients(pageRequest);
		} else {
			String filterValue = (String) filterBy.get("globalFilter").getFilterValue();
			patients = patientRepository.findByNameOrRecord(filterValue, filterValue, pageRequest);
		}

		setRowCount((int) patients.getTotalElements());
		return patients.getContent();
	}

    @Cacheable(key = "#root.methodName")
	public List<Patient> getAvailablePatients() {
		return patientRepository.findAvailablePatients();
	}

	@Cacheable
	public Map<Integer, PatientStats> getPatientStats(Session session) {
		List<PatientStats> stats = patientRepository.findPatienStats(session.getSessionId(), session.getSessionEnd());

		Map<Integer, PatientStats> patientStats = stats.stream().collect(Collectors.toMap(PatientStats::getPatientId, Function.identity()));
		for (PatientSession patientSession : session.getPatientSessions()) {
			Integer patientId = patientSession.getPatient().getPatientId();
			patientStats.putIfAbsent(patientId, Patient.getEmptyPatientStats(patientId));
		}

		return patientStats;
	}

	@Cacheable(sync = true)
	public PatientStats getPatientStats(Integer patientId) {
		return patientRepository.findPatienStats(patientId);
	}

	public List<Patient> getAvailablePatientsForSession(Integer sessionId) {
		return patientRepository.findPatientsNotInSession(sessionId);
	}

    @CacheEvict(allEntries = true)
	public void deletePatient() {
		patient.setActive(false);
		patient.audit(UserPrincipal.getLoggedUser());
		patientRepository.save(patient);
	}

	@CacheEvict(allEntries = true)
	public void savePatient() {
		try {
			if (patient.getAge() != null && patient.getAge() < 1) {
				Faces.validationFailed();
				Messages.addGlobalError(MessageUtil.getMessage("error.patientInvalid"));
				return;
			}

			patient.setName(patient.getName().trim());
			patient.audit(UserPrincipal.getLoggedUser());
			patientRepository.save(patient);
		} catch (DataIntegrityViolationException e) {
			Faces.validationFailed();
			Messages.addGlobalError(MessageUtil.getMessage("error.patientRecord"));
		}
	}

    @CacheEvict(allEntries = true)
	public void finishTreatment() {
		patientRepository.save(patient);
	}

    @CacheEvict(allEntries = true)
	public void restartTreatment(Patient patient) {
		patient.setFinalSessionDate(null);
		patient.setPatientStatus(null);
		patientRepository.save(patient);
	}

	@Cacheable(key = "{ #patient.patientId, #patientStats.completedSessions }")
	public String getWarning(Patient patient, PatientStats patientStats) {
		Object result = alarmService.evaluateScripts(ImmutableMap.of("patient", patient,"patientStats", patientStats));

		if (result != null) {
			return result.toString();
		}

		return "";
	}

	@Cacheable(key = "#root.methodName")
	public List<Patient> getInactivePatients() {
		return patientRepository.findInactivePatients(LocalDate.now().minusMonths(4).atStartOfDay());
	}

	@Caching(evict = {
		@CacheEvict(key = "'getPatients'"),
		@CacheEvict(key = "'getInactivePatients'")
	})
	public void finishInactivePatientTreatment(Patient patient) {
		patient.setFinalSessionDate(LocalDate.now());
		patientRepository.save(patient);
	}

	public void newPatient() {
		patient = new Patient();
	}
}