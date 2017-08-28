package br.com.litecode.domain.model;

import br.com.litecode.domain.model.ChamberEvent.EventType;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.SortNatural;

import javax.persistence.*;
import java.io.Serializable;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.SortedSet;
import java.util.TreeSet;

@Entity
@Getter
@Setter
public class Session implements Comparable<Session>, Serializable {
	public enum SessionStatus { CREATED, COMPRESSING, O2_ON, O2_OFF, SHUTTING_DOWN, FINISHED }

	public enum TimePeriod { MORNING, AFTERNOON, NIGHT }

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer sessionId;

	@ManyToOne
	@JoinColumn(name = "chamber_id")
	private Chamber chamber;

	@SortNatural
	@OneToMany(mappedBy = "session", cascade = CascadeType.ALL, orphanRemoval = true)
	private SortedSet<PatientSession> patientSessions;

	private LocalDateTime scheduledTime;
	private LocalTime startTime;
	private LocalTime endTime;

	@Enumerated(EnumType.STRING)
	private SessionStatus status;

	@Transient
	private String timeRemaining;

	@Transient
	private long currentProgress;

	@Transient
	private String managedBy;

	public Session() {
		patientSessions = new TreeSet<>();
		status = SessionStatus.CREATED;
		currentProgress = 0;
	}

	public void reset() {
		LocalDateTime now = LocalDateTime.now();
		startTime = now.toLocalTime();
		endTime = now.plus(chamber.getChamberEvent(EventType.COMPLETION).getTimeout(), ChronoUnit.SECONDS).toLocalTime();
		currentProgress = 0;
		status = SessionStatus.CREATED;
	}

	public void addPatient(Patient patient) {
		patientSessions.add(new PatientSession(patient, this));
	}

	public void updateProgress() {
		long remainingMillis = Duration.between(LocalTime.now(), endTime).toMillis();
		long duration = Duration.between(startTime, endTime).toMillis();
		long elapsedTime =  duration - remainingMillis;

		timeRemaining = LocalTime.MIDNIGHT.plus(remainingMillis, ChronoUnit.MILLIS).format(DateTimeFormatter.ofPattern("HH:mm:ss"));
		currentProgress = elapsedTime * 100 / duration;
	}

	public TimePeriod getTimePeriod() {
		LocalTime startTime = scheduledTime.toLocalTime();
		if (startTime.isBefore(LocalTime.parse("12:00:00"))) {
			return TimePeriod.MORNING;
		}

		if (startTime.isAfter(LocalTime.parse("17:59:59"))) {
			return TimePeriod.NIGHT;
		}

		return TimePeriod.AFTERNOON;
	}

	public LocalTime getShutdownTime() {
		return startTime.plusSeconds(chamber.getChamberEvent(EventType.SHUTDOWN).getTimeout());
	}

	public boolean isRunning() {
		return status != SessionStatus.CREATED && status != SessionStatus.FINISHED;
	}

	@Override
	public int compareTo(Session session) {
		return startTime.compareTo(session.getStartTime());
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		Session session = (Session) o;
		return sessionId.equals(session.sessionId);
	}

	@Override
	public int hashCode() {
		return sessionId.hashCode();
	}

	@Override
	public String toString() {
		return "Session " + sessionId + " [" + status + "]";
	}
}
