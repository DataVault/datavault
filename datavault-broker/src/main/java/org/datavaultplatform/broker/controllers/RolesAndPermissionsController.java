package org.datavaultplatform.broker.controllers;

import static org.datavaultplatform.common.util.Constants.HEADER_CLIENT_KEY;
import static org.datavaultplatform.common.util.Constants.HEADER_USER_ID;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.datavaultplatform.common.email.EmailTemplate;
import org.datavaultplatform.broker.services.*;
import org.datavaultplatform.common.event.roles.CreateRoleAssignment;
import org.datavaultplatform.common.event.roles.DeleteRoleAssignment;
import org.datavaultplatform.common.event.roles.UpdateRoleAssignment;
import org.datavaultplatform.common.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;

@RestController
//@CrossOrigin
@RequestMapping("/permissions")
@Tag(name = "roles-and-permissions-controller", description = "Interact with DataVault Roles and Permissions")
public class RolesAndPermissionsController {

    public static final String EMAIL_ROLE = "role";
    public static final String EMAIL_HOMEPAGE = "homepage" ;
    public static final String EMAIL_HELP_PAGE = "helppage";
    public static final String EMAIL_ASSIGNEE = "assignee";
    public static final String EMAIL_VAULT = "vault";
    public static final String EMAIL_OWNER = "owner";
    public static final String EMAIL_TARGET = "target";
    public static final String EMAIL_SCHOOL = "school";
    private final EventService eventService;
    private final EmailService emailService;
    private final VaultsService vaultsService;
    private final GroupsService groupsService;
    private final UsersService usersService;
    private final String homePage;
    private final String helpPage;

    private final RolesAndPermissionsService rolesAndPermissionsService;
    private final ClientsService clientsService;

    @Autowired
    public RolesAndPermissionsController(EventService eventService, EmailService emailService,
        VaultsService vaultsService, GroupsService groupsService, UsersService usersService,
        @Value("${home.page}") String homePage,
        @Value("${help.roles}") String helpPage, RolesAndPermissionsService rolesAndPermissionsService,
        ClientsService clientsService) {
        this.eventService = eventService;
        this.emailService = emailService;
        this.vaultsService = vaultsService;
        this.groupsService = groupsService;
        this.usersService = usersService;
        this.homePage = homePage;
        this.helpPage = helpPage;
        this.rolesAndPermissionsService = rolesAndPermissionsService;
        this.clientsService = clientsService;
    }

    @Operation(
            summary = "Create a new DataVault Role",
            description = "Create a new DataVault Role"
    )
    @PostMapping(value = "/role", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public RoleModel createRole(@RequestBody RoleModel role) {
        return rolesAndPermissionsService.createRole(role);
    }

    @Operation(
            summary = "Create a new DataVault Role Assignment",
            description = "Create a new DataVault Role Assignment"
    )
    @PostMapping(value = "/roleAssignment", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public RoleAssignment createRoleAssignment(@RequestHeader(HEADER_USER_ID) String userId,
                                               @RequestHeader(HEADER_CLIENT_KEY) String clientKey,
                                               @RequestBody RoleAssignment roleAssignment) {
        sendEmails(EmailTemplate.NEW_ROLE_ASSIGNMENT, roleAssignment, userId);

        RoleAssignment assignment = rolesAndPermissionsService.createRoleAssignment(roleAssignment);

        CreateRoleAssignment roleAssignmentEvent = new CreateRoleAssignment(roleAssignment, userId);
        RoleType type = roleAssignment.getRole().getType();
        if (type == RoleType.VAULT){
            roleAssignmentEvent.setVault(vaultsService.getVault(roleAssignment.getVaultId()));
        } else if (type == RoleType.SCHOOL) {
            roleAssignmentEvent.setSchool(groupsService.getGroup(roleAssignment.getSchoolId()));
        }
        roleAssignmentEvent.setUser(usersService.getUser(userId));
        roleAssignmentEvent.setAgentType(Agent.AgentType.BROKER);
        roleAssignmentEvent.setAgent(clientsService.getClientByApiKey(clientKey).getName());
        roleAssignmentEvent.setAssignee(usersService.getUser(roleAssignment.getUserId()));
        roleAssignmentEvent.setRole(roleAssignment.getRole());

        eventService.addEvent(roleAssignmentEvent);

        return assignment;
    }

    @Operation(
            summary = "Get all school permissions",
            description = "Gets all school permissions"
    )
    @GetMapping(value = "/school", produces = MediaType.APPLICATION_JSON_VALUE)
    public PermissionModel[] getSchoolPermissions() {
        List<PermissionModel> schoolPermissions = rolesAndPermissionsService.getSchoolPermissions();
        return schoolPermissions.toArray(new PermissionModel[0]);
    }

    @Operation(
            summary = "Get all vault permissions",
            description = "Gets all vault permissions"
    )
    @GetMapping(value = "/vault", produces = MediaType.APPLICATION_JSON_VALUE)
    public PermissionModel[] getVaultPermissions() {
        List<PermissionModel> vaultPermissions = rolesAndPermissionsService.getVaultPermissions();
        return vaultPermissions.toArray(new PermissionModel[0]);
    }

    @Operation(
            summary = "Get a specific role by ID",
            description = "Gets a specific role by its ID"
    )
    @GetMapping(value = "/role/{roleId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public RoleModel getRole(@PathVariable Long roleId) {
        return rolesAndPermissionsService.getRole(roleId);
    }

    @Operation(
            summary = "Get the 'I.S. Admin' Role",
            description = "Gets the 'I.S. Admin' Role"
    )
    @GetMapping(value = "/role/isAdmin", produces = MediaType.APPLICATION_JSON_VALUE)
    public RoleModel getIsAdmin() {
        return rolesAndPermissionsService.getIsAdmin();
    }

    @Operation(
            summary = "Get all editable roles",
            description = "Gets all roles which can be edited"
    )
    @GetMapping(value = "/roles", produces = MediaType.APPLICATION_JSON_VALUE)
    public RoleModel[] getEditableRoles() {
        List<RoleModel> editableRoles = rolesAndPermissionsService.getEditableRoles();
        return editableRoles.toArray(new RoleModel[0]);
    }

    @Operation(
            summary = "Get all viewable (read-only) roles",
            description = "Gets all roles which can be viewed but not edited"
    )
    @GetMapping(value = "/roles/readOnly", produces = MediaType.APPLICATION_JSON_VALUE)
    public RoleModel[] getViewableRoles() {
        List<RoleModel> viewableRoles = rolesAndPermissionsService.getViewableRoles();
        return viewableRoles.toArray(new RoleModel[0]);
    }

    @Operation(
            summary = "Get all school roles",
            description = "Gets all school roles"
    )
    @GetMapping(value = "/roles/school", produces = MediaType.APPLICATION_JSON_VALUE)
    public RoleModel[] getAllSchoolRoles() {
        List<RoleModel> editableRoles = rolesAndPermissionsService.getSchoolRoles();
        return editableRoles.toArray(new RoleModel[0]);
    }

    @Operation(
            summary = "Get all vault roles",
            description = "Gets all vault roles"
    )
    @GetMapping(value = "/roles/vault", produces = MediaType.APPLICATION_JSON_VALUE)
    public RoleModel[] getAllVaultRoles() {
        List<RoleModel> editableRoles = rolesAndPermissionsService.getVaultRoles();
        return editableRoles.toArray(new RoleModel[0]);
    }

    @Operation(
            summary = "Get a specified role assignment by ID",
            description = "Gets a specified role assignment by its ID"
    )
    @GetMapping(value = "/roleAssignment/{assignmentId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public RoleAssignment getRoleAssignment(
            @PathVariable Long assignmentId) {
        return rolesAndPermissionsService.getRoleAssignment(assignmentId);
    }

    @Operation(
            summary = "Get all role assignments for a given school",
            description = "Gets all role assignments for a given school"
    )
    @GetMapping(value = "/roleAssignments/school/{schoolId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public RoleAssignment[] getRoleAssignmentsForSchool(
            @PathVariable String schoolId) {
        List<RoleAssignment> schoolRoleAssignments = rolesAndPermissionsService.getRoleAssignmentsForSchool(schoolId);
        return schoolRoleAssignments.toArray(new RoleAssignment[0]);
    }

    @Operation(
            summary = "Get all role assignments for a given vault",
            description = "Gets all role assignments for a given vault"
    )
    @GetMapping(value = "/roleAssignments/vault/{vaultId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public RoleAssignment[] getRoleAssignmentsForVault(
            @PathVariable String vaultId) {
        List<RoleAssignment> schoolRoleAssignments = rolesAndPermissionsService.getRoleAssignmentsForVault(vaultId);
        return schoolRoleAssignments.toArray(new RoleAssignment[0]);
    }

    @Operation(
            summary = "Get all role assignments for a given user",
            description = "Gets all role assignments for a given user"
    )
    @GetMapping(value = "/roleAssignments/user/{userId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public RoleAssignment[] getRoleAssignmentsForUser(
            @PathVariable String userId) {
        List<RoleAssignment> schoolRoleAssignments = rolesAndPermissionsService.getRoleAssignmentsForUser(userId);
        return schoolRoleAssignments.toArray(new RoleAssignment[0]);
    }

    @Operation(
            summary = "Get all role assignments for a given role",
            description = "Gets all role assignments for a given role"
    )
    @GetMapping(value = "/roleAssignments/role/{roleId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public RoleAssignment[] getRoleAssignmentsForRole(
            @PathVariable Long roleId) {
        List<RoleAssignment> roleAssignments = rolesAndPermissionsService.getRoleAssignmentsForRole(roleId);
        return roleAssignments.toArray(new RoleAssignment[0]);
    }

    @Operation(
            summary = "Update an existing role",
            description = "Updates an existing role"
    )
    @PutMapping(value = "/role", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public RoleModel updateRole(@RequestBody RoleModel role) {
        return rolesAndPermissionsService.updateRole(role);
    }

    @Operation(
            summary = "Update an existing DataVault Role Assignment",
            description = "Update an existing DataVault Role Assignment"
    )
    @PutMapping(value = "/roleAssignment", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public RoleAssignment updateRoleAssignment(@RequestHeader(HEADER_USER_ID) String userId,
                                               @RequestHeader(HEADER_CLIENT_KEY) String clientKey,
                                               @RequestBody RoleAssignment roleAssignment) {
        sendEmails(EmailTemplate.UPDATE_ROLE_ASSIGNMENT, roleAssignment, userId);

        UpdateRoleAssignment roleAssignmentEvent = new UpdateRoleAssignment(roleAssignment, userId);

        RoleType type = roleAssignment.getRole().getType();
        if (type == RoleType.VAULT){
            roleAssignmentEvent.setVault(vaultsService.getVault(roleAssignment.getVaultId()));
        } else if (type == RoleType.SCHOOL) {
            roleAssignmentEvent.setSchool(groupsService.getGroup(roleAssignment.getSchoolId()));
        }
        roleAssignmentEvent.setUser(usersService.getUser(userId));
        roleAssignmentEvent.setAgentType(Agent.AgentType.BROKER);
        roleAssignmentEvent.setAgent(clientsService.getClientByApiKey(clientKey).getName());
        roleAssignmentEvent.setAssignee(usersService.getUser(roleAssignment.getUserId()));
        roleAssignmentEvent.setRole(roleAssignment.getRole());

        eventService.addEvent(roleAssignmentEvent);

        RoleAssignment assignment = rolesAndPermissionsService.updateRoleAssignment(roleAssignment);

        return assignment;
    }

    @Operation(
            summary = "Delete a role by ID",
            description = "Deletes a role by its ID"
    )
    @DeleteMapping("/role/{roleId}")
    public ResponseEntity<Void> deleteRole(@PathVariable Long roleId) {
        rolesAndPermissionsService.deleteRole(roleId);
        return ResponseEntity.ok().build();
    }

    @Operation(
            summary = "Delete a DataVault Role Assignment",
            description = "Delete a DataVault Role Assignment"
    )
    @DeleteMapping("/roleAssignment/{roleAssignmentId}")
    public ResponseEntity<Void> deleteRoleAssignment(
             @RequestHeader(HEADER_USER_ID) String userId,
             @RequestHeader(HEADER_CLIENT_KEY) String clientKey,
            @PathVariable Long roleAssignmentId) {

        RoleAssignment assignment = rolesAndPermissionsService.getRoleAssignment(roleAssignmentId);

        sendEmails(EmailTemplate.DELETE_ROLE_ASSIGNMENT, assignment, userId);

        DeleteRoleAssignment roleAssignmentEvent = new DeleteRoleAssignment(assignment, userId);

        RoleType type = assignment.getRole().getType();
        if (type == RoleType.VAULT){
            roleAssignmentEvent.setVault(vaultsService.getVault(assignment.getVaultId()));
        } else if (type == RoleType.SCHOOL) {
            roleAssignmentEvent.setSchool(groupsService.getGroup(assignment.getSchoolId()));
        }
        roleAssignmentEvent.setUser(usersService.getUser(userId));
        roleAssignmentEvent.setAgentType(Agent.AgentType.BROKER);
        roleAssignmentEvent.setAgent(clientsService.getClientByApiKey(clientKey).getName());
        roleAssignmentEvent.setAssignee(usersService.getUser(assignment.getUserId()));
        roleAssignmentEvent.setRole(assignment.getRole());

        eventService.addEvent(roleAssignmentEvent);

        rolesAndPermissionsService.deleteRoleAssignment(roleAssignmentId);
        return ResponseEntity.ok().build();
    }

    private void sendEmails(String template, RoleAssignment roleAssignment, String userId) {

        HashMap<String, Object> model = new HashMap<>();
        model.put(EMAIL_ROLE, roleAssignment.getRole().getName());
        model.put(EMAIL_HOMEPAGE, this.homePage);
        model.put(EMAIL_HELP_PAGE, this.helpPage);
        User assignee = this.usersService.getUser(userId);
        model.put(EMAIL_ASSIGNEE, assignee.getFirstname() + " " + assignee.getLastname());
        if(roleAssignment.getVaultId() != null) {
            Vault vault = vaultsService.getVault(roleAssignment.getVaultId());
            model.put(EMAIL_VAULT, vault.getName());
            User vaultOwner = rolesAndPermissionsService.getVaultOwner(vault.getID());
            if(vaultOwner != null) {
                model.put(EMAIL_OWNER, vaultOwner.getFirstname() + " " + vaultOwner.getLastname());
            } else {
                model.put(EMAIL_OWNER, "None");
            }
            model.put(EMAIL_TARGET, "vault");
        }
        if(roleAssignment.getSchoolId() != null) {
            model.put(EMAIL_SCHOOL, groupsService.getGroup(roleAssignment.getSchoolId()).getName());
            model.put(EMAIL_TARGET, "school");
        }

        // Send email to the deposit user
        emailService.sendTemplateMailToUser(roleAssignment.getUserId(),
                "Datavault - Role Assignment",
                template,
                model);
    }
}