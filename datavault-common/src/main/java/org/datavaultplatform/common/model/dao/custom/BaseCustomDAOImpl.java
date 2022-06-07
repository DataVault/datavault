package org.datavaultplatform.common.model.dao.custom;

import javax.persistence.EntityManager;
import org.hibernate.Session;

public abstract class BaseCustomDAOImpl implements BaseCustomDAO {

  private final EntityManager em;

  public BaseCustomDAOImpl(EntityManager em) {
    this.em = em;
  }

  public Session getCurrentSession() {
    return em.unwrap(Session.class);
  }

}
