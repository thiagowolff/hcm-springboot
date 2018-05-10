package br.com.litecode.service;

import br.com.litecode.domain.model.Alarm;
import br.com.litecode.domain.repository.AlarmRepository;
import br.com.litecode.service.push.PushChannel;
import br.com.litecode.service.push.PushService;
import br.com.litecode.service.push.message.NotificationMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.Trigger;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;

@Service
@Slf4j
public class AlarmService {
	@Autowired
	private AlarmRepository alarmRepository;

	@Autowired
	private PushService pushService;

	@Autowired
	private TaskScheduler taskScheduler;

	Map<Integer, ScheduledFuture> scheduledAlarms;

	@PostConstruct
	private void init() {
		scheduledAlarms = new HashMap<>();
		initializeAlarms();
	}

	public void initializeAlarm(Alarm alarm) {
		cancelAlarm(alarm);

		Trigger trigger = new CronTrigger(alarm.getCronExpression());
		Runnable task = () -> pushService.publish(PushChannel.NOTIFY,  new NotificationMessage(null, null, alarm.getName(), alarm.getMessage()), null);
		scheduledAlarms.put(alarm.getAlarmId(), taskScheduler.schedule(task, trigger));

		log.info("Alarm {} initialized.", alarm);
	}

	public void cancelAlarm(Alarm alarm) {
		ScheduledFuture scheduledAlarm = scheduledAlarms.get(alarm.getAlarmId());
		if (scheduledAlarm != null) {
			scheduledAlarm.cancel(true);
		}
	}

	private void initializeAlarms() {
		List<Alarm> alarms = alarmRepository.findActiveAlarms();

		for (Alarm alarm : alarms) {
			initializeAlarm(alarm);
		}
	}
}
