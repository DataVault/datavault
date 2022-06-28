package org.datavaultplatform.common.model.dao;

import java.util.List;
import java.util.Optional;
import org.datavaultplatform.common.model.RetentionPolicy;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Order;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional
public interface RetentionPolicyDAO extends AbstractDAO<RetentionPolicy,Integer> {

  @Override
  default List<RetentionPolicy> list() {
    return findAll(Sort.by(Order.asc("name")));
  }

  @Override
  @EntityGraph(RetentionPolicy.EG_RETENTION_POLICY)
  Optional<RetentionPolicy> findById(Integer id);

  @Override
  @EntityGraph(RetentionPolicy.EG_RETENTION_POLICY)
  List<RetentionPolicy> findAll();

  @Override
  @EntityGraph(RetentionPolicy.EG_RETENTION_POLICY)
  List<RetentionPolicy> findAll(Sort sort);
}
