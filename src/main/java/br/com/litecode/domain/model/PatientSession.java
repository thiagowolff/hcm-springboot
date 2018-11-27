package br.com.litecode.domain.model;

import br.com.litecode.util.TextUtil;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Objects;

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

	public Integer getPatientId() {
	    return patient.getPatientId();
    }

	@Override
	public int compareTo(PatientSession patientSession) {
		return TextUtil.normalizeText(patient.getName()).compareTo(TextUtil.normalizeText(patientSession.getPatient().getName()));
	}

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PatientSession that = (PatientSession) o;
        return Objects.equals(patient, that.patient) && Objects.equals(session, that.session);
    }

    @Override
    public int hashCode() {
        return Objects.hash(patient, session);
    }
}
