package br.com.litecode.domain.model;

import br.com.litecode.domain.model.PatientData.ConsultationReason;
import br.com.litecode.domain.model.PatientData.HealthInsurance;
import br.com.litecode.domain.model.PatientData.PatientStatus;
import br.com.litecode.domain.model.PatientData.PhysicianAssistant;
import br.com.litecode.util.MessageUtil;
import com.google.common.base.Joiner;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;

@Entity
@Getter
@Setter
public class Patient {
	public enum Gender { M, F }

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
	private HealthInsurance healthInsurance;

	@ManyToOne
	@JoinColumn(name = "consultation_reason_id")
	private ConsultationReason consultationReason;

	@ManyToOne
	@JoinColumn(name = "physician_assistant_id")
	private PhysicianAssistant physicianAssistant;

	@ManyToOne
	@JoinColumn(name = "patient_status_id")
	private PatientStatus patientStatus;

	@OneToMany(mappedBy = "patient")
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
		String record = MessageUtil.getFormattedLabel("label.patientRecord", patientRecord);
		String reason = MessageUtil.getFormattedLabel("label.consultationReason", consultationReason == null ? null : consultationReason.getName());

		return Joiner.on("<br/>").skipNulls().join("<strong>" + name + "</strong>", record, reason);
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
        if (o == null || getClass() != o.getClass()) return false;
        Patient patient = (Patient) o;
        return Objects.equals(patientRecord, patient.patientRecord);
    }

    @Override
    public int hashCode() {
        return Objects.hash(patientRecord);
    }

    @Override
	public String toString() {
		return "[" + patientId + "] " + name;
	}

	public interface PatientStats {
		Integer getPatientId();
		int getCompletedSessions();
		int getAbsentSessions();
		int getCompletedSessionsInMonth();
		LocalDateTime getInitialSessionDate();
		LocalDateTime getLastSessionDate();
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
			public int getCompletedSessionsInMonth() {
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

			@Override
			public LocalDateTime getLastSessionDate() {
				return null;
			}
		};
	}
}