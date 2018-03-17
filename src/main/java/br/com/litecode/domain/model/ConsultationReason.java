package br.com.litecode.domain.model;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.time.Instant;

@Entity
@Getter
@Setter
public class HealthInsurance {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer healthInsuranceId;
	private String name;

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		HealthInsurance that = (HealthInsurance) o;

		return healthInsuranceId.equals(that.healthInsuranceId);
	}

	@Override
	public int hashCode() {
		return healthInsuranceId.hashCode();
	}

	@Override
	public String toString() {
		return "[" + healthInsuranceId + "] " + name;
	}
}