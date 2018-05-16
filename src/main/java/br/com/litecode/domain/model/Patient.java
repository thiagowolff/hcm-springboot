package br.com.litecode.domain.model;

import br.com.litecode.domain.model.PatientData.PatientStatus;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.Set;
import java.util.TreeSet;

@Entity
@Getter
@Setter
public class Patient {
	public enum Gender { M, F };

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer patientId;

	private String name;
	private String patientRecord;
	private String email;
	private String phoneNumber;
	private LocalDate birthDate;
	private LocalDate consultationDate;
	private LocalDate finalSessionDate;
	private Boolean medicalIndication;
	private boolean active;
	private String remarks;

	@Embedded
	private AuditLog auditLog;

	@Enumerated(EnumType.STRING)
	private Gender gender;

	@ManyToOne
	@JoinColumn(name = "health_insurance_id")
	private PatientData.HealthInsurance healthInsurance;

	@ManyToOne
	@JoinColumn(name = "consultation_reason_id")
	private PatientData.ConsultationReason consultationReason;

	@ManyToOne
	@JoinColumn(name = "physician_assistant_id")
	private PatientData.PhysicianAssistant physicianAssistant;

	@ManyToOne
	@JoinColumn(name = "patient_status_id")
	private PatientStatus patientStatus;

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
		return consultationReason == null ? null : consultationReason.getName();
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
		LocalDateTime getInitialSessionDate();
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

			@Override
			public LocalDateTime getInitialSessionDate() {
				return null;
			}
		};
	}
}