package br.com.litecode.domain;

import javax.annotation.PostConstruct;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Embeddable
public class Address {
	public enum State { AC,	AL,	AP,	AM,	BA,	CE,	DF,	ES,	GO,	MA,	MT,	MS,	MG,	PA,	PB,	PR,	PE,	PI,	RR,	RO,	RJ,	RN,	RS,	SC,	SP,	SE,	TO }

	@Column(name = "address_street")
	private String addressStreet;

	@Column(name = "address_complement")
	private String addressComplement;

	@Column(name = "address_number")
	private String addressNumber;

	@Column
	private String neighborhood;
	
	@Column
	private String city;
	
	@Enumerated(EnumType.STRING)
	private State state;
	
	@Column(name = "zip_code")
	private String zipCode;
	
	@Column(name = "phone_number")
	private String phoneNumber;

	public String getAddressStreet() {
		return addressStreet;
	}

	public void setAddressStreet(String addressStreet) {
		this.addressStreet = addressStreet;
	}

	public String getAddressComplement() {
		return addressComplement;
	}
	
	public String getAddressNumber() {
		return addressNumber;
	}

	public void setAddressNumber(String addressNumber) {
		this.addressNumber = addressNumber;
	}

	public void setAddressComplement(String addressComplement) {
		this.addressComplement = addressComplement;
	}

	public String getNeighborhood() {
		return neighborhood;
	}

	public void setNeighborhood(String neighborhood) {
		this.neighborhood = neighborhood;
	}

	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public State getState() {
		return state;
	}

	public void setState(State state) {
		this.state = state;
	}

	public String getZipCode() {
		return zipCode;
	}

	public void setZipCode(String zipCode) {
		this.zipCode = zipCode;
	}

	public String getPhoneNumber() {
		return phoneNumber;
	}

	public void setPhoneNumber(String phone) {
		this.phoneNumber = phone;
	}
}
