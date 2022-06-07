package org.datavaultplatform.common.model.dao;

import org.datavaultplatform.common.model.PermissionModel;
import org.datavaultplatform.common.model.dao.custom.PermissionCustomDAO;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional
public interface PermissionDAO extends BaseDAO<PermissionModel>, PermissionCustomDAO {
}
