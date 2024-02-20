package org.datavaultplatform.common.model.dao.custom;

import java.util.List;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaQuery;
import lombok.extern.slf4j.Slf4j;
import org.datavaultplatform.common.util.DaoUtils;

@Slf4j
public abstract class BaseCustomDAOImpl implements BaseCustomDAO {

  protected final EntityManager em;

  public BaseCustomDAOImpl(EntityManager em) {
    this.em = em;
  }

  protected <T> T getSingleResult(CriteriaQuery<T> criteriaQuery) {
    try {
      return addEntityGraph(criteriaQuery.getResultType(), em.createQuery(criteriaQuery)).getSingleResult();
    } catch (NoResultException ex) {
      log.debug("no result", ex);
      return null;
    }
  }

  protected long getCount(TypedQuery<Long> typedQuery) {
    Long result = typedQuery.getSingleResult();
    return result == null ? 0 : result;
  }

  protected <V> List<V> getResults(CriteriaQuery<V> cq) {
    return getResults(em, cq);
  }

  protected <V> List<V> getResults(Class<V> clazz, TypedQuery<V> tq) {
    return addEntityGraph(clazz, tq).getResultList();
  }

  public <T> TypedQuery<T> addEntityGraph(Class<T> clazz, TypedQuery<T> query) {
    return addEntityGraph(em, clazz, query);
  }

  public static <T> TypedQuery<T> addEntityGraph(EntityManager em, Class<T> clazz, TypedQuery<T> query) {
    return DaoUtils.addEntityGraph(em, clazz, query);
  }
  protected static <V> List<V> getResults(EntityManager em, CriteriaQuery<V> cq) {
    return addEntityGraph(em, cq.getResultType(), em.createQuery(cq)).getResultList();
  }


  public String getQueryLower(String query) {
    return "%" + query.toLowerCase() + "%";
  }

}
