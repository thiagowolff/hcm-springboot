package br.com.litecode.domain.model;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Cacheable
@Getter
@Setter
public class Alarm implements Serializable {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "alarm_id")
	private Integer alarmId;

	@Column(name = "cron_expression")
	private String cronExpression;

	@Column
	private String name;

	@Column
	private String message;

	@Column(name = "notification_sound")
	private String notificationSound;

	public Alarm() {
	}

	public Alarm(String name, String message, String notificationSound) {
		this.name = name;
		this.message = message;
		this.notificationSound = notificationSound;
	}

	@Override
	public String toString() {
		return "ID: " + alarmId + ", cronExpression='" + cronExpression + ", name='" + name + ", notificationSound='" + notificationSound;
	}
}
