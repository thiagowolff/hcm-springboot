package br.com.litecode.domain.model;

import br.com.litecode.util.TextUtil;
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
	private Session session;

	private boolean absent;

	private Float temperature;

	private String bloodPressure;

	public PatientSession() {
	}

	public PatientSession(Patient patient, Session session) {
		this.patient = patient;
		this.session = session;
	}

	@Override
	public int compareTo(PatientSession patientSession) {
		return TextUtil.normalizeText(patient.getName()).compareTo(TextUtil.normalizeText(patientSession.getPatient().getName()));
	}
}
