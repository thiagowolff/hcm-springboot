package br.com.litecode.domain.model;

import br.com.litecode.domain.model.ChamberEvent.EventType;
import br.com.litecode.domain.repository.ContextDataConverter;
import com.google.common.base.Joiner;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.SortNatural;
import org.omnifaces.util.Faces;

import javax.persistence.*;
import java.io.Serializable;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Iterator;
import java.util.SortedSet;
import java.util.TreeSet;

@Entity
@Getter
@Setter
public class Session implements Comparable<Session>, Serializable {
	public enum SessionStatus { CREATED, COMPRESSING, O2_ON, O2_OFF, SHUTTING_DOWN, FINISHED }
	public enum TimePeriod { MORNING, AFTERNOON, NIGHT }

	private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm:ss");

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

	@Convert(converter = ContextDataConverter.class)
	private SessionMetadata sessionMetadata;

	@Transient
	private String timeRemaining;

	public Session() {
		patientSessions = new TreeSet<>();
		status = SessionStatus.CREATED;
		sessionMetadata = new SessionMetadata();
	}

	public LocalDate getSessionDate() {
		return scheduledTime.toLocalDate();
	}

	public String getPatientNames() {
		String patients = "";
		for (PatientSession patientSession : patientSessions) {
			patients += patientSession.getPatient().getName() + "<br />";
		}
		return patients;
	}

	public void addPatient(Patient patient) {
		patientSessions.add(new PatientSession(patient, this));
	}

	public void init() {
		ZoneId timeZone = Faces.getSessionAttribute("timeZone");
		sessionMetadata.setTimeZone(timeZone == null ? ZoneId.systemDefault().getId() : timeZone.getId());
		LocalDateTime now = LocalDateTime.now(ZoneId.of(sessionMetadata.getTimeZone()));
		sessionMetadata.setCreatedOn(now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));

		startTime = now.toLocalTime();
		endTime = now.plusSeconds(getDuration()).toLocalTime();
		sessionMetadata.setCurrentProgress(0);
		sessionMetadata.setElapsedTime(0);
		timeRemaining = LocalTime.MIDNIGHT.plusSeconds(getDuration()).format(TIME_FORMAT);
		status = SessionStatus.CREATED;

		sessionMetadata.setPaused(false);
	}

	public void resume() {
		LocalDateTime now = LocalDateTime.now(ZoneId.of(sessionMetadata.getTimeZone()));
		endTime = now.plusSeconds(getDuration() - getSessionMetadata().elapsedTime).toLocalTime();
		sessionMetadata.setPaused(false);
	}

	public void reset() {
		startTime = scheduledTime.toLocalTime();
		endTime = scheduledTime.plusSeconds(getDuration()).toLocalTime();
		status = SessionStatus.CREATED;
		sessionMetadata.setCurrentProgress(0);
		sessionMetadata.setElapsedTime(0);
		timeRemaining = LocalTime.MIDNIGHT.plusSeconds(getDuration()).format(TIME_FORMAT);
		sessionMetadata.setPaused(false);
	}

	public void pause() {
		updateProgress();
		long remainingSeconds = Duration.between(currentTime(), endTime).getSeconds();
		sessionMetadata.setElapsedTime(getDuration() - remainingSeconds);
		sessionMetadata.setPaused(true);
	}

	public void updateProgress() {
		long remainingSeconds = Duration.between(currentTime(), endTime).getSeconds();
		long duration = getDuration();
		long elapsedTime = duration - remainingSeconds;

		timeRemaining = LocalTime.MIDNIGHT.plusSeconds(remainingSeconds).format(TIME_FORMAT);
		sessionMetadata.setCurrentProgress(elapsedTime * 100 / duration);
	}

	public long getCurrentProgress() {
		return sessionMetadata.getCurrentProgress();
	}

	public String getTimeRemaining() {
		if (isPaused()) {
			return LocalTime.MIDNIGHT.plusSeconds(getDuration() - sessionMetadata.getElapsedTime()).format(TIME_FORMAT);
		}

		if (status == SessionStatus.FINISHED) {
			return "00:00:00";
		}

		if (timeRemaining == null) {
			long remainingSeconds = Duration.between(currentTime(), endTime).getSeconds();
			timeRemaining = LocalTime.MIDNIGHT.plusSeconds(remainingSeconds).format(TIME_FORMAT);
		}

		return timeRemaining;
	}

	public int getDuration() {
		return chamber.getChamberEvent(EventType.COMPLETION).getTimeout();
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

	public String getSessionInfo() {
		if (sessionMetadata == null) {
			return "";
		}

		String createdOn = sessionMetadata.getCreatedOn() == null ? null : "Criada em: " + sessionMetadata.getCreatedOn();
		String createdBy = sessionMetadata.getCreatedBy() == null ? null : "Criada por: " + sessionMetadata.getCreatedBy();
		String startedBy = sessionMetadata.getStartedBy() == null ? null : "Iniciada por: " + sessionMetadata.getStartedBy();

		return Joiner.on("<br/>").skipNulls().join(createdOn, createdBy, startedBy);
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

		return endTime.minusSeconds(nextChamberEvent.getTimeout());
	}

	public boolean isRunning() {
		return status != SessionStatus.CREATED && status != SessionStatus.FINISHED && !isPaused();
	}

	public boolean isPaused() {
		return sessionMetadata.isPaused();
	}

	private LocalTime currentTime() {
		if (sessionMetadata.getTimeZone() != null) {
			return LocalTime.now(ZoneId.of(sessionMetadata.getTimeZone()));
		} else {
			return LocalTime.now();
		}
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
	public static class SessionMetadata implements Serializable {
		private String createdOn;
		private String createdBy;
		private String startedBy;
		private String timeZone;
		private long currentProgress;
		private long elapsedTime;
		private boolean paused;
	}
}
