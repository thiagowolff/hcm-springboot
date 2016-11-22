package br.com.litecode.domain;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "chamber_event")
@Cacheable
public class ChamberEvent implements Serializable {
	public enum EventType { CREATION, START, DECOMPRESSION, TRANSPORT, WEAR_MASK, REMOVE_MASK, SHUTDOWN, COMPLETION }

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
