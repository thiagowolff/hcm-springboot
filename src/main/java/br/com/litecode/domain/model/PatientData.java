package br.com.litecode.domain.model;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Getter
@Setter
@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "patient_data_type")
public abstract class PatientData {
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Integer patientDataId;
	protected String name;

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		PatientData that = (PatientData) o;

		return patientDataId.equals(that.patientDataId);
	}

	@Override
	public int hashCode() {
		return patientDataId.hashCode();
	}

	@Override
	public String toString() {
		return "[" + patientDataId + "] " + name;
	}
}