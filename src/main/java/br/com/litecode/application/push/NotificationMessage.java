package br.com.litecode.application.push;

import br.com.litecode.domain.ChamberEvent;

import java.io.Serializable;

public class NotificationMessage implements Serializable {
	private ChamberEvent chamberEvent;
	private String messageSummary;
	private String messageDetail;

	public NotificationMessage(ChamberEvent chamberEvent, String messageSummary, String messageDetail) {
		this.chamberEvent = chamberEvent;
		this.messageSummary = messageSummary;
		this.messageDetail = messageDetail;
	}

	public ChamberEvent getChamberEvent() {
		return chamberEvent;
	}

	public String getMessageSummary() {
		return messageSummary;
	}

	public String getMessageDetail() {
		return messageDetail;
	}

	public String getEventType() {
		return chamberEvent.getEventType().toString().toLowerCase();
	}
}
