package br.com.litecode.domain.model;

import com.google.common.base.Joiner;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Embeddable;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.criteria.CriteriaBuilder;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

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

	@Override
	public String toString() {
		StringBuilder auditInfo = new StringBuilder();
		auditInfo.append(createdDate == null ? "" : "Criado em: " + formatDate(createdDate) + "<br/>");
		auditInfo.append(createdBy == null ? "" : "Criado por: " + createdBy.getName() + "<br/>");
		auditInfo.append(modifiedDate == null ? "" : "Modificado em: " + formatDate(modifiedDate) + "<br/>");
		auditInfo.append(modifiedBy == null ? "" : "Modificado por: " + modifiedBy.getName());

		return auditInfo.toString();
	}

	private String formatDate(Instant instant) {
		return LocalDateTime.ofInstant(instant, ZoneId.systemDefault()).format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));
	}
}