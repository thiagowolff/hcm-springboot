package br.com.litecode.domain.model;

import com.google.common.base.Strings;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.TreeSet;

import static br.com.litecode.util.MessageUtil.getMessage;

@Entity
@Getter
@Setter
public class Patient {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer patientId;

	private String name;
	private String patientRecord;
	private String folderNumber;
	private String healthInsurance;
	private String email;
	private String phoneNumber;
	private LocalDate birthDate;
	private LocalDateTime creationDate;
	private boolean active;

	@OneToMany(mappedBy = "patient")
	private Set<PatientSession> patientSessions;

	public Patient() {
		creationDate = LocalDateTime.now();
		patientSessions = new TreeSet<>();
		active = true;
		name = "";
	}

	public Patient(String name) {
		this();
		this.name = name;
	}

	public String getRecordInfo() {
		String folderNumber = Strings.isNullOrEmpty(this.folderNumber) ? "-" : this.folderNumber.trim();
		String patientRecord = Strings.isNullOrEmpty(this.patientRecord) ? "-" : this.patientRecord.trim();
		return String.format("%s: %s | %s: %s", getMessage("label.folderNumber"), folderNumber, getMessage("label.patientRecord"), patientRecord);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof Patient)) return false;

		Patient patient = (Patient) o;

		return patientId != null ? patientId.equals(patient.patientId) : patient.patientId == null;

	}

	@Override
	public int hashCode() {
		return patientId != null ? patientId.hashCode() : 0;
	}

	@Override
	public String toString() {
		return "[" + patientId + "] " + name;
	}

	public interface PatientStats {
		Integer getPatientId();
		int getCompletedSessions();
		int getAbsentSessions();
	}
}