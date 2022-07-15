package org.datavaultplatform.common.model.dao;

import java.util.List;
import java.util.Optional;
import org.datavaultplatform.common.model.User;
import org.datavaultplatform.common.model.dao.custom.UserCustomDAO;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional
public interface UserDAO extends BaseDAO<User>, UserCustomDAO {

  @Override
  @EntityGraph(User.EG_USER)
  Optional<User> findById(String id);

  @Override
  @EntityGraph(User.EG_USER)
  List<User> findAll();
}
