package br.com.litecode.domain.model;

import com.google.common.collect.Lists;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Set;

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
	@JoinColumn(name = "chamber_id", nullable = false)
	@OrderBy("timeout")
	private Set<ChamberEvent> chamberEvents;

	public List<ChamberEvent> getEvents() {
		return Lists.newArrayList(chamberEvents);
	}

	public ChamberEvent getFirstEvent() {
		return chamberEvents.stream()
				.filter(e -> e.getEventType().isActive())
				.min(Comparator.comparingInt(ChamberEvent::getTimeout))
				.get();
	}

	public ChamberEvent getLastEvent() {
		return chamberEvents.stream()
				.filter(e -> e.getEventType().isActive())
				.max(Comparator.comparingInt(ChamberEvent::getTimeout))
				.get();
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
