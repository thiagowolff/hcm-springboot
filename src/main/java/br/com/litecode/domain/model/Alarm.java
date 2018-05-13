package br.com.litecode.domain.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Getter
@Setter
@AllArgsConstructor
public class Alarm implements Serializable {
	public enum AlarmType { CRON, SCRIPT }

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer alarmId;

	@Enumerated(EnumType.STRING)
	private AlarmType alarmType;

	private String expression;
	private String name;
	private String message;
	private boolean active;

	public Alarm() {
		active = true;
	}

	@Override
	public String toString() {
		return "[" + alarmId + "] " + name;
	}
}
