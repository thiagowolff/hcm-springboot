package br.com.litecode.controller;

import br.com.litecode.domain.model.Alarm;
import br.com.litecode.domain.repository.AlarmRepository;
import br.com.litecode.service.AlarmService;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.faces.view.ViewScoped;
import java.io.Serializable;

@ViewScoped
@Component
public class AlarmController implements Serializable {
	@Autowired
	private AlarmRepository alarmRepository;

	@Autowired
	private AlarmService alarmService;

	@Getter
	@Setter
	private Alarm alarm;

	@Setter
	private Iterable<Alarm> alarms;

	@PostConstruct
	public void init() {
    	alarm = new Alarm();
	}

	public Iterable<Alarm> getAlarms() {
		if (alarms == null) {
			alarms = alarmRepository.findAll();
		}
		return alarms;
	}

	@CacheEvict(cacheNames = "patient", allEntries = true)
	public void saveAlarm() {
		alarmRepository.save(alarm);

		if (alarm.getAlarmType() == Alarm.AlarmType.CRON) {
			if (alarm.isActive()) {
				alarmService.initializeAlarm(alarm);
			} else {
				alarmService.cancelAlarm(alarm);
			}
		}

		alarms = null;
	}
	
	public void deleteAlarm() {
		alarm.setActive(false);
		alarmRepository.save(alarm);
		alarms = null;
	}

	public void newAlarm() {
		alarm = new Alarm();
	}
}