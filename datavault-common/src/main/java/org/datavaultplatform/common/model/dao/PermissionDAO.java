package org.datavaultplatform.common.model.dao;

import org.datavaultplatform.common.model.Permission;
import org.datavaultplatform.common.model.PermissionModel;

import java.util.Collection;
import java.util.List;

public interface PermissionDAO {

    void synchronisePermissions();

    PermissionModel find(Permission permission);

    Collection<PermissionModel> findAll();

    List<PermissionModel> findByType(PermissionModel.PermissionType type);
}
