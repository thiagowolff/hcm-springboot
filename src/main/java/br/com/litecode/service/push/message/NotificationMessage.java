package br.com.litecode.service.push.message;

import br.com.litecode.domain.model.ChamberEvent;
import br.com.litecode.domain.model.ChamberEvent.EventType;
import br.com.litecode.domain.model.Session;
import br.com.litecode.util.MessageUtil;
import lombok.Value;

import java.io.Serializable;

@Value
public class NotificationMessage implements Serializable {
	private String eventType;
	private String notificationSound;
	private String messageSummary;
	private String messageDetail;

	public static NotificationMessage create(Session session, EventType eventType) {
		String messageKey = "message." + eventType.toString().toLowerCase();
		String messageSummary = MessageUtil.getMessage(messageKey + ".summary", session.getChamber().getName(), session.getSessionId());
		String messageDetail = MessageUtil.getMessage(messageKey + ".detail", session.getStartTime());

		ChamberEvent chamberEvent = session.getChamber().getChamberEvent(eventType);
		return new NotificationMessage(chamberEvent.toString(), chamberEvent.getNotificationSound(), messageSummary, messageDetail);
	}
}
