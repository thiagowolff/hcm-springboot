package br.com.litecode.domain;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "patient_session")
@Cacheable
@NamedQuery(
		name = "findPatientSessionsByDate",
		query = "select ps from PatientSession ps where ps.patient.patientId = :patientId and ps.session.sessionTime <= :sessionDate",
		hints = { @QueryHint (name = "org.hibernate.cacheable", value = "true") }
)
public class PatientSession implements Comparable<PatientSession>, Serializable {
	public enum PatientSessionStatus { ACTIVE, ABSENT }

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "patient_session_id")
	private Integer id;

	@ManyToOne(cascade = { CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH })
	@JoinColumn(name = "patient_id")
	private Patient patient;

	@ManyToOne(cascade = { CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH })
	@JoinColumn(name = "session_id")
	private Session session;

	@Enumerated(EnumType.STRING)
	private PatientSessionStatus status;

	public PatientSession() {
		status = PatientSessionStatus.ACTIVE;
	}

	public PatientSession(Patient patient, Session session) {
		this();
		this.patient = patient;
		this.session = session;
	}

	@Override
	public int compareTo(PatientSession patientSession) {
		return patient.getName().compareTo(patientSession.getPatient().getName());
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public Patient getPatient() {
		return patient;
	}

	public void setPatient(Patient patient) {
		this.patient = patient;
	}

	public Session getSession() {
		return session;
	}

	public void setSession(Session session) {
		this.session = session;
	}

	public PatientSessionStatus getStatus() {
		return status;
	}

	public void setStatus(PatientSessionStatus status) {
		this.status = status;
	}
}
