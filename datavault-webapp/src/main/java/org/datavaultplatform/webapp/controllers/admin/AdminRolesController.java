package org.datavaultplatform.webapp.controllers.admin;

import com.google.common.collect.MoreCollectors;
import org.apache.commons.lang3.StringUtils;
import org.datavaultplatform.common.model.*;
import org.datavaultplatform.common.util.RoleUtils;
import org.datavaultplatform.webapp.exception.EntityNotFoundException;
import org.datavaultplatform.webapp.model.RoleViewModel;
import org.datavaultplatform.webapp.services.ForceLogoutService;
import org.datavaultplatform.webapp.services.RestService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import java.security.Principal;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Controller
@ConditionalOnBean(RestService.class)
public class AdminRolesController {

    private static final Logger logger = LoggerFactory.getLogger(AdminRolesController.class);

    private final RestService restService;

    private final ForceLogoutService forceLogoutService;

    @Autowired
    public AdminRolesController(RestService restService,
        ForceLogoutService forceLogoutService) {
        this.restService = restService;
        this.forceLogoutService = forceLogoutService;
    }

    @GetMapping("/admin/roles")
    public ModelAndView getRolesListing(Principal principal) {
        ModelAndView mav = new ModelAndView();
        mav.setViewName("admin/roles/index");
        mav.addObject("readOnlyRoles", restService.getViewableRoles());
        mav.addObject("roles", restService.getEditableRoles());
        mav.addObject("isSuperAdmin", isSuperAdmin(principal));
        mav.addObject("superAdminRole", restService.getIsAdmin());

        return mav;
    }

    @GetMapping("/admin/roles/isadmin")
    public ModelAndView getSuperAdminUsersListing(Principal principal) {

        RoleModel superAdminRole = restService.getIsAdmin();
        List<User> superAdminUsers = restService.getRoleAssignmentsForRole(superAdminRole.getId())
                .stream()
                .map(roleAssignment -> restService.getUser(roleAssignment.getUserId()))
                .collect(Collectors.toList());

        ModelAndView mav = new ModelAndView();
        mav.setViewName("admin/roles/isadmin/index");
        mav.addObject("users", superAdminUsers);
        mav.addObject("role", superAdminRole);
        return mav;
    }

    @PostMapping("/admin/roles/save")
    public ResponseEntity save(@RequestParam(value = "id") long id,
                               @RequestParam(value = "name") String name,
                               @RequestParam(value = "type") String type,
                               @RequestParam(value = "status") String status,
                               @RequestParam(value = "description", required = false) String description,
                               @RequestParam(value = "permissions", required = false) String[] permissions) {

        Optional<RoleModel> stored = restService.getRole(id);
        if (!stored.isPresent()) {
            logger.debug("No existing role was found with ID={} - preparing to store new role.", id);
            return createNewRole(name, type, description, permissions, status);
        }

        logger.debug("Existing role found with ID={} - preparing to persist update.", id);
        return updateRole(id, name, type, description, permissions, status);
    }

    private ResponseEntity createNewRole(String name,
                                         String type,
                                         String description,
                                         String[] permissions,
                                         String status ) {

        List<RoleModel> roles = restService.getEditableRoles();

        if (roles.stream().anyMatch(role -> name.equalsIgnoreCase(role.getName()))) {
            logger.debug("Cannot create a role with the same name as an existing role");
            return validationFailed("A role already exists with this name");
        }

        List<PermissionModel> selectedPermissions = getSelectedPermissions(type, permissions);

        if (StringUtils.isEmpty(name)) {
            logger.debug("Could not create role - no name provided");
            return validationFailed("Roles must have a name.");

        } else if (RoleUtils.isReservedRoleName(name)) {
            logger.debug("Could not create role - reserved name");
            return validationFailed("Cannot create role with reserved name " + name);

        } else if (StringUtils.isEmpty(description)) {
            logger.debug("Could not create role - no description provided");
            return validationFailed("Roles must have a description.");

        } else if (selectedPermissions.size() < 1) {
            logger.debug("Could not create role - no permissions selected");
            return validationFailed("Roles must have at least one permission.");
        } else if (RoleType.valueOf(type).equals(RoleType.VAULT) && StringUtils.isEmpty(status)) {
            logger.debug("Could not create vault role - no level.");
            return validationFailed("Vault roles must have a level.");
        } else if (! StringUtils.isEmpty(status)) {
            try {
                int statusAsInt = Integer.parseInt(status);
                if (statusAsInt < 0) {
                    logger.debug("Could not create role - negative level.");
                    return validationFailed("Roles must have a positive level.");
                }
            } catch (NumberFormatException nfe) {
                logger.debug("Could not create role - invalid level.");
                return validationFailed("Roles must have a valid level.");
            }

        }

        RoleModel role = new RoleModel();
        role.setPermissions(selectedPermissions);
        role.setName(name);
        role.setDescription(description);
        role.setType(RoleType.valueOf(type));
        role.setStatus(status);

        logger.info("Attempting to create new role with name {}", name);
        restService.createRole(role);
        return ResponseEntity.ok().build();
    }

    private List<PermissionModel> getSelectedPermissions(String type, String[] permIds) {
        if (permIds == null) {
            return new ArrayList<>();
        }
        Set<String> permissionIds = new HashSet<>(Arrays.asList(permIds));
        return getPermissionsByType(type).stream()
                .filter(permission -> permissionIds.contains(permission.getId()))
                .collect(Collectors.toList());
    }

    private List<PermissionModel> getPermissionsByType(String type) {
        switch (type) {
            case "VAULT":
                return restService.getVaultPermissions();
            case "SCHOOL":
                return restService.getSchoolPermissions();
            default:
                return new ArrayList<>();
        }
    }

    private ResponseEntity validationFailed(String message) {
        return ResponseEntity.status(422).body(message);
    }

    private ResponseEntity updateRole(long id,
                                      String name,
                                      String type,
                                      String description,
                                      String[] permissions,
                                      String status) {

        RoleModel role = restService.getRole(id)
                .orElseThrow(() -> new EntityNotFoundException(RoleModel.class, String.valueOf(id)));

        List<PermissionModel> selectedPermissions = getSelectedPermissions(type, permissions);
        List<RoleModel> roles = restService.getEditableRoles();

        Predicate<RoleModel> rolePredicate = otherRole -> name.equalsIgnoreCase(otherRole.getName())
                && otherRole.getId() != id;

        if (roles.stream().anyMatch(rolePredicate)) {
            logger.debug("Cannot create a role with the same name as an existing role");
            return validationFailed("A role already exists with this name");
        }

        if (!role.getType().isCustomCreatable()) {
            logger.debug("Could not update role - non-editable role type");
            return validationFailed("Cannot update non-editable roles.");

        } else if (StringUtils.isEmpty(name)) {
            logger.debug("Could not update role - no name provided");
            return validationFailed("Roles must have a name.");

        } else if (RoleUtils.isReservedRoleName(name)) {
            logger.debug("Could not create role - reserved name");
            return validationFailed("Cannot update role to have reserved name " + name);

        } else if (StringUtils.isEmpty(description)) {
            logger.debug("Could not update role - no description provided");
            return validationFailed("Roles must have a description.");

        } else if (selectedPermissions.size() < 1) {
            logger.debug("Could not update role - no permissions selected");
            return validationFailed("Roles must have at least one permission.");

        } else if (!role.getType().name().equals(type) && role.getAssignedUserCount() > 0) {
            logger.debug("Could not update role - attempted to change role type while users are still applied.");
            return validationFailed("Cannot change role type with users assigned to the role.");
        } else if (RoleType.valueOf(type).equals(RoleType.VAULT) && StringUtils.isEmpty(status)) {
            logger.debug("Could not create vault role - no status.");
            return validationFailed("Vault roles must have a status.");
        } else if (! StringUtils.isEmpty(status)) {
            try {
                int statusAsInt = Integer.parseInt(status);
                if (statusAsInt < 0) {
                    logger.debug("Could not create role - negative status.");
                    return validationFailed("Roles must have a positive status.");
                }
            } catch (NumberFormatException nfe) {
                logger.debug("Could not create role - invalid status.");
                return validationFailed("Roles must have a valid status.");
            }

        }

        boolean hasReducedPermissions = RoleUtils.hasReducedPermissions(role.getPermissions(), selectedPermissions);
        role.setPermissions(selectedPermissions);
        role.setName(name);
        role.setDescription(description);
        role.setType(RoleType.valueOf(type));
        role.setStatus(status);

        logger.info("Attempting to update role with ID={}", id);
        restService.updateRole(role);
        if (hasReducedPermissions) {
            forceLogoutService.logoutRole(role.getId());
        }
        return ResponseEntity.ok().build();
    }

    @PostMapping("/admin/roles/delete")
    public ResponseEntity deleteRole(@RequestParam("id") long roleId) {

        logger.debug("Preparing to delete role with ID={}", roleId);
        Optional<RoleModel> role = restService.getRole(roleId);
        if (!role.isPresent()) {
            logger.error("Could not delete role with ID={} - no such role found", roleId);
            return validationFailed("Role does not exist Role Id: " + roleId);

        } else if (role.get().getAssignedUserCount() > 0) {
            logger.debug("Could not delete role with ID={} - users are still assigned to the role", roleId);
            return validationFailed("A role that has users cannot be deleted.");
        }

        logger.info("Attempting to delete role with ID={}", roleId);
        restService.deleteRole(roleId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/admin/roles/op")
    public ResponseEntity addSuperAdmin(@RequestParam("op-id") String userId) {

        logger.debug("Preparing to add user ID={} to IS ADMIN role", userId);
        if (StringUtils.isEmpty(userId)) {
            logger.debug("Could not assign user to IS Admin role - no user ID provided");
            return validationFailed("Please specify a user.");
        }

        RoleModel superAdminRole = restService.getIsAdmin();
        User user = restService.getUser(userId);

        if (user == null) {
            logger.debug("Could not assign user to IS Admin role - user not found");
            return validationFailed("Could not find user with ID=" + userId);
        }

        if (restService.
                getRoleAssignmentsForUser(userId)
                .stream()
                .anyMatch(ra -> ra.getRole().equals(superAdminRole))) {
            logger.warn("Cannot add the user ID={} to IS Admin Role ID={} because they already have it.", userId, superAdminRole.getId());
            return validationFailed(user.getFirstname() + " " + user.getLastname() + " is already an IS Admin.");
        }

        logger.info("Attempting to add user ID={} to IS Admin role with ID={}", userId, superAdminRole.getId());

        RoleAssignment newRoleAssignment = new RoleAssignment();
        newRoleAssignment.setRole(superAdminRole);
        newRoleAssignment.setUserId(userId);
        restService.createRoleAssignment(newRoleAssignment);

        forceLogoutService.logoutUser(userId);

        return ResponseEntity.ok().build();
    }

    @PostMapping("/admin/roles/deop")
    public ResponseEntity deleteSuperAdmin(Principal principal, HttpServletRequest request, @RequestParam("deop-id") String userId) throws ServletException {

        logger.debug("Preparing to remove user ID={} from IS ADMIN role", userId);
        RoleModel superAdminRole = restService.getIsAdmin();

        if (restService.getRoleAssignmentsForRole(superAdminRole.getId()).size() <= 1) {
            logger.info("Rejected a request to remove the user ID={} from IS Admin role ID={} because there must be at least one IS Admin", userId, superAdminRole.getId());
            return validationFailed("Cannot remove the last IS Admin.");
        }

        Optional<RoleAssignment> roleAssignment = restService.
                getRoleAssignmentsForUser(userId)
                .stream()
                .filter(ra -> ra.getRole().equals(superAdminRole))
                .collect(MoreCollectors.toOptional());

        if (!roleAssignment.isPresent()) {
            logger.warn("Cannot remove the user ID={} from IS Admin Role ID={} because they don't have it.", userId, superAdminRole.getId());
            return validationFailed("User is not an IS Admin.");
        }

        logger.info("Attempting to remove user ID={} from IS Admin role with ID={}", userId, superAdminRole.getId());
        restService.deleteRoleAssignment(roleAssignment.get().getId());

        forceLogoutService.logoutUser(userId);

        return ResponseEntity.ok().build();
    }

    @GetMapping(value = "/admin/roles/{id}", produces = "application/json")
    public ResponseEntity<RoleViewModel> getRole(@PathVariable("id") long roleId) {

        Optional<RoleModel> role = restService.getRole(roleId);
        if (!role.isPresent()) {
            logger.error("Could not find role with ID={}", roleId);
            throw new EntityNotFoundException(RoleModel.class, String.valueOf(roleId));
        }

        List<PermissionModel> allPermissions = getPermissionsByType(role.get().getType().name());
        List<PermissionModel> unsetPermissions = allPermissions.stream()
                .filter(p -> !role.get().hasPermission(p.getPermission()))
                .collect(Collectors.toList());

        return ResponseEntity.ok(new RoleViewModel(role.get(), unsetPermissions));
    }

    @GetMapping(value = "/admin/roles/getvaultpermissions", produces = "application/json")
    public ResponseEntity<List<PermissionModel>> getAllVaultPermissions() {
        List<PermissionModel> permissions = restService.getVaultPermissions();
        return ResponseEntity.ok(permissions);
    }

    @GetMapping(value = "/admin/roles/getschoolpermissions", produces = "application/json")
    public ResponseEntity<List<PermissionModel>> getAllSchoolPermissions() {
        List<PermissionModel> permissions = restService.getSchoolPermissions();
        return ResponseEntity.ok(permissions);
    }

    private boolean isSuperAdmin(Principal principal) {
        if (principal == null) return false;

        // TODO: just pull from principal, when it starts having roles. For now: ask broker by name
        // superAdminRole /should/ never be null.
        RoleModel superAdminRole = restService.getIsAdmin();

        // Principal.getName() should give ID of the user, according to the existing code
        return restService
                .getRoleAssignmentsForUser(principal.getName())
                .stream()
                .map(RoleAssignment::getRole)
                .anyMatch(superAdminRole::equals);

    }
}
