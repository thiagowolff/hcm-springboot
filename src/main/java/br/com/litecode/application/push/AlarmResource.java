package br.com.litecode.application.push;

import br.com.litecode.domain.Alarm;
import org.primefaces.push.annotation.OnMessage;
import org.primefaces.push.annotation.PushEndpoint;
import org.primefaces.push.impl.JSONEncoder;

@PushEndpoint("/alarm")
public class AlarmResource {
	@OnMessage(encoders = {JSONEncoder.class})
	public Alarm onMessage(Alarm alarm) {
		return alarm;
	}
}