package br.com.litecode.application.push;

import br.com.litecode.domain.Session;
import org.primefaces.push.annotation.OnMessage;
import org.primefaces.push.annotation.PushEndpoint;
import org.primefaces.push.impl.JSONEncoder;

@PushEndpoint("/progress")
public class ProgressResource {
	@OnMessage(encoders = {JSONEncoder.class})
	public SessionProgressMessage onMessage(SessionProgressMessage session) {
		return session;
	}
}