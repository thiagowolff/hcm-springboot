package br.com.litecode.domain.model;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Entity;

@Entity
@Getter
@Setter
public class ConsultationReason extends PatientData {
//	@Id
//	@GeneratedValue(strategy = GenerationType.IDENTITY)
//	private Integer consultationReasonId;
//	private String name;
//
//	@Override
//	public boolean equals(Object o) {
//		if (this == o) return true;
//		if (o == null || getClass() != o.getClass()) return false;
//
//		ConsultationReason that = (ConsultationReason) o;
//
//		return consultationReasonId.equals(that.consultationReasonId);
//	}
//
//	@Override
//	public int hashCode() {
//		return consultationReasonId.hashCode();
//	}
//
//	@Override
//	public String toString() {
//		return name;
//	}
}