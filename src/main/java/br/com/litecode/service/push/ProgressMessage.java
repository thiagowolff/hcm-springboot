package br.com.litecode.service.push;

import br.com.litecode.domain.model.Session;
import lombok.Value;

import java.io.Serializable;

@Value
public class ProgressMessage implements Serializable {
	private Integer sessionId;
	private long currentProgress;
	private String timeRemaining;

	public static ProgressMessage create(Session session) {
		return new ProgressMessage(session.getSessionId(), session.getCurrentProgress(), session.getTimeRemaining());
	}
}
