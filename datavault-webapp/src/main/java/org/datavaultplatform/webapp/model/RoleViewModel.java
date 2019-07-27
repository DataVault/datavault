package org.datavaultplatform.webapp.model;


import com.google.common.collect.Sets;
import org.datavaultplatform.common.model.Permission;
import org.datavaultplatform.common.model.PermissionModel;
import org.datavaultplatform.common.model.RoleModel;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


public class RoleViewModel extends RoleModel {

    private RoleModel role;

    public RoleViewModel() {
        unsetpermissions = new ArrayList<PermissionModel>();
    }

    public void setRole(RoleModel role)
    {
        this.role = role;
    }

    public RoleModel getRole()
    {
       return role;
    }



    private Collection<PermissionModel> unsetpermissions;

    public Collection<PermissionModel> getPermissions() {
        return Sets.newHashSet(unsetpermissions);
    }

    public void setPermissions(Collection<PermissionModel> permissions) {
        this.unsetpermissions = permissions;
    }

    public boolean addPermission(PermissionModel permissionModel) {
        if (!hasPermission(permissionModel.getPermission())) {
            unsetpermissions.add(permissionModel);
            return true;
        }
        return false;
    }

    public boolean hasPermission(Permission permission) {
        return unsetpermissions.stream()
                .map(PermissionModel::getPermission)
                .anyMatch(p -> p.equals(permission));
    }
}
