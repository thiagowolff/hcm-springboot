package br.com.litecode.domain;

import br.com.litecode.util.MessageUtil;
import org.joda.time.Duration;
import org.joda.time.Period;
import org.joda.time.format.PeriodFormatter;
import org.joda.time.format.PeriodFormatterBuilder;

import javax.persistence.*;
import java.io.Serializable;

import static br.com.litecode.util.MessageUtil.*;

@Entity
@Table(name = "chamber_event")
@Cacheable
public class ChamberEvent implements Serializable {
	public enum EventType {CREATION, START, WEAR_MASK, REMOVE_MASK, SHUTDOWN, COMPLETION}

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "event_id")
	private Integer eventId;

	@Column
	private Integer timeout;

	@Enumerated(value = EnumType.STRING)
	@Column(name = "event_type")
	private EventType eventType;

	@Column(name = "notification_sound")
	private String notificationSound;

	public String getDuration() {
		PeriodFormatter formatter = new PeriodFormatterBuilder()
				.appendHours()
				.appendSuffix("h ")
				.appendMinutes()
				.appendSuffix("m ")
				.appendSeconds()
				.appendSuffix("s ")
				.toFormatter();

		return formatter.print(Duration.millis(timeout).toPeriod());
	}

	public Integer getEventId() {
		return eventId;
	}

	public Integer getTimeout() {
		return timeout;
	}

	public EventType getEventType() {
		return eventType;
	}

	public String getNotificationSound() {
		return notificationSound;
	}
}
