package br.com.litecode.domain.repository;

import br.com.litecode.domain.model.User;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface UserRepository extends CrudRepository<User, Integer> {
	@Query("select u from User u where active = true order by username")
	List<User> findActiveUsers();

	@Query("select u from User u where u.username = :username")
	User findByUsername(String username);

	@Query("select u from User u where u.sessionId = :sessionId")
	User findUserBySessionId(String sessionId);

	@Modifying
	@Query("update User u set u.sessionId = null")
	@Transactional
	void initializeUserSessions();
}