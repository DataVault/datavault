package org.datavaultplatform.webapp.model;

import org.datavaultplatform.common.model.PermissionModel;
import org.datavaultplatform.common.model.RoleModel;

import java.util.Collection;

public record RoleViewModel(RoleModel role, Collection<PermissionModel> unsetPermissions) {

}
