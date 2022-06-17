package org.datavaultplatform.common.model.dao.custom;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaQuery;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.Session;

@Slf4j
public abstract class BaseCustomDAOImpl implements BaseCustomDAO {

  protected final EntityManager em;

  public BaseCustomDAOImpl(EntityManager em) {
    this.em = em;
  }

  public Session getCurrentSession() {
    return em.unwrap(Session.class);
  }

  protected <T> T getSingleResult(CriteriaQuery<T> criteriaQuery) {
    try {
      return em.createQuery(criteriaQuery).getSingleResult();
    } catch (NoResultException ex) {
      log.debug("no result", ex);
      return null;
    }
  }

  private <T> T getSingleResult(TypedQuery<T> typedQuery) {
    try {
      return typedQuery.getSingleResult();
    } catch (NoResultException ex) {
      log.debug("no result", ex);
      return null;
    }
  }


  protected long getCount(TypedQuery<Long> typedQuery) {
    Long result = typedQuery.getSingleResult();
    return result == null ? 0 : result.longValue();
  }
}
