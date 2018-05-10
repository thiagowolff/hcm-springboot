package br.com.litecode.domain.model;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Entity;

@Entity
@Getter
@Setter
public class PhysicianAssistant extends PatientData {
//	@Id
//	@GeneratedValue(strategy = GenerationType.IDENTITY)
//	private Integer physicianAssistantId;
//	private String name;
//
//	@Override
//	public boolean equals(Object o) {
//		if (this == o) return true;
//		if (o == null || getClass() != o.getClass()) return false;
//
//		PhysicianAssistant that = (PhysicianAssistant) o;
//
//		return physicianAssistantId.equals(that.physicianAssistantId);
//	}
//
//	@Override
//	public int hashCode() {
//		return physicianAssistantId.hashCode();
//	}
//
//	@Override
//	public String toString() {
//		return "[" + physicianAssistantId + "] " + name;
//	}
}