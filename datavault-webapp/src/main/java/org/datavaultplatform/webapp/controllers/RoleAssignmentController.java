package org.datavaultplatform.webapp.controllers;

import org.datavaultplatform.common.model.RoleAssignment;
import org.datavaultplatform.common.model.RoleModel;
import org.datavaultplatform.common.model.RoleType;
import org.datavaultplatform.common.model.User;
import org.datavaultplatform.webapp.exception.EntityNotFoundException;
import org.datavaultplatform.webapp.exception.InvalidUunException;
import org.datavaultplatform.webapp.services.ForceLogoutService;
import org.datavaultplatform.webapp.services.RestService;
import org.datavaultplatform.webapp.services.UserLookupService;
import org.hibernate.validator.constraints.NotEmpty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

@Controller
@ConditionalOnBean(RestService.class)
public class RoleAssignmentController {

    private final RestService rest;

    private final ForceLogoutService forceLogoutService;

    private final UserLookupService userLookupService;

    @Autowired
    public RoleAssignmentController(RestService rest, ForceLogoutService forceLogoutService, UserLookupService userLookupService) {
        this.rest = rest;
        this.forceLogoutService = forceLogoutService;
        this.userLookupService = userLookupService;
    }

    @PostMapping("/security/roles/{roleType}/{target}/user/update")
    @PreAuthorize("hasPermission(#targetId, #type, 'ASSIGN_VAULT_ROLES') or hasPermission(#targetId, 'GROUP_VAULT', 'ASSIGN_SCHOOL_VAULT_ROLES')")
    public ResponseEntity updateRoleAssignment(
            @PathVariable("roleType") String type,
            @PathVariable("target") String targetId,
            @Valid @NotNull @RequestParam("assignment") Long assignmentId,
            @Valid @NotNull(message = "Please specify a role") @RequestParam("role") Long roleId) {

        RoleAssignment roleAssignment = rest.getRoleAssignment(assignmentId)
                .orElseThrow(() -> new EntityNotFoundException(RoleAssignment.class, String.valueOf(assignmentId)));

        String existingTargetId = roleAssignment.getTargetId();

        if (!existingTargetId.equals(targetId)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        RoleModel role = rest.getRole(roleId).orElseThrow
                (()  -> new EntityNotFoundException(RoleModel.class, String.valueOf(roleId)));

        roleAssignment.setRole(role);
        rest.updateRoleAssignment(roleAssignment);

        forceLogoutService.logoutUser(roleAssignment.getUserId());

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

        forceLogoutService.logoutUser(assignment.getUserId());

        return ResponseEntity.ok().build();
    }

    @PreAuthorize("hasPermission(#targetId, #type, 'ASSIGN_VAULT_ROLES') or hasPermission(#targetId, 'GROUP_VAULT', 'ASSIGN_SCHOOL_VAULT_ROLES')")
    @PostMapping("/security/roles/{roleType}/{target}/user")
    public ResponseEntity createRoleAssignment(
            @PathVariable("roleType") String type,
            @PathVariable("target") String targetId,
            @Valid  RoleAssignmentRequest request) {

        RoleAssignment assignment = new RoleAssignment();
        User user = rest.getUser(request.user);
        if (user == null) {
            try {
                userLookupService.ensureUserExists(request.user);
            } catch (InvalidUunException e) {
                return ResponseEntity.status(422).body("Could not find user with UUN: " + request.user);
            }
        }

        RoleType roleType = RoleType.valueOf(type.toUpperCase());
        switch (roleType) {
            case SCHOOL:
                assignment.setSchoolId(targetId);
                break;
            case VAULT:
                boolean hasVaultRole = rest.getRoleAssignmentsForUser(request.user)
                        .stream()
                        .anyMatch(role -> role.getVaultId() != null && role.getVaultId().equals(targetId));

                if (hasVaultRole) {
                    return ResponseEntity.status(422).body("User already has a role in this vault");
                }

                assignment.setVaultId(targetId);
                break;
            default:
                return ResponseEntity.notFound().build();
        }

        RoleModel role = rest.getRole(request.role)
                .orElseThrow(() -> new EntityNotFoundException(RoleModel.class, String.valueOf(request.role)));

        assignment.setUserId(request.user);
        assignment.setRole(role);
        rest.createRoleAssignment(assignment);

        forceLogoutService.logoutUser(request.user);

        return ResponseEntity.ok().build();
    }


    public static class RoleAssignmentRequest {
        @NotNull(message = "Please specify a role")
        Long role;

        @NotNull
        @NotEmpty(message = "Please specify a user")
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
