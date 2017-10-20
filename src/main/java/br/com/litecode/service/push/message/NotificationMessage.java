package br.com.litecode.service.push.message;

import br.com.litecode.domain.model.Session;
import br.com.litecode.util.MessageUtil;
import lombok.Value;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

@Value
public class NotificationMessage implements Serializable {
	private String eventType;
	private String messageSummary;
	private String messageDetail;

	public static NotificationMessage create(Session session, String eventType) {
		ZoneId zoneId = session.getContextData().getTimeZone() != null ? ZoneId.of(session.getContextData().getTimeZone()) : ZoneId.systemDefault();
		String messageKey = "message." + eventType.toLowerCase();
		String messageSummary = MessageUtil.getMessage(messageKey + ".summary", session.getChamber().getName(), session.getSessionId());
		String messageDetail = MessageUtil.getMessage(messageKey + ".detail", LocalDateTime.now(zoneId).format(DateTimeFormatter.ofPattern("HH:mm:ss")));

		return new NotificationMessage(eventType.toLowerCase(), messageSummary, messageDetail);
	}
}
