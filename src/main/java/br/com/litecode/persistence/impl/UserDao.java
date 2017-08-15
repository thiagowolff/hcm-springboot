package br.com.litecode.persistence.impl;

import br.com.litecode.domain.User;
import br.com.litecode.persistence.AbstractDao;

import javax.persistence.Query;

public class UserDao extends AbstractDao<User> {
	public User findByUsername(String username) {
		return getQueryUniqueResult("findUserByUsername", username);
	}

	public User findBySessionId(String sessionId) {
		return getQueryUniqueResult("findUserBySessionId", sessionId);
	}

	public void initializeUserSessions() {
		Query query = entityManager.createQuery("update User u set u.sessionId = null");
		query.executeUpdate();
	}
}