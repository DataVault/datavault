package org.datavaultplatform.common.model.dao.custom;

import org.datavaultplatform.common.model.Permission;
import org.datavaultplatform.common.model.PermissionModel;
import org.springframework.data.jpa.repository.EntityGraph;

import java.util.List;


public interface PermissionCustomDAO extends BaseCustomDAO {

    void synchronisePermissions();

    @EntityGraph(PermissionModel.EG_PERMISSION_MODEL)
    PermissionModel find(Permission permission);

    @EntityGraph(PermissionModel.EG_PERMISSION_MODEL)
    List<PermissionModel> findByType(PermissionModel.PermissionType type);
}
