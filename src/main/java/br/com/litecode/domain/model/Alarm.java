package br.com.litecode.domain.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.io.Serializable;

@Entity
@Getter
@Setter
@AllArgsConstructor
public class Alarm implements Serializable {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer alarmId;

	private String cronExpression;
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
