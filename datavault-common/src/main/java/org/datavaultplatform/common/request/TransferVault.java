package org.datavaultplatform.common.request;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.jsondoc.core.annotation.ApiObject;
import org.jsondoc.core.annotation.ApiObjectField;

@JsonIgnoreProperties(ignoreUnknown = true)
@ApiObject(name = "TransferVault")
public class TransferVault {

    @ApiObjectField(description = "The reason for the vault transfer", required = true)
    private String reason;

    @ApiObjectField(description = "The ID of the user to transfer the vault to", required = true)
    private String userId;

    @ApiObjectField(description = "The ID of the role to associate with the previous vault owner")
    private Long roleId;

    @ApiObjectField(description = "If the previous data owner is changing to a new role as part of this transfer")
    private boolean changingRoles;

    @ApiObjectField(description = "If the vault is being orphaned. Only possible to do so if the user is an administrator. Mutually exclusive with `changingRolse`")
    private boolean orphaning;

    public Long getRoleId() {
        return roleId;
    }

    public boolean isOrphaning() {
        return orphaning;
    }

    public void setOrphaning(boolean orphaning) {
        this.orphaning = orphaning;
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

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
