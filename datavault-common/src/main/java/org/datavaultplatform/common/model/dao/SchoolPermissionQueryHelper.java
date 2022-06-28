package org.datavaultplatform.common.model.dao;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Selection;
import lombok.extern.slf4j.Slf4j;
import org.datavaultplatform.common.model.Group;
import org.datavaultplatform.common.model.Group_;
import org.datavaultplatform.common.util.DaoUtils;

@Slf4j
public class SchoolPermissionQueryHelper<T> {

  private final EntityManager em;

  private final Class<T> criteriaType;
  private final CriteriaBuilder cb;
  private final Set<String> schoolIds = new HashSet<>();

  private TypeToSchoolGroupJoinGenerator<T> typeToSchoolGroupGenerator;
  private Integer firstResult;
  private Integer maxResults;
  private OrderByHelper orderByHelper;
  private PredicateHelper predicateHelper;
  private SinglePredicateHelper singlePredicateHelper;
  private SelectionHelper<T> selectionHelper;

  public SchoolPermissionQueryHelper(EntityManager em, Class<T> criteriaType) {
    this.criteriaType = criteriaType;
    this.em = em;
    this.cb = em.getCriteriaBuilder();
    this.typeToSchoolGroupGenerator = null;
    this.orderByHelper = (cb,rt) -> Collections.emptyList();
    this.predicateHelper = (cb,rt) -> Collections.emptyList();
    this.singlePredicateHelper = (cb,rt) -> null;
    this.selectionHelper = root -> root;
  }

  public SchoolPermissionQueryHelper setSchoolIds(Set<String> schoolIds) {
    this.schoolIds.clear();
    this.schoolIds.addAll(schoolIds);
    return this;
  }

  public SchoolPermissionQueryHelper setTypeToSchoolGenerator(
      TypeToSchoolGroupJoinGenerator<T> generator) {
    this.typeToSchoolGroupGenerator = generator;
    return this;
  }

  public SchoolPermissionQueryHelper setNumericExpressionHelper (
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
          ? (Root<Group>)root
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
    Selection<T> selection = selectionHelper.getSelection(root);
    cq.select(selection).distinct(distinct);
    cq.where(this.getPredicates(root));
    cq.orderBy(this.orderByHelper.getOrders(this.cb, root));

    Set<Join<T, ?>> joins = root.getJoins();
    int i=0;
    for (Join<T, ?> join : joins) {
      System.out.printf("JOIN [%d] on [%s]:[%s]%n",i,join.getAttribute().getName(),join.getJoinType());
      i++;
    }
    TypedQuery<T> queryWithEG = getQueryWithRestrictions(cq);
    return DaoUtils.addEntityGraph(em, this.criteriaType, queryWithEG).getResultList();
  }

  public List<T> getItems() {
    return getItems(true);
  }


  public Long getItemCount() {
    CriteriaQuery<Long> cq = cb.createQuery(Long.class);
    Root<T> root = cq.from(this.criteriaType);
    cq.select(cb.countDistinct(root));
    cq.where(this.getPredicates(root));
    return getQueryWithRestrictions(cq).getSingleResult();
  }

  public Long queryForNumber(NumericExpressionHelper<T> numericExpressionHelper) {
    CriteriaQuery<Long> cq = cb.createQuery(Long.class);
    Root<T> root = cq.from(this.criteriaType);
    cq.select(numericExpressionHelper.getNumericExpression(cb, root));
    cq.where(this.getPredicates(root));
    return getQueryWithRestrictions(cq).getSingleResult();
  }

  private <V> TypedQuery<V> getQueryWithRestrictions(CriteriaQuery<V> cq) {
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

  public void setPredicateHelper(PredicateHelper predicateHelper) {
    this.predicateHelper = predicateHelper;
  }

  public void setSinglePredicateHelper(SinglePredicateHelper singlePredicateHelper) {
    this.singlePredicateHelper = singlePredicateHelper;
  }

  public void setOrderByHelper(OrderByHelper orderByHelper) {
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


  public void setSelectionHelper(SelectionHelper<T> selectionHelper) {
    this.selectionHelper = selectionHelper;
  }
  public interface SelectionHelper<S> {
    Selection<S> getSelection(Root<S> root);
  }
}
