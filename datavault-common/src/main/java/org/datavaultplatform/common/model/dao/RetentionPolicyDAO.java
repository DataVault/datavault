package org.datavaultplatform.common.model.dao;

import java.util.List;
import org.datavaultplatform.common.model.RetentionPolicy;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Order;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional
public interface RetentionPolicyDAO extends AbstractDAO<RetentionPolicy,Integer> {

  @Override
  default List<RetentionPolicy> list() {
    return findAll(Sort.by(Order.asc("name")));
  }
}
