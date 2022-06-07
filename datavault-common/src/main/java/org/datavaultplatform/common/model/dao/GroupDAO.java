package org.datavaultplatform.common.model.dao;

import java.util.List;
import org.datavaultplatform.common.model.Group;
import org.datavaultplatform.common.model.dao.custom.GroupCustomDAO;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Order;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional
public interface GroupDAO extends BaseDAO<Group>, GroupCustomDAO {

  @Override
  default List<Group> list() {
    return findAll(Sort.by(Order.asc("name")));
  }
}
