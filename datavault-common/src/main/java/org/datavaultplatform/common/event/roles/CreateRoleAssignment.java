package org.datavaultplatform.common.event.roles;

import org.datavaultplatform.common.event.Event;
import org.datavaultplatform.common.model.RoleAssignment;

import jakarta.persistence.Entity;

@Entity
public class CreateRoleAssignment extends Event {

    public CreateRoleAssignment() {
    }
    public CreateRoleAssignment(RoleAssignment roleAssignment, String creatorId) {
        super(roleAssignment.getRole().getName()+" role given to "+roleAssignment.getUserId()+" by "+creatorId);
        this.eventClass = CreateRoleAssignment.class.getCanonicalName();
    }
}
