package org.datavaultplatform.common.model.dao;

import java.util.List;
import java.util.Optional;
import org.datavaultplatform.common.model.Group;
import org.datavaultplatform.common.model.Group_;
import org.datavaultplatform.common.model.dao.custom.GroupCustomDAO;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Order;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional
public interface GroupDAO extends BaseDAO<Group>, GroupCustomDAO {

  @Override
  @EntityGraph(Group.EG_GROUP)
  default List<Group> list() {
    return findAll(Sort.by(Order.asc(Group_.NAME)));
  }

  @Override
  @EntityGraph(Group.EG_GROUP)
  Optional<Group> findById(String id);

  @Override
  @EntityGraph(Group.EG_GROUP)
  List<Group> findAll();

  @Override
  @EntityGraph(Group.EG_GROUP)
  List<Group> findAll(Sort sort);

}
