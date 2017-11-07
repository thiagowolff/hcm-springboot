package br.com.litecode.domain.repository;

import br.com.litecode.domain.model.Alarm;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface AlarmRepository extends CrudRepository<Alarm, Integer> {
	@Query("select a from Alarm a where active = true")
	List<Alarm> findActiveAlarms();
}