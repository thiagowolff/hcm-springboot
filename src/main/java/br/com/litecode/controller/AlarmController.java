package br.com.litecode.controller;

import br.com.litecode.domain.model.Alarm;
import br.com.litecode.domain.repository.AlarmRepository;
import br.com.litecode.service.AlarmService;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.faces.view.ViewScoped;
import java.io.Serializable;

@ViewScoped
@Component
@CacheConfig(cacheNames = "alarm")
public class AlarmController implements Serializable {
	@Autowired
	private AlarmRepository alarmRepository;

	@Autowired
	private AlarmService alarmService;

	@Getter
	@Setter
	private Alarm alarm;

	@PostConstruct
	public void init() {
    	alarm = new Alarm();
	}

	@Cacheable(key = "#root.methodName")
	public Iterable<Alarm> getAlarms() {
		return alarmRepository.findAll();
	}

	@CacheEvict(allEntries = true)
	public void saveAlarm() {
		alarmRepository.save(alarm);

		if (alarm.getAlarmType() == Alarm.AlarmType.CRON) {
			if (alarm.isActive()) {
				alarmService.initializeAlarm(alarm);
			} else {
				alarmService.cancelAlarm(alarm);
			}
		}
	}

	@CacheEvict(allEntries = true)
	public void deleteAlarm() {
		alarm.setActive(false);
		alarmRepository.save(alarm);
	}

	public void newAlarm() {
		alarm = new Alarm();
	}
}