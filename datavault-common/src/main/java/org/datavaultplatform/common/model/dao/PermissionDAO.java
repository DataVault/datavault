package org.datavaultplatform.common.model.dao;

import java.util.List;
import java.util.Optional;
import org.datavaultplatform.common.model.PermissionModel;
import org.datavaultplatform.common.model.dao.custom.PermissionCustomDAO;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional
public interface PermissionDAO extends BaseDAO<PermissionModel>, PermissionCustomDAO {

  @Override
  @EntityGraph(PermissionModel.EG_PERMISSION_MODEL)
  Optional<PermissionModel> findById(String id);

  @Override
  @EntityGraph(PermissionModel.EG_PERMISSION_MODEL)
  List<PermissionModel> findAll();
}
