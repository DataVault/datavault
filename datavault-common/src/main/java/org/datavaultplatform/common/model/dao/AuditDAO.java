package org.datavaultplatform.common.model.dao;

import java.util.List;
import org.datavaultplatform.common.model.Audit;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Order;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional
public interface AuditDAO extends BaseDAO<Audit> {

  @Override
  default List<Audit> list() {
    return findAll(Sort.by(Order.asc("timestamp")));
  }
}
