package org.datavaultplatform.common.model.dao.custom;

import java.util.Collection;
import java.util.List;
import org.datavaultplatform.common.model.Permission;
import org.datavaultplatform.common.model.PermissionModel;

public interface PermissionCustomDAO {

    void synchronisePermissions();

    PermissionModel find(Permission permission);

    Collection<PermissionModel> findAll();

    List<PermissionModel> findByType(PermissionModel.PermissionType type);
}
