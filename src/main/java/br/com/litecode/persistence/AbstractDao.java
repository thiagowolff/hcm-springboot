package br.com.litecode.persistence;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaQuery;

public abstract class AbstractDao<T> implements GenericDao<T> {
	@PersistenceContext
	protected EntityManager entityManager;
	
	private final Class<T> entityType;
	
   @SuppressWarnings("unchecked")
   public AbstractDao() {
        ParameterizedType parameterizedType = (ParameterizedType) getClass().getGenericSuperclass();
        Type type = parameterizedType.getActualTypeArguments()[0];
        entityType = (Class<T>) type;
    }

	@Override
	public T findById(Object id) {
		try {
			return entityManager.find(entityType, id);
		} catch (NoResultException e) {
			return null;
		}
	}

	@Override
	public List<T> findAll() {
		CriteriaQuery<T> criteriaQuery = entityManager.getCriteriaBuilder().createQuery(entityType);
		criteriaQuery.from(entityType);
		return entityManager.createQuery(criteriaQuery).getResultList();
	}

	@Override
	public List<T> getQueryResults(String queryName, Object... parameters) {
		TypedQuery<T> query = entityManager.createNamedQuery(queryName, entityType);
		for (int i = 0; i < parameters.length; i++) {
			query.setParameter(i + 1, parameters[i]);
		}
		return query.getResultList();
	}
	
	@Override
	public T getQueryUniqueResult(String queryName, Object... parameters) {
		TypedQuery<T> query = entityManager.createNamedQuery(queryName, entityType);
		for (int i = 0; i < parameters.length; i++) {
			query.setParameter(i + 1, parameters[i]);
		}

		try {
			return query.getSingleResult();
		} catch(NoResultException e) {
			return null;
		}
	}
	
	@Override
	public void insert(T entity) {
		entityManager.persist(entity);
	}

	@Override
	public T update(T entity) {
		return entityManager.merge(entity);
	}

	@Override
	public void delete(T entity) {
		entityManager.remove(entityManager.merge(entity));	
	}
}
