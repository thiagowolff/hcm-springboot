package br.com.litecode.domain.model;

import br.com.litecode.domain.model.ChamberEvent.EventType;
import br.com.litecode.domain.repository.ContextDataConverter;
import lombok.Getter;
import lombok.Setter;
import org.apache.shiro.SecurityUtils;
import org.hibernate.annotations.SortNatural;
import org.omnifaces.util.Faces;

import javax.persistence.*;
import java.io.Serializable;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Iterator;
import java.util.SortedSet;
import java.util.TimeZone;
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
	@OneToMany(mappedBy = "session", fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
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

	@Convert(converter = ContextDataConverter.class)
	private ContextData contextData;

	public Session() {
		patientSessions = new TreeSet<>();
		status = SessionStatus.CREATED;
		currentProgress = 0;
		contextData = new ContextData();
	}

	public LocalDate getSessionDate() {
		return scheduledTime.toLocalDate();
	}

	public String getPatients() {
		String patients = "";
		for (PatientSession patientSession : patientSessions) {
			patients += patientSession.getPatient().getName() + "<br />";
		}
		return patients;
	}

	public void init() {
		ZoneId timeZone = Faces.getSessionAttribute("timeZone");
		contextData.setTimeZone(timeZone == null ? ZoneId.systemDefault().getId() : timeZone.getId());

		LocalDateTime now = LocalDateTime.now(ZoneId.of(contextData.getTimeZone()));
		startTime = now.toLocalTime();
		endTime = now.plus(chamber.getChamberEvent(EventType.COMPLETION).getTimeout(), ChronoUnit.SECONDS).toLocalTime();
		currentProgress = 0;
		status = SessionStatus.CREATED;
	}

	public void reset() {
		startTime = scheduledTime.toLocalTime();
		endTime = scheduledTime.plus(chamber.getChamberEvent(EventType.COMPLETION).getTimeout(), ChronoUnit.SECONDS).toLocalTime();
		status = SessionStatus.CREATED;
		currentProgress = 0;
	}

	public void addPatient(Patient patient) {
		patientSessions.add(new PatientSession(patient, this));
	}

	public void updateProgress() {
		long remainingMillis = Duration.between(LocalTime.now(ZoneId.of(contextData.getTimeZone())), endTime).toMillis();
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

	public ChamberEvent getNextChamberEvent() {
		for (Iterator<ChamberEvent> it = chamber.getChamberEvents().iterator(); it.hasNext();) {
			ChamberEvent chamberEvent = it.next();
			if (chamberEvent.getEventType().getSessionStatus() == status) {
				return it.hasNext() ? it.next() : null;
			}
		}

		return chamber.getChamberEvent(EventType.START.next());
	}

	public LocalTime getNextChamberEventTime() {
		ChamberEvent nextChamberEvent = getNextChamberEvent();
		if (nextChamberEvent == null) {
			return null;
		}

		return startTime.plusSeconds(nextChamberEvent.getTimeout());
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

	@Getter
	@Setter
	public static class ContextData implements Serializable {
		private String createdBy;
		private String startedBy;
		private String timeZone;
	}
}
