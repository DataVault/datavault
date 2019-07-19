package org.datavaultplatform.common.model.dao;

import org.datavaultplatform.common.model.Permission;
import org.datavaultplatform.common.model.PermissionModel;

import java.util.Collection;

public interface PermissionDAO {

    PermissionModel find(Permission permission);

    Collection<PermissionModel> findAll();
}
