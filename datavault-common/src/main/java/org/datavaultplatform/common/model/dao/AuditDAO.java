package org.datavaultplatform.common.model.dao;

import org.datavaultplatform.common.model.Audit;
import org.datavaultplatform.common.model.Audit_;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Order;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
@Transactional
public interface AuditDAO extends BaseDAO<Audit> {

  @Override
  @EntityGraph(Audit.EG_AUDIT)
  default List<Audit> list() {
    return findAll(Sort.by(Order.asc(Audit_.TIMESTAMP)));
  }

  @Override
  @EntityGraph(Audit.EG_AUDIT)
  Optional<Audit> findById(String id);

  @Override
  @EntityGraph(Audit.EG_AUDIT)
  List<Audit> findAll();

  @Override
  @EntityGraph(Audit.EG_AUDIT)
  List<Audit> findAll(Sort sort);
}
