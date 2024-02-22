package org.datavaultplatform.common.model.dao.custom;

import org.datavaultplatform.common.model.Permission;
import org.datavaultplatform.common.model.PermissionModel;

import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;


public interface PermissionCustomDAO extends BaseCustomDAO {

    void synchronisePermissions();

    @EntityGraph(PermissionModel.EG_PERMISSION_MODEL)
    PermissionModel find(Permission permission);

    @EntityGraph(PermissionModel.EG_PERMISSION_MODEL)
    List<PermissionModel> findByType(PermissionModel.PermissionType type);
}
