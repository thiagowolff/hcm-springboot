package br.com.litecode.service.timer;

import java.util.Set;

public interface TimeTicker<T> {
	void start(T listener);

	void stop(T listener);

	void register(T listener, Runnable task, long delay);

	Set<T> getActiveListeners();

	default void elapseTime(T listener) {
	}
}
