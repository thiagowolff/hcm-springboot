package br.com.litecode.application.push;

import br.com.litecode.domain.Session;

import java.io.Serializable;

public class SessionProgressMessage implements Serializable {
	private Integer sessionId;
	private long currentProgress;
	private String timeRemaining;

	public static SessionProgressMessage create(Session session) {
		return new SessionProgressMessage(session.getSessionId(), session.getCurrentProgress(), session.getTimeRemaining());
	}

	private SessionProgressMessage(Integer sessionId, long currentProgress, String timeRemaining) {
		this.sessionId = sessionId;
		this.currentProgress = currentProgress;
		this.timeRemaining = timeRemaining;
	}

	public Integer getSessionId() {
		return sessionId;
	}

	public long getCurrentProgress() {
		return currentProgress;
	}

	public String getTimeRemaining() {
		return timeRemaining;
	}
}
