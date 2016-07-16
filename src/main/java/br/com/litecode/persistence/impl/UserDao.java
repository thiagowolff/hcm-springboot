package br.com.litecode.persistence.impl;

import br.com.litecode.domain.User;
import br.com.litecode.persistence.AbstractDao;

public class UserDao extends AbstractDao<User> {
	public User findByUsername(String username) {
		return getQueryUniqueResult("findUserByUsername", username);
	}
}