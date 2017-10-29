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
import java.util.ArrayList;
import java.util.List;
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

	List<ScheduledFuture> scheduledAlarms;

	@PostConstruct
	private void init() {
		scheduledAlarms = new ArrayList<>();
		initializeAlarms();
	}

	public void initializeAlarms() {
		List<Alarm> alarms = alarmRepository.findActiveAlarms();

		cancelAlarms();

		for (Alarm alarm : alarms) {
			Trigger trigger = new CronTrigger(alarm.getCronExpression());
			Runnable task = () -> pushService.publish(PushChannel.NOTIFY,  new NotificationMessage(null, null, alarm.getName(), alarm.getMessage(), null), null);
			scheduledAlarms.add(taskScheduler.schedule(task, trigger));

			log.info("Alarm {} initialized.", alarm);
		}
	}

	private void cancelAlarms() {
		scheduledAlarms.forEach(sf -> sf.cancel(true));
	}
}
