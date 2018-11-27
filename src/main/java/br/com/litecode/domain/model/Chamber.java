package br.com.litecode.domain.model;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

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

	public ChamberEvent getChamberEvent(EventType eventType) {
		Optional<ChamberEvent> chamberEvent = chamberEvents.stream().filter(event -> event.getEventType().equals(eventType)).findFirst();
		if (chamberEvent.isPresent()) {
			return chamberEvent.get();
		}
		throw new RuntimeException("The chamber " + eventType + " event is not defined!");
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		Chamber chamber = (Chamber) o;
		return Objects.equals(chamberId, chamber.chamberId);
	}

	@Override
	public int hashCode() {
		return Objects.hash(chamberId);
	}

	@Override
	public String toString() {
		return "[" + chamberId + "] " + name;
	}
}
