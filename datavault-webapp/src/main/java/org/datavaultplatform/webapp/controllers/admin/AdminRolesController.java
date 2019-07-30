package org.datavaultplatform.webapp.controllers.admin;

import org.apache.commons.lang.StringUtils;
import org.datavaultplatform.common.model.PermissionModel;
import org.datavaultplatform.common.model.RoleModel;
import org.datavaultplatform.common.model.RoleType;
import org.datavaultplatform.common.util.RoleUtils;
import org.datavaultplatform.webapp.exception.EntityNotFoundException;
import org.datavaultplatform.webapp.services.RestService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.datavaultplatform.webapp.model.RoleViewModel;
import org.testng.collections.Sets;

import java.util.*;
import java.util.stream.Collectors;

@Controller
public class AdminRolesController {

    private static final Logger logger = LoggerFactory.getLogger(AdminRolesController.class);

    private RestService restService;

    public void setRestService(RestService restService) {
        this.restService = restService;
    }

    @GetMapping("/admin/roles")
    public ModelAndView getRolesListing() {
        ModelAndView mav = new ModelAndView();
        mav.setViewName("admin/roles/index");
        mav.addObject("readOnlyRoles", restService.getViewableRoles());
        mav.addObject("roles", restService.getEditableRoles());
        return mav;
    }

    @PostMapping("/admin/roles/save")
    public ResponseEntity save(@RequestParam(value = "id") long id,
                       @RequestParam(value = "name") String name,
                       @RequestParam(value = "type") String type,
                       @RequestParam(value = "description", required = false) String description,
                       @RequestParam(value = "permissions", required = false) String[] permissions) {

        Optional<RoleModel> stored = restService.getRole(id);
        if (!stored.isPresent()) {
            logger.debug("No existing role was found with ID={} - preparing to store new role.", id);
            return createNewRole(name, type, description, permissions);
        }

        logger.debug("Existing role found with ID={} - preparing to persist update.", id);
        return updateRole(id, name, type, description, permissions);
    }

    private ResponseEntity createNewRole(String name,
                               String type,
                               String description,
                               String[] permissions) {

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
        }

        RoleModel role = new RoleModel();
        role.setPermissions(selectedPermissions);
        role.setName(name);
        role.setDescription(description);
        role.setType(RoleType.valueOf(type));

        logger.info("Attempting to create new role with name {}", name);
        restService.createRole(role);
        return ResponseEntity.ok().build();
    }

    private List<PermissionModel> getSelectedPermissions(String type, String[] permIds) {
        if (permIds == null) {
            return new ArrayList<>();
        }
        Set<String> permissionIds = Sets.newHashSet(Arrays.asList(permIds));
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
                            String[] permissions) {

        RoleModel role = restService.getRole(id)
                .orElseThrow(() -> new EntityNotFoundException(RoleModel.class, String.valueOf(id)));

        List<PermissionModel> selectedPermissions = getSelectedPermissions(type, permissions);

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
        }

        role.setPermissions(selectedPermissions);
        role.setName(name);
        role.setDescription(description);
        role.setType(RoleType.valueOf(type));

        logger.info("Attempting to update role with ID={}", id);
        restService.updateRole(role);
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
            return validationFailed("Can't delete a role that has users");
        }

        logger.info("Attempting to delete role with ID={}", roleId);
        restService.deleteRole(roleId);
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
}
