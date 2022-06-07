package org.datavaultplatform.common.model.dao.custom;

import org.datavaultplatform.common.model.Permission;
import org.datavaultplatform.common.model.PermissionModel;

import java.util.List;


public interface PermissionCustomDAO extends BaseCustomDAO {

    void synchronisePermissions();

    PermissionModel find(Permission permission);

    List<PermissionModel> findByType(PermissionModel.PermissionType type);
}
