package org.datavaultplatform.common.event.roles;

import org.datavaultplatform.common.event.Event;
import org.datavaultplatform.common.model.RoleAssignment;

import jakarta.persistence.Entity;

@Entity
public class DeleteRoleAssignment extends Event {

    public DeleteRoleAssignment() {
    }
    public DeleteRoleAssignment(RoleAssignment roleAssignment, String userId) {
        super(roleAssignment.getRole().getName()+" role removed from "+roleAssignment.getUserId()+" by "+userId);
        this.eventClass = DeleteRoleAssignment.class.getCanonicalName();
    }
}
