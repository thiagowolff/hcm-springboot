package br.com.litecode.application.push;

import org.primefaces.push.annotation.OnMessage;
import org.primefaces.push.annotation.PushEndpoint;
import org.primefaces.push.impl.JSONEncoder;

@PushEndpoint("/notify")
public class NotifyResource {
	@OnMessage(encoders = {JSONEncoder.class})
	public NotificationMessage onMessage(NotificationMessage notificationMessage) {
		return notificationMessage;
	}
}