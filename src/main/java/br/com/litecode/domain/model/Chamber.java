package br.com.litecode.domain.model;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

@Entity
@Getter
@Setter
public class Chamber {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer chamberId;

	private String name;
	private Integer capacity;

	@OneToMany(fetch = FetchType.EAGER)
	@JoinColumn(name = "chamber_id")
	@OrderBy("timeout")
	private List<ChamberEvent> chamberEvents;

	public ChamberEvent getFirstEvent() {
		return chamberEvents.stream().min(Comparator.comparingInt(ChamberEvent::getTimeout)).get();
	}

	public ChamberEvent getLastEvent() {
		return chamberEvents.stream().max(Comparator.comparingInt(ChamberEvent::getTimeout)).get();
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		Chamber chamber = (Chamber) o;
		return Objects.equals(name, chamber.name);
	}

	@Override
	public int hashCode() {
		return Objects.hash(name);
	}

	@Override
	public String toString() {
		return "[" + chamberId + "] " + name;
	}
}
