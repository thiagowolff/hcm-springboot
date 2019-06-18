package br.com.litecode.domain.model;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.Objects;

@Getter
@Setter
@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "patient_data_type")
public abstract class PatientData {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer patientDataId;
	protected String name;

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		PatientData that = (PatientData) o;
		return Objects.equals(patientDataId, that.patientDataId);
	}

	@Override
	public int hashCode() {
		return Objects.hash(patientDataId);
	}

	@Override
	public String toString() {
		return "[" + patientDataId + "] " + name;
	}

	@Entity
	@DiscriminatorValue("HealthInsurance")
	public static class HealthInsurance extends PatientData {
	}

	@Entity
	@DiscriminatorValue("PhysicianAssistant")
	public static class PhysicianAssistant extends PatientData {
	}

	@Entity
	@DiscriminatorValue("PatientStatus")
	public static class PatientStatus extends PatientData {
	}

	@Entity
	@DiscriminatorValue("ConsultationReason")
	public static class ConsultationReason extends PatientData {
	}
}