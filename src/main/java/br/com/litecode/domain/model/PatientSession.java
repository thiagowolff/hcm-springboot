package br.com.litecode.domain.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Getter
@Setter
public class PatientSession implements Comparable<PatientSession>, Serializable {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer patientSessionId;

	@ManyToOne
	@JoinColumn(name = "patient_id", nullable = false)
	private Patient patient;

	@ManyToOne
	@JoinColumn(name = "session_id", nullable = false)
	@JsonIgnore
	private Session session;

	private boolean absent;

	public PatientSession() {
	}

	public PatientSession(Patient patient, Session session) {
		this.patient = patient;
		this.session = session;
	}

	@Override
	public int compareTo(PatientSession patientSession) {
		return patient.getName().compareTo(patientSession.getPatient().getName());
	}
}
