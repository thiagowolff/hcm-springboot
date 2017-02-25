package br.com.litecode.domain;

import br.com.litecode.domain.ChamberEvent.EventType;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.*;
import org.joda.time.LocalDateTime;
import org.joda.time.LocalTime;

import javax.persistence.*;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import java.io.Serializable;
import java.util.Date;
import java.util.SortedSet;
import java.util.TreeSet;

@Entity
@Table(name = "session")
@Cacheable
@NamedQuery(
		name = "findChamberSessionsByDate",
		query = "select distinct s from Session s where s.chamber.chamberId = :chamberId and s.sessionTime between :startOfDay and :endOfDay order by s.sessionTime, s.sessionId",
		hints = { @QueryHint (name = "org.hibernate.cacheable", value = "true") }
)
public class Session implements Comparable<Session>, Serializable {
	public enum SessionStatus { CREATED, COMPRESSING, O2_ON, O2_OFF, SHUTTING_DOWN, FINISHED }
	public enum TimePeriod { MORNING, AFTERNOON, NIGHT }

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "session_id")
	private Integer sessionId;

	@ManyToOne
	@JoinColumn(name = "chamber_id")
	private Chamber chamber;

	@OneToMany(mappedBy = "session", cascade = CascadeType.REMOVE)
	@SortNatural
	private SortedSet<PatientSession> patientSessions;

	@Column(name = "session_time")
	private Date sessionTime;

	@Column(name = "start_time")
	private Date startTime;

	@Column(name = "end_time")
	private Date endTime;

	@Enumerated(EnumType.STRING)
	private SessionStatus status;

	@Transient
	private String timeRemaining;

	@Transient
	private long currentProgress;

	public Session() {
		patientSessions = new TreeSet<>();
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

	public Date getShutdownTime() {
		return LocalDateTime.fromDateFields(startTime).plusMillis(chamber.getChamberEvent(EventType.SHUTDOWN).getTimeout()).toDate();
	}

	public boolean isRunning() {
		return status != SessionStatus.CREATED && status != SessionStatus.FINISHED;
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

	public SortedSet<PatientSession> getPatientSessions() {
		return patientSessions;
	}

	public void setPatientSessions(SortedSet<PatientSession> patientSessions) {
		this.patientSessions = patientSessions;
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
}
