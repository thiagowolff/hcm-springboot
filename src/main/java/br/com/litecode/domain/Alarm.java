package br.com.litecode.domain;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "alarm")
@Cacheable
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

	public String getCronExpression() {
		return cronExpression;
	}

	public String getName() {
		return name;
	}

	public String getMessage() {
		return message;
	}

	public String getNotificationSound() {
		return notificationSound;
	}
}
