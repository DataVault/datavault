package org.datavaultplatform.common.model.dao.custom;

import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Root;

import com.oracle.bmc.util.internal.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.datavaultplatform.common.util.DaoUtils;
import org.springframework.util.Assert;

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

  public static Path<?> getPathFromNestedProperties(Root<?> rt, String sort) {
    Assert.isTrue(rt != null, "The rt parameter cannot be null");
    Assert.isTrue(StringUtils.isNotBlank(sort), "The sort parameter cannot be blank");
    String[] segments = sort.split("\\."); // split using '.' delimiter
    Assert.isTrue(segments != null, "The segments array is null");
    Assert.isTrue(segments.length != 0, "The segments array is empty");
    Path<?> sortPath = rt.get(segments[0]);
    for (int i = 1; i < segments.length; i++) {
      String segment = segments[i];
      sortPath = sortPath.get(segment);
    }
    return sortPath;
  }
}
