package org.datavaultplatform.webapp.controllers;

import org.datavaultplatform.common.model.RoleAssignment;
import org.datavaultplatform.common.model.RoleModel;
import org.datavaultplatform.common.model.RoleType;
import org.datavaultplatform.common.model.User;
import org.datavaultplatform.common.model.Vault;
import org.datavaultplatform.webapp.exception.EntityNotFoundException;
import org.datavaultplatform.webapp.services.RestService;
import org.hibernate.validator.constraints.NotEmpty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

@Controller
public class RoleAssignmentController {

    private final RestService rest;

    @Autowired
    public RoleAssignmentController(RestService rest) {
        this.rest = rest;
    }

    @PostMapping("/security/roles/{roleType}/{target}/user/update")
    @PreAuthorize("hasPermission(#targetId, #type, 'ASSIGN_VAULT_ROLES') or hasPermission(#targetId, 'GROUP_VAULT', 'ASSIGN_SCHOOL_VAULT_ROLES')")
    public ResponseEntity updateRoleAssignment(
            @PathVariable("roleType") String type,
            @PathVariable("target") String targetId,
            @Valid @NotNull @RequestParam("assignment") Long assignmentId,
            @Valid RoleAssignmentRequest assignmentRequest) {

        RoleAssignment roleAssignment = rest.getRoleAssignment(assignmentId)
                .orElseThrow(() -> new EntityNotFoundException(RoleAssignment.class, String.valueOf(assignmentId)));

        String existingTargetId = roleAssignment.getTargetId();

        if (!existingTargetId.equals(targetId)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        RoleModel role = rest.getRole(assignmentRequest.role).orElseThrow
                (()  -> new EntityNotFoundException(RoleModel.class, String.valueOf(assignmentRequest.role)));

        roleAssignment.setRole(role);
        rest.updateRoleAssignment(roleAssignment);

        return ResponseEntity.ok().build();
    }

    @PostMapping("/security/roles/{roleType}/{target}/user/delete")
    @PreAuthorize("hasPermission(#targetId, #type, 'ASSIGN_VAULT_ROLES') or hasPermission(#targetId, 'GROUP_VAULT', 'ASSIGN_SCHOOL_VAULT_ROLES')")
    public ResponseEntity removeRoleAssignment(@PathVariable("roleType") String type,
                                               @PathVariable("target") String targetId,
                                               @RequestParam("assignment") long assignmentId) {

        RoleAssignment assignment = rest.getRoleAssignment(assignmentId)
                .orElseThrow(() -> new EntityNotFoundException(RoleAssignment.class, String.valueOf(assignmentId)));

        String existingTargetId = assignment.getTargetId();

        if (!existingTargetId.equals(targetId)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        rest.deleteRoleAssignment(assignment.getId());

        return ResponseEntity.ok().build();
    }

    @PreAuthorize("hasPermission(#targetId, #type, 'ASSIGN_VAULT_ROLES') or hasPermission(#targetId, 'GROUP_VAULT', 'ASSIGN_SCHOOL_VAULT_ROLES')")
    @PostMapping("/security/roles/{roleType}/{target}/user")
    public ResponseEntity createRoleAssignment(
            @PathVariable("roleType") String type,
            @PathVariable("target") String targetId,
            @RequestParam("role") long roleId,
            @RequestParam("user") String userId) {

        RoleAssignment assignment = new RoleAssignment();
        User user = rest.getUser(userId);
        if (user == null) {
            return ResponseEntity.status(422).body("No such user");
        }

        RoleType roleType = RoleType.valueOf(type.toUpperCase());
        switch (roleType) {
            case SCHOOL:
                assignment.setSchool(rest.getGroup(targetId));
                break;
            case VAULT:
                boolean hasVaultRole = rest.getRoleAssignmentsForUser(userId)
                        .stream()
                        .anyMatch(role -> {
                            Vault vault = role.getVault();
                            return vault != null && vault.getID().equals(targetId);
                        });

                if (hasVaultRole) {
                    return ResponseEntity.status(422).body("User already has a role in this vault");
                }

                assignment.setVault(rest.getVaultRecord(targetId));
                break;
            default:
                return ResponseEntity.notFound().build();
        }


        RoleModel role = rest.getRole(roleId)
                .orElseThrow(() -> new EntityNotFoundException(RoleModel.class, String.valueOf(roleId)));

        assignment.setUser(user);
        assignment.setRole(role);
        rest.createRoleAssignment(assignment);

        return ResponseEntity.ok().build();
    }


    public static class RoleAssignmentRequest {
        @NotNull
        Long role;

        @NotNull
        @NotEmpty
        String user;

        public Long getRole() {
            return role;
        }

        public void setRole(Long role) {
            this.role = role;
        }

        public String getUser() {
            return user;
        }

        public void setUser(String user) {
            this.user = user;
        }
    }
}
