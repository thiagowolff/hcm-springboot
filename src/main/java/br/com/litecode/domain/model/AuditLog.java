package br.com.litecode.domain.model;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.time.Instant;

@Entity
@Getter
@Setter
public class AuditLog {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer auditId;

	private Instant auditDate;
	private String message;

	@ManyToOne
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	public AuditLog() {
	}

	public AuditLog(String message, User user) {
		this.message = message;
		this.user = user;
		auditDate = Instant.now();
	}
}