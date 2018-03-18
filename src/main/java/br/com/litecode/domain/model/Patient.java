package br.com.litecode.domain.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.Set;
import java.util.TreeSet;

import static br.com.litecode.util.MessageUtil.getMessage;

@Entity
@Getter
@Setter
public class Patient {
	private enum Gender { M, F };

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer patientId;

	private String name;
	private String patientRecord;
	private String folderNumber;
	private String email;
	private String phoneNumber;
	private LocalDate birthDate;
	private LocalDate consultationDate;
	private Boolean medicalIndication;
	private boolean active;

	@Embedded
	private AuditLog auditLog;

	@Enumerated(EnumType.STRING)
	private Gender gender;

	@ManyToOne
	@JoinColumn(name = "health_insurance_id")
	private HealthInsurance healthInsurance;

	@ManyToOne
	@JoinColumn(name = "consultation_reason_id")
	private ConsultationReason consultationReason;

	@OneToMany(mappedBy = "patient")
	@JsonIgnore
	private Set<PatientSession> patientSessions;

	public Patient() {
		patientSessions = new TreeSet<>();
		active = true;
		name = "";
	}

	public Patient(String name) {
		this();
		this.name = name;
	}

	public String getRecordInfo() {
		String record = Strings.isNullOrEmpty(patientRecord) ? null : String.format("%s: %s", getMessage("label.patientRecord"), patientRecord.trim());
		String folder = Strings.isNullOrEmpty(folderNumber) ? null : String.format("%s: %s", getMessage("label.folderNumber"), folderNumber.trim());
		return Joiner.on(" | ").skipNulls().join(record, folder);
	}

	public Integer getAge() {
		if (birthDate == null) {
			return null;
		}
		return Period.between(birthDate, LocalDate.now()).getYears();
	}

	public void audit(User user) {
		if (auditLog == null) {
			auditLog = new AuditLog();
		}

		if (patientId == null) {
			auditLog.setCreatedDate(Instant.now());
			auditLog.setCreatedBy(user);
		} else {
			auditLog.setModifiedDate(Instant.now());
			auditLog.setModifiedBy(user);
		}
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

	public static PatientStats getEmptyPatientStats(Integer patientId) {
		return new PatientStats() {
			@Override
			public Integer getPatientId() {
				return patientId;
			}

			@Override
			public int getCompletedSessions() {
				return 0;
			}

			@Override
			public int getAbsentSessions() {
				return 0;
			}
		};
	}
}