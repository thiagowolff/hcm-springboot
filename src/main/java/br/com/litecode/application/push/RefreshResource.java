package br.com.litecode.application.push;

import org.primefaces.push.annotation.OnMessage;
import org.primefaces.push.annotation.PushEndpoint;
import org.primefaces.push.impl.JSONEncoder;

@PushEndpoint("/refresh")
public class RefreshResource {
	@OnMessage(encoders = {JSONEncoder.class})
	public void onMessage(String message) {
	}
}