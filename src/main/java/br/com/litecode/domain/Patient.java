package br.com.litecode.domain;

import javax.persistence.*;
import javax.validation.constraints.Pattern;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static br.com.litecode.util.MessageUtil.getMessage;

@Entity
@Table(name = "patient")
@Cacheable
public class Patient {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "patient_id")
	private Integer patientId;

	@Column
	private String name;

	@Column(name = "patient_record")
	private String patientRecord;

	@Column(name = "folder_number")
	private String folderNumber;

	@Column(name = "health_insurance")
	private String healthInsurance;

	@Column
	@Pattern(regexp = "^([_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,}))?$")
	private String email;

	@Column(name = "phone_number")
	private String phoneNumber;

	@Column(name = "birth_date")
	private Date birthDate;

	@Column(name = "creation_date")
	private Date creationDate;

	@OneToMany(mappedBy = "patient")
	private List<PatientSession> patientSessions;

	public Patient() {
		creationDate = Date.from(Instant.now());
		patientSessions = new ArrayList<>();
	}

	public String getDisplayName() {
		String folderNumber = this.folderNumber != null && !this.folderNumber.isEmpty() ? this.folderNumber.trim() : "-";
		String patientRecord = this.patientRecord != null && !this.patientRecord.isEmpty() ? this.patientRecord.trim() : "-";
		return String.format("%s [%s: %s | %s: %s]", name, getMessage("label.folderNumber"), folderNumber, getMessage("label.patientRecord"), patientRecord);
	}

	public Integer getPatientId() {
		return patientId;
	}

	public void setPatientId(Integer patientId) {
		this.patientId = patientId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getFolderNumber() {
		return folderNumber;
	}

	public void setFolderNumber(String folderNumber) {
		this.folderNumber = folderNumber;
	}

	public String getHealthInsurance() {
		return healthInsurance;
	}

	public void setHealthInsurance(String healthInsurance) {
		this.healthInsurance = healthInsurance;
	}

	public void setCreationDate(Date creationDate) {
		this.creationDate = creationDate;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public Date getBirthDate() {
		return birthDate;
	}

	public void setBirthDate(Date birthDate) {
		this.birthDate = birthDate;
	}

	public Date getCreationDate() {
		return creationDate;
	}

	public String getPhoneNumber() {
		return phoneNumber;
	}

	public void setPhoneNumber(String phoneNumber) {
		this.phoneNumber = phoneNumber;
	}

	public List<PatientSession> getPatientSessions() {
		return patientSessions;
	}

	public void setPatientSessions(List<PatientSession> patientSessions) {
		this.patientSessions = patientSessions;
	}

	public String getPatientRecord() {
		return patientRecord;
	}

	public void setPatientRecord(String patientRecord) {
		this.patientRecord = patientRecord;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof Patient)) return false;

		Patient patient = (Patient) o;

		return patientId != null ? patientId.equals(patient.patientId) : patient.patientId == null;

	}

	@Override
	public int hashCode() {
		return patientId != null ? patientId.hashCode() : 0;
	}

	@Override
	public String toString() {
		return "[" + patientId + "] " + name;
	}
}