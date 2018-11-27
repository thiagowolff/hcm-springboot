package br.com.litecode.domain.model;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Objects;

@Entity
@Getter
@Setter
public class EventType implements Serializable {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer eventTypeId;

	private String eventTypeCode;
	private String description;

	@Enumerated(EnumType.STRING)
	private Session.SessionStatus sessionStatus;

	public EventType() {
	}

	public EventType(String eventTypeCode) {
		this.eventTypeCode = eventTypeCode;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		EventType eventType = (EventType) o;
		return Objects.equals(eventTypeCode, eventType.eventTypeCode);
	}

	@Override
	public int hashCode() {
		return Objects.hash(eventTypeCode);
	}
}
