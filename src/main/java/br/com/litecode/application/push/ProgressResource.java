package br.com.litecode.application.push;

import br.com.litecode.domain.Session;
import org.primefaces.push.annotation.OnMessage;
import org.primefaces.push.annotation.PushEndpoint;
import org.primefaces.push.impl.JSONEncoder;

import javax.faces.application.FacesMessage;

@PushEndpoint("/progress")
public class ProgressResource {
	@OnMessage(encoders = {JSONEncoder.class})
	public Session onMessage(Session session) {
		return session;
	}
}