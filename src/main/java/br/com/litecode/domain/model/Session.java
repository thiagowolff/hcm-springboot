package br.com.litecode.domain.model;

import br.com.litecode.domain.repository.ContextDataConverter;
import com.google.common.base.Joiner;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.SortNatural;
import org.springframework.cache.interceptor.SimpleKey;

import javax.persistence.*;
import java.io.Serializable;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Iterator;
import java.util.Objects;
import java.util.SortedSet;
import java.util.TreeSet;

@Entity
@Getter
@Setter
public class Session implements Comparable<Session>, Serializable {
	public enum SessionStatus { CREATED, COMPRESSING, O2_ON, O2_OFF, SHUTTING_DOWN, FINISHED }
	private enum TimePeriod { MORNING, AFTERNOON, NIGHT }

	private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm:ss");

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer sessionId;

	@ManyToOne(optional = false)
	@JoinColumn(name = "chamber_id", nullable = false)
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
	private ExecutionMetadata executionMetadata;

	private Instant createdOn;

	@OneToOne
	@JoinColumn(name = "created_by")
	private User createdBy;

	@Transient
	private Integer duration;

	@Transient
	private String timeRemaining;

	@Transient
	private Clock clock;

	public Session() {
		patientSessions = new TreeSet<>();
		status = SessionStatus.CREATED;
		executionMetadata = new ExecutionMetadata();
		clock = Clock.systemDefaultZone();
	}

	public LocalDate getSessionDate() {
		return scheduledTime.toLocalDate();
	}

	public String getPatientNames() {
		StringBuilder patients = new StringBuilder();
		for (PatientSession patientSession : patientSessions) {
			String style = patientSession.isAbsent() ? "style=\"color: #ff7043;\"" : "";
			patients.append("<span " + style + ">").append(patientSession.getPatient().getName()).append("</span> <br />");
		}
		return patients.toString();
	}

	public void addPatient(Patient patient) {
		patientSessions.add(new PatientSession(patient, this));
	}

	public void init() {
		startTime = currentTime();
		endTime = currentTime().plusSeconds(getDuration());
		timeRemaining = LocalTime.MIDNIGHT.plusSeconds(getDuration()).format(TIME_FORMAT);
		status = SessionStatus.CREATED;
		executionMetadata.init();
		executionMetadata.setPaused(false);
	}

	public void resume() {
		endTime = currentTime().plusSeconds(getDuration() - this.getExecutionMetadata().elapsedTime);
		executionMetadata.setPaused(false);
	}

	public void reset() {
		startTime = scheduledTime.toLocalTime();
		endTime = scheduledTime.plusSeconds(getDuration()).toLocalTime();
		status = SessionStatus.CREATED;
		timeRemaining = LocalTime.MIDNIGHT.plusSeconds(getDuration()).format(TIME_FORMAT);
		executionMetadata.init();
		executionMetadata.setPaused(false);
	}

	public void pause() {
		updateProgress();
		long remainingSeconds = Duration.between(currentTime(), endTime).getSeconds();
		executionMetadata.setElapsedTime(getDuration() - remainingSeconds);
		executionMetadata.setPaused(true);
	}

	public void updateProgress() {
		long remainingSeconds = Duration.between(currentTime(), endTime).getSeconds();
		long duration = getDuration();
		long elapsedTime = duration - remainingSeconds;

		timeRemaining = LocalTime.MIDNIGHT.plusSeconds(remainingSeconds).format(TIME_FORMAT);
		executionMetadata.setCurrentProgress(Math.min(100, elapsedTime * 100 / duration));
		executionMetadata.setElapsedTime(elapsedTime);
	}

	public long getCurrentProgress() {
		return executionMetadata.getCurrentProgress();
	}

	public String getTimeRemaining() {
		if (isPaused()) {
			return LocalTime.MIDNIGHT.plusSeconds(getDuration() - executionMetadata.getElapsedTime()).format(TIME_FORMAT);
		}

		if (status == SessionStatus.CREATED && timeRemaining == null) {
			return LocalTime.MIDNIGHT.plusSeconds(getDuration()).format(TIME_FORMAT);
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

	public Integer getDuration() {
		if (duration == null) {
			duration = chamber.getLastEvent().getTimeout();
		}
		return duration;
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

	public LocalDateTime getSessionEnd() {
		return scheduledTime.toLocalDate().atTime(endTime);
	}

	public String getSessionInfo() {
		if (executionMetadata == null) {
			return "";
		}

		String createdOn = this.createdOn == null ? null : "Criada em: " + this.createdOn.atZone(ZoneId.systemDefault()).format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));
		String createdBy = this.createdBy == null ? null : "Criada por: " + this.createdBy.getUsername();
		String startedBy = executionMetadata.getStartedBy() == null ? null : "Iniciada por: " + executionMetadata.getStartedBy();

		return Joiner.on("<br/>").skipNulls().join(createdOn, createdBy, startedBy);
	}

	public ChamberEvent getCurrentChamberEvent() {
		if (status == SessionStatus.FINISHED) {
			return chamber.getLastEvent();
		}
		return executionMetadata.getCurrentEvent();
	}

	public ChamberEvent getNextChamberEvent() {
		Iterator<ChamberEvent> chamberEvents = chamber.getEvents().stream().filter(e -> e.getEventType().isActive()).iterator();
		for (Iterator<ChamberEvent> it = chamberEvents; it.hasNext();) {
			ChamberEvent chamberEvent = it.next();
			if (chamberEvent.getEventType().getSessionStatus() == status) {
				return it.hasNext() ? it.next() : null;
			}
		}

		return chamber.getFirstEvent();
	}

	public LocalTime getNextChamberEventTime() {
		ChamberEvent nextChamberEvent = getNextChamberEvent();
		if (nextChamberEvent == null) {
			return null;
		}

		return endTime.minusSeconds(getDuration()).plusSeconds(nextChamberEvent.getTimeout());
	}

	public boolean isRunning() {
		return status != SessionStatus.CREATED && status != SessionStatus.FINISHED && !isPaused();
	}

	public boolean isPaused() {
		return executionMetadata.isPaused();
	}

	public SimpleKey getCacheKey() {
		return new SimpleKey(chamber.getChamberId(), getSessionDate());
	}

	private LocalTime currentTime() {
		return LocalTime.now(clock).truncatedTo(ChronoUnit.SECONDS);
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
		return Objects.equals(chamber, session.chamber) && Objects.equals(scheduledTime, session.scheduledTime);
	}

	@Override
	public int hashCode() {
		return Objects.hash(chamber, scheduledTime);
	}

	@Override
	public String toString() {
		String chamberName = Objects.isNull(chamber) ? "" : chamber.getName();
		String sessionDate = Objects.isNull(scheduledTime) ? "" : scheduledTime.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));
		return String.format("%s | %s | %s", sessionId, chamberName, sessionDate);
	}

	@Getter
	@Setter
	public static class ExecutionMetadata implements Serializable {
		private long currentProgress;
		private ChamberEvent currentEvent;
		private long elapsedTime;
		private boolean paused;
		private String startedBy;

		public void init() {
			currentProgress = 0;
			elapsedTime = 0;
			currentEvent = null;
		}

		@Override
		public String toString() {
			return "ExecutionMetadata{" +
					"currentProgress=" + currentProgress +
					", currentEvent=" + currentEvent +
					", elapsedTime=" + elapsedTime +
					", paused=" + paused +
					", startedBy='" + startedBy + '\'' +
					'}';
		}
	}

	public interface MonthlyStats {
		int getYear();
		int getMonth();
		int getNumberOfSessions();
		int getNumberOfAbsences();
	}

	public static MonthlyStats getEmptyMonthlyStats(LocalDate sessionDate) {
		return new MonthlyStats() {
			@Override
			public int getYear() {
				return sessionDate.getYear();
			}

			@Override
			public int getMonth() {
				return sessionDate.getMonthValue();
			}

			@Override
			public int getNumberOfSessions() {
				return 0;
			}

			@Override
			public int getNumberOfAbsences() {
				return 0;
			}
		};
	}
}
