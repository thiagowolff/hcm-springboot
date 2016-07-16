package br.com.litecode.persistence;

import java.util.List;

public interface GenericDao<T> {
	List<T> findAll();
	List<T> getQueryResults(String queryName, Object... parameters);
	T getQueryUniqueResult(String queryName, Object... parameters);
	T findById(Object id);
	T update(T entity);
	void insert(T entity);
	void delete(T entity);
}
