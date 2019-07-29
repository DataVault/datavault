package org.datavaultplatform.webapp.model;

import org.datavaultplatform.common.model.PermissionModel;
import org.datavaultplatform.common.model.RoleModel;

import java.util.Collection;

public class RoleViewModel {

    private final Collection<PermissionModel> unsetPermissions;
    private final RoleModel role;

    public RoleViewModel(RoleModel role, Collection<PermissionModel> unsetPermissions) {
        this.role = role;
        this.unsetPermissions = unsetPermissions;
    }

    public RoleModel getRole() {
        return role;
    }

    public Collection<PermissionModel> getUnsetPermissions() {
        return unsetPermissions;
    }
}
