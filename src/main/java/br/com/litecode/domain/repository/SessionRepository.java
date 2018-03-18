package br.com.litecode.domain.repository;

import br.com.litecode.domain.model.Session;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.math.BigInteger;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

public interface SessionRepository extends CrudRepository<Session, Integer> {
	default List<Session> findSessionsByDate(LocalDate sessionDate) {
		return findSessionsByDate(sessionDate.atStartOfDay(), sessionDate.atTime(23, 59, 59));
	}

	default List<Session> findSessionsByChamberAndDate(Integer chamberId, LocalDate sessionDate) {
		return findSessionsByChamberAndDate(chamberId, sessionDate.atStartOfDay(), sessionDate.atTime(23, 59, 59));
	}

	@Query("select s from Session s where s.scheduledTime between :startOfDay and :endOfDay order by s.scheduledTime, s.sessionId")
	List<Session> findSessionsByDate(LocalDateTime startOfDay, LocalDateTime endOfDay);

	@Query("select s from Session s where s.chamber.chamberId = :chamberId and s.scheduledTime between :startOfDay and :endOfDay order by s.scheduledTime, s.sessionId")
	List<Session> findSessionsByChamberAndDate(Integer chamberId, LocalDateTime startOfDay, LocalDateTime endOfDay);

	@Query("select s.scheduledTime from Session s order by s.scheduledTime desc")
	List<LocalDateTime> findScheduledSessionDates();
}