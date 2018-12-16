package br.com.litecode.domain.model;

import br.com.litecode.util.TextUtil;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Embeddable;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

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
		auditInfo.append(createdDate == null ? "" : "Criado em: " + formatDate(createdDate) + System.lineSeparator());
		auditInfo.append(createdBy == null ? "" : "Criado por: " + createdBy.getName() + System.lineSeparator());
		auditInfo.append(modifiedDate == null ? "" : "Modificado em: " + formatDate(modifiedDate) + System.lineSeparator());
		auditInfo.append(modifiedBy == null ? "" : "Modificado por: " + modifiedBy.getName());

		return TextUtil.toHtmlLineBreaks(auditInfo.toString());
	}

	private String formatDate(Instant instant) {
		return LocalDateTime.ofInstant(instant, ZoneId.systemDefault()).format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));
	}
}