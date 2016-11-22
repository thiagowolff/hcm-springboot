package br.com.litecode.domain;

import javax.persistence.*;
import javax.validation.constraints.Pattern;
import java.time.Instant;
import java.util.*;

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

	@Column(name = "folder_number")
	private String folderNumber;

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

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof Patient)) return false;

		Patient patient = (Patient) o;

		if (patientId != null ? !patientId.equals(patient.patientId) : patient.patientId != null) return false;

		return true;
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