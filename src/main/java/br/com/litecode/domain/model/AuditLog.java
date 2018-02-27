package br.com.litecode.domain.model;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Embeddable;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import java.time.Instant;

@Embeddable
@Getter
@Setter
public class AuditLog {
	private Instant createdDate;
	private Instant modifiedDate;

	@OneToOne
	@JoinColumn(name = "created_by")
	private User createdBy;

	@OneToOne
	@JoinColumn(name = "modified_by")
	private User modifiedBy;

	public AuditLog() {
	}
}