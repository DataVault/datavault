package org.datavaultplatform.common.request;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.jsondoc.core.annotation.ApiObject;
import org.jsondoc.core.annotation.ApiObjectField;

import java.util.OptionalLong;

@JsonIgnoreProperties(ignoreUnknown = true)
@ApiObject(name = "TransferVault")
public class TransferVault {

    @ApiObjectField(description = "The ID of the user to transfer the vault to", required = true)
    private String userId;

    @ApiObjectField(description = "The ID of the role to associate with the previous vault owner")
    private Long roleId;

    @ApiObjectField(description = "If the previous data owner is changing to a new role as part of this transfer")
    private boolean changingRoles;

    public Long getRoleId() {
        return roleId;
    }

    public boolean isChangingRoles() {
        return changingRoles;
    }

    public void setChangingRoles(boolean changingRoles) {
        this.changingRoles = changingRoles;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setRoleId(Long roleId) {
        this.roleId = roleId;
    }
}
