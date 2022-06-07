package org.datavaultplatform.common.model.dao;

import org.datavaultplatform.common.model.RoleAssignment;
import org.datavaultplatform.common.model.dao.custom.RoleAssignmentCustomDAO;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional
public interface RoleAssignmentDAO
    extends AbstractDAO<RoleAssignment,Long>, RoleAssignmentCustomDAO {
}
