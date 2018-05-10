package br.com.litecode.domain.model;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
@Getter
@Setter
public class PatientStatus extends PatientData {
//	@Id
//	@GeneratedValue(strategy = GenerationType.IDENTITY)
//	private Integer patientStatusId;
//	private String name;
//
//	@Override
//	public boolean equals(Object o) {
//		if (this == o) return true;
//		if (o == null || getClass() != o.getClass()) return false;
//
//		PatientStatus that = (PatientStatus) o;
//
//		return patientStatusId.equals(that.patientStatusId);
//	}
//
//	@Override
//	public int hashCode() {
//		return patientStatusId.hashCode();
//	}
//
//	@Override
//	public String toString() {
//		return "[" + patientStatusId + "] " + name;
//	}
}