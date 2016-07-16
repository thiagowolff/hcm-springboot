package br.com.litecode.domain;

import br.com.litecode.domain.ChamberEvent.EventType;
import org.joda.time.LocalDateTime;
import org.joda.time.LocalTime;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Entity
@Table(name = "session")
public class Session implements Serializable {
	public enum SessionStatus { CREATED, RUNNING, DECOMPRESSING, SHUTTING_DOWN, FINISHED }
	public enum TimePeriod { MORNING, AFTERNOON, NIGHT }

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "session_id")
	private Integer sessionId;

	@ManyToOne
	@JoinColumn(name = "chamber_id")
	private Chamber chamber;

	@ManyToOne(cascade = { CascadeType.MERGE, CascadeType.PERSIST })
	@JoinColumn(name = "patient_id")
	private Patient patient;

	@Column(name = "session_time")
	private Date sessionTime;

	@Column(name = "start_time")
	private Date startTime;

	@Column(name = "end_time")
	private Date endTime;

	@Enumerated(EnumType.STRING)
	private SessionStatus status;

	@Column(name = "patient_session_count")
	private Long numberOfPatientSessions;

	@Transient
	private String timeRemaining;

	@Transient
	private long currentProgress;

	public Session() {
		status = SessionStatus.CREATED;
		currentProgress = 0;
	}

	public void updateProgress(long timeRemaining) {
		this.timeRemaining = LocalTime.MIDNIGHT.plusMillis((int) timeRemaining).toString("HH:mm:ss");
		long duration = chamber.getChamberEvent(EventType.COMPLETION).getTimeout();
		long elapsedTime = duration - timeRemaining;
		currentProgress = elapsedTime * 100 / duration;
	}

	public TimePeriod getTimePeriod() {
		LocalTime startTime = LocalTime.fromDateFields(sessionTime);
		if (startTime.isBefore(LocalTime.parse("12:00:00"))) {
			return TimePeriod.MORNING;
		}

		if (startTime.isAfter(LocalTime.parse("17:59:59"))) {
			return TimePeriod.NIGHT;
		}

		return TimePeriod.AFTERNOON;
	}

	public Date getDecompressionTime() {
		return LocalDateTime.fromDateFields(startTime).plusMillis(chamber.getChamberEvent(EventType.DECOMPRESSION).getTimeout()).toDate();
	}

	public Date getShutdownTime() {
		return LocalDateTime.fromDateFields(startTime).plusMillis(chamber.getChamberEvent(EventType.SHUTDOWN).getTimeout()).toDate();
	}

	public String getTimeRemaining() {
		return timeRemaining;
	}

	public long getCurrentProgress() {
		return currentProgress;
	}

	public void setCurrentProgress(int currentProgress) {
		this.currentProgress = currentProgress;
	}

	public Date getEndTime() {
		return endTime;
	}

	public Integer getSessionId() {
		return sessionId;
	}

	public Chamber getChamber() {
		return chamber;
	}

	public void setChamber(Chamber chamber) {
		this.chamber = chamber;
	}

	public Patient getPatient() {
		return patient;
	}

	public void setPatient(Patient patient) {
		this.patient = patient;
	}

	public SessionStatus getStatus() {
		return status;
	}

	public void setStatus(SessionStatus status) {
		this.status = status;
	}

	public Date getSessionTime() {
		return sessionTime;
	}

	public void setSessionTime(Date sessionTime) {
		this.sessionTime = sessionTime;
	}

	public Date getStartTime() {
		return startTime;
	}

	public void setStartTime(Date startTime) {
		this.startTime = startTime;
	}

	public void setEndTime(Date endTime) {
		this.endTime = endTime;
	}

	public Long getNumberOfPatientSessions() {
		return numberOfPatientSessions;
	}

	public void setNumberOfPatientSessions(Long numberOfPatientSessions) {
		this.numberOfPatientSessions = numberOfPatientSessions;
	}
}
