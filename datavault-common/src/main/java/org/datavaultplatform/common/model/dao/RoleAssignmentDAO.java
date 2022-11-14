package org.datavaultplatform.common.model.dao;

import java.util.List;
import java.util.Optional;
import org.datavaultplatform.common.model.RoleAssignment;
import org.datavaultplatform.common.model.dao.custom.RoleAssignmentCustomDAO;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional
public interface RoleAssignmentDAO
    extends AbstractDAO<RoleAssignment,Long>, RoleAssignmentCustomDAO {

  @Override
  @EntityGraph(RoleAssignment.EG_ROLE_ASSIGNMENT)
  Optional<RoleAssignment> findById(Long id);

  @Override
  @EntityGraph(RoleAssignment.EG_ROLE_ASSIGNMENT)
  List<RoleAssignment> findAll();
}
