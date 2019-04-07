package br.com.litecode.domain.repository;

import br.com.litecode.domain.model.Alarm;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

@Cacheable(cacheNames = "alarm", key = "#root.methodName")
public interface AlarmRepository extends BaseCrudRepository<Alarm, Integer> {
	@Query("select a from Alarm a where alarmType = 'CRON' and active = true")
	List<Alarm> findActiveCronAlarms();

	@Query("select a from Alarm a where alarmType = 'SCRIPT' and active = true")
	List<Alarm> findActiveScriptAlarms();
}