package br.com.litecode.application;

public interface ChamberSessionTimerMBean {
	void initializeAlarms();
	void pushAlarm(String name, String message);
	int getNumberOfActiveTimers();
}
