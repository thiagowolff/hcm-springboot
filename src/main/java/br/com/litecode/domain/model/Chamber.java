package br.com.litecode.domain.model;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.SortNatural;

import javax.persistence.*;
import java.util.List;
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
	@SortNatural
	private List<ChamberEvent> chamberEvents;

	public ChamberEvent getChamberEvent(ChamberEvent.EventType eventType) {
		Optional<ChamberEvent> chamberEvent = chamberEvents.stream().filter(event -> event.getEventType() == eventType).findFirst();
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
		return chamberId != null ? chamberId.equals(chamber.chamberId) : chamber.chamberId == null;
	}

	@Override
	public int hashCode() {
		return chamberId.hashCode();
	}

	@Override
	public String toString() {
		return "[" + chamberId + "] " + name;
	}
}
