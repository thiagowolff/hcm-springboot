package br.com.litecode.domain.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.time.Duration;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

import static br.com.litecode.domain.model.Session.SessionStatus;
import static java.time.temporal.ChronoUnit.SECONDS;

@Entity
@Getter
@Setter
public class ChamberEvent implements Comparable<ChamberEvent> {
	@AllArgsConstructor
	@Getter
	public enum EventType {
		START(SessionStatus.COMPRESSING),
		WEAR_MASK(SessionStatus.O2_ON),
		REMOVE_MASK(SessionStatus.O2_OFF),
		SHUTDOWN(SessionStatus.SHUTTING_DOWN),
		COMPLETION(SessionStatus.FINISHED);

		private SessionStatus sessionStatus;
	}

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer eventId;

	private Integer timeout;

	@Enumerated(value = EnumType.STRING)
	private EventType eventType;

	public String getDuration() {
		Duration duration = Duration.of(timeout, SECONDS);
		return LocalTime.MIDNIGHT.plus(duration).format(DateTimeFormatter.ofPattern("HH:mm:ss"));
	}

	@Override
	public int compareTo(ChamberEvent chamberEvent) {
		return timeout.compareTo(chamberEvent.getTimeout());
	}

	@Override
	public String toString() {
		return eventType.toString();
	}
}
