package br.com.litecode.service.timer;

import br.com.litecode.domain.model.Session;

public interface SessionTimer {
	void startSession(Session session);
	void stopSession(Session session);
}
