package br.com.litecode.service.timer;

public interface ChamberSessionTimerMBean {
	void initializeAlarms();
	void pushAlarm(String name, String message);
	int getNumberOfActiveTimers();
}
