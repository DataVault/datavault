package org.datavaultplatform.common.model.dao;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.*;
import lombok.extern.slf4j.Slf4j;
import org.datavaultplatform.common.model.Group;
import org.datavaultplatform.common.model.Group_;
import org.datavaultplatform.common.util.DaoUtils;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
public class SchoolPermissionQueryHelper<T> {

  private final EntityManager em;

  private final Class<T> criteriaType;
  private final CriteriaBuilder cb;
  private final Set<String> schoolIds = new HashSet<>();

  private TypeToSchoolGroupJoinGenerator<T> typeToSchoolGroupGenerator;
  private Integer firstResult;
  private Integer maxResults;
  private OrderByHelper<T> orderByHelper;
  private PredicateHelper<T> predicateHelper;
  private SinglePredicateHelper<T> singlePredicateHelper;
  public SchoolPermissionQueryHelper(EntityManager em, Class<T> criteriaType) {
    this.criteriaType = criteriaType;
    this.em = em;
    this.cb = em.getCriteriaBuilder();
    this.typeToSchoolGroupGenerator = null;
    this.orderByHelper = (cb,rt) -> Collections.emptyList();
    this.predicateHelper = (cb,rt) -> Collections.emptyList();
    this.singlePredicateHelper = (cb,rt) -> null;
  }

  public SchoolPermissionQueryHelper<T> setSchoolIds(Set<String> schoolIds) {
    this.schoolIds.clear();
    this.schoolIds.addAll(schoolIds);
    return this;
  }

  public SchoolPermissionQueryHelper<T> setTypeToSchoolGenerator(
      TypeToSchoolGroupJoinGenerator<T> generator) {
    this.typeToSchoolGroupGenerator = generator;
    return this;
  }

  public SchoolPermissionQueryHelper<T> setNumericExpressionHelper(
      TypeToSchoolGroupJoinGenerator<T> generator) {
    this.typeToSchoolGroupGenerator = generator;
    return this;
  }

  public boolean hasNoAccess() {
    return schoolIds.isEmpty();
  }

  protected Predicate[] getPredicates(Root<T> root) {
    List<Predicate> result = new ArrayList<>();
    if (!schoolIds.contains(DaoUtils.FULL_ACCESS_INDICATOR)) {
      Path<Group> groupPath = criteriaType == Group.class
          ? (Root<Group>) root
          : typeToSchoolGroupGenerator.generateJoinToSchoolGroup(root);
      Predicate restrictOnSchoolIds = groupPath.get(Group_.id).in(schoolIds);
      result.add(restrictOnSchoolIds);
    }
    if (this.predicateHelper != null) {
      List<Predicate> additional = this.predicateHelper.getPredicates(this.cb, root);
      result.addAll(additional.stream().filter(Objects::nonNull).collect(Collectors.toList()));
    }
    if (this.singlePredicateHelper != null) {
      Predicate single = this.singlePredicateHelper.getPredicate(this.cb, root);
      if(single != null) {
        result.add(single);
      }
    }
    Predicate[] array = result.toArray(new Predicate[0]);
    return array;
  }

  public List<T> getItems(boolean distinct) {
    CriteriaQuery<T> cq = cb.createQuery(this.criteriaType);
    Root<T> root = cq.from(this.criteriaType);
    cq.select(root).distinct(distinct);
    cq.where(this.getPredicates(root));
    cq.orderBy(this.orderByHelper.getOrders(this.cb, root));

    TypedQuery<T> queryWithEG = getQueryWithFirstAndMaxRestrictions(cq);
    debugTopLevelJoins(root);
    return DaoUtils.addEntityGraph(em, this.criteriaType, queryWithEG).getResultList();
  }

  void debugTopLevelJoins(Root<T> root) {
    Set<Join<T, ?>> joins = root.getJoins();
    int i = 0;
    for (Join<T, ?> join : joins) {
      String joinName = join.getAttribute().getName();
      String msg = String.format("JOIN [%d] on [%s]:[%s]%n", i, joinName, join.getJoinType());
      log.info(msg);
      i++;
    }
  }

  public List<T> getItems() {
    return getItems(true);
  }


  public Long getItemCount() {
    CriteriaQuery<Long> cq = cb.createQuery(Long.class);
    Root<T> root = cq.from(this.criteriaType);
    cq.select(cb.countDistinct(root));
    Predicate[] preds = this.getPredicates(root);
    if (preds.length > 0) {
      cq.where(preds);
    }
    return getQueryWithFirstAndMaxRestrictions(cq).getSingleResult();
  }

  public Long queryForNumber(NumericExpressionHelper<T> numericExpressionHelper) {
    CriteriaQuery<Long> cq = cb.createQuery(Long.class);
    Root<T> root = cq.from(this.criteriaType);
    cq.select(numericExpressionHelper.getNumericExpression(cb, root));
    cq.where(this.getPredicates(root));
    return getQueryWithFirstAndMaxRestrictions(cq).getSingleResult();
  }

  private <V> TypedQuery<V> getQueryWithFirstAndMaxRestrictions(CriteriaQuery<V> cq) {
    TypedQuery<V> typedQuery = em.createQuery(cq);
    if (this.firstResult != null) {
      typedQuery.setFirstResult(firstResult);
    }
    if (this.maxResults != null) {
      typedQuery.setMaxResults(maxResults);
    }
    return typedQuery;
  }

  public void setMaxResults(Integer maxResults) {
    this.maxResults = maxResults;
  }

  public void setFirstResult(Integer firstResult) {
    this.firstResult = firstResult;
  }

  public void setPredicateHelper(PredicateHelper<T> predicateHelper) {
    this.predicateHelper = predicateHelper;
  }

  public void setSinglePredicateHelper(SinglePredicateHelper<T> singlePredicateHelper) {
    this.singlePredicateHelper = singlePredicateHelper;
  }

  public void setOrderByHelper(OrderByHelper<T> orderByHelper) {
    this.orderByHelper = orderByHelper;
  }

  public interface TypeToSchoolGroupJoinGenerator<U> {
    Join<?, Group> generateJoinToSchoolGroup(Root<U> root);
  }
  public interface OrderByHelper<V> {
    List<Order> getOrders(CriteriaBuilder cb, Root<V> root);
  }
  public interface PredicateHelper<V> {
    List<Predicate> getPredicates(CriteriaBuilder cb, Root<V> root);
  }
  public interface SinglePredicateHelper<V> {
    Predicate getPredicate(CriteriaBuilder cb, Root<V> root);
  }

  public interface NumericExpressionHelper<V> {
    Expression<Long> getNumericExpression(CriteriaBuilder cb, Root<V> root);
  }
}