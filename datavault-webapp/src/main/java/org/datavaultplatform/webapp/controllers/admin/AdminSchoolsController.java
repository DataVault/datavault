package org.datavaultplatform.webapp.controllers.admin;

import org.apache.commons.lang.StringUtils;
import org.datavaultplatform.common.model.*;
import org.datavaultplatform.webapp.exception.EntityNotFoundException;
import org.datavaultplatform.webapp.exception.InvalidUunException;
import org.datavaultplatform.webapp.services.RestService;
import org.datavaultplatform.webapp.services.UserLookupService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;
import java.util.Optional;

@Controller
public class AdminSchoolsController {

    private static final Logger logger = LoggerFactory.getLogger(AdminSchoolsController.class);

    private RestService restService;

    private UserLookupService userLookupService;

    public void setRestService(RestService restService) {
        this.restService = restService;
    }

    public void setUserLookupService(UserLookupService userLookupService) {
        this.userLookupService = userLookupService;
    }

    @GetMapping("/admin/schools")
    public ModelAndView getSchoolsListingPage() {
        ModelAndView mav = new ModelAndView();
        mav.setViewName("admin/schools/index");
        mav.addObject("schools", restService.getGroups());
        return mav;
    }

    @GetMapping("/admin/schools/{school}")
    public ModelAndView getSchoolRoleAssignmentsPage(@PathVariable("school") String schoolId) {

        Optional<Group> school = getGroup(schoolId);
        if (!school.isPresent()) {
            logger.error("Attempted to access school role management page for group {} but no such group was found",
                    schoolId);
            throw new EntityNotFoundException(Group.class, schoolId);
        }

        ModelAndView mav = new ModelAndView();
        mav.setViewName("admin/schools/schoolRoles");
        mav.addObject("school", school.get());
        mav.addObject("roles", getRoles());
        mav.addObject("roleAssignments", getRoleAssignments(school.get()));
        return mav;
    }

    @PostMapping("/admin/schools/{school}/user")
    public ResponseEntity addNewRoleAssignment(@PathVariable("school") String schoolId,
                                               @RequestParam("user") String userId,
                                               @RequestParam("role") Long roleId) {

        if (StringUtils.isEmpty(userId)) {
            logger.debug("Attempted to add a role assignment without specifying the user");
            return validationFailed("Please specify a user.");

        } else if (roleId == null) {
            logger.debug("Attempted to add a role assignment without specifying the role");
            return validationFailed("Please specify a role.");
        }

        Optional<Group> school = getGroup(schoolId);
        Optional<User> user = getUser(userId);
        Optional<RoleModel> role = getRole(roleId);

        if (!school.isPresent()) {
            logger.error("Attempted to add a new school role assignment to group {} but no such group was found",
                    schoolId);
            throw new EntityNotFoundException(Group.class, schoolId);

        } else if (!role.isPresent()) {
            logger.error("Attempted to add a new school role assignment with role {} but no such role was found",
                    roleId);
            throw new EntityNotFoundException(RoleModel.class, String.valueOf(roleId));

        } else if (RoleType.SCHOOL != role.get().getType()) {
            logger.debug("Attempted to add a new role assignment with role {} as a school role assignment but was not a school role",
                    roleId);
            return validationFailed("Role " + role.get().getName() + " is not a school role");

        } else if (!user.isPresent()) {
            logger.debug("Attempted to add a new school role assignment to user {} but no such user was found", userId);
            return validationFailed("Could not find user with ID=" + userId);
        }

        List<RoleAssignment> userRoleAssignments = restService.getRoleAssignmentsForUser(userId);
        if (userRoleAssignments.stream().anyMatch(r -> r.getSchool() != null && r.getSchool().getID().equals(schoolId))) {
            logger.debug("Attempted to add a new school role assignment for user {} and school {} when one already exists",
                    userId,
                    schoolId);
            return validationFailed("User " + userId + " already has a role assigned to them.");

        } else if (userRoleAssignments.stream().anyMatch(r -> r.getRole().getId().equals(restService.getIsAdmin().getId()))) {
            logger.debug("Attempted to add a school role to an IS Admin.");
            return validationFailed("User " + userId + " is an IS Admin with full global permissions.");
        }

        logger.info("Adding new school role assignment: [userId={},groupId={},roleId={}]",
                userId,
                schoolId,
                roleId);
        RoleAssignment newRoleAssignment = new RoleAssignment();
        newRoleAssignment.setRole(role.get());
        newRoleAssignment.setUser(user.get());
        newRoleAssignment.setSchool(school.get());
        createNewRoleAssignment(newRoleAssignment);

        return ResponseEntity.ok().build();
    }

    private ResponseEntity validationFailed(String message) {
        return ResponseEntity.status(422).body(message);
    }

    @PostMapping("/admin/schools/{school}/user/update")
    public ResponseEntity updateExistingRoleAssignment(@PathVariable("school") String schoolId,
                                               @RequestParam("assignment") Long assignmentId,
                                               @RequestParam("role") Long roleId) {

        if (assignmentId == null) {
            logger.debug("Attempted to update a role assignment without specifying the ID");
            return validationFailed("Please specify a user.");

        } else if (roleId == null) {
            logger.debug("Attempted to update a role assignment without specifying the role");
            return validationFailed("Please specify a role.");
        }

        Optional<RoleModel> role = getRole(roleId);
        Optional<RoleAssignment> userRoleAssignment = getRoleAssignment(assignmentId);

        if (!role.isPresent()) {
            logger.error("Attempted to update a school role assignment to role {} but no such role was found",
                    roleId);
            throw new EntityNotFoundException(RoleModel.class, String.valueOf(roleId));

        } else if (RoleType.SCHOOL != role.get().getType()) {
            logger.debug("Attempted to add a new role assignment with role {} as a school role assignment but was not a school role",
                    roleId);
            return validationFailed("Role " + role.get().getName() + " is not a school role");

        } else if (!userRoleAssignment.isPresent()) {
            logger.error("Attempted to update a school role assignment [userId={},groupId={}] but no such role assignment was found",
                    assignmentId,
                    schoolId);
            throw new EntityNotFoundException(RoleAssignment.class, String.valueOf(assignmentId));
        }

        logger.info("Updating school role assignment. New role assignment: [ID={},groupId={},roleId={}]",
                assignmentId,
                schoolId,
                roleId);
        updateRoleAssignment(userRoleAssignment.get(), role.get());

        return ResponseEntity.ok().build();
    }

    @PostMapping("/admin/schools/{school}/user/delete")
    public ResponseEntity deleteRoleAssignment(@PathVariable("school") String schoolId,
                                       @RequestParam("assignment") Long assignmentId) {

        if (assignmentId == null) {
            logger.debug("Attempted to delete a role assignment without specifying the ID");
            return validationFailed("Please specify a user.");
        }

        Optional<RoleAssignment> userRoleAssignment = getRoleAssignment(assignmentId);
        if (!userRoleAssignment.isPresent()) {
            logger.error("Attempted to delete a school role assignment {} but no such role assignment was found", assignmentId);
            throw new EntityNotFoundException(RoleAssignment.class, String.valueOf(assignmentId));

        } else if (userRoleAssignment.get().getSchool() == null
                || !userRoleAssignment.get().getSchool().getID().equals(schoolId)) {
            logger.debug("Attempted to delete role assignment that was not assigned to school {}.", schoolId);
            return validationFailed("Role assignment belongs to a different school");
        }

        logger.info("Deleting school role assignment {}", assignmentId);
        deleteRoleAssignment(userRoleAssignment.get());

        return ResponseEntity.ok().build();
    }

    private List<RoleModel> getRoles() {
        return restService.getSchoolRoles();
    }

    private List<RoleAssignment> getRoleAssignments(Group school) {
        return restService.getRoleAssignmentsForSchool(school.getID());
    }

    private Optional<Group> getGroup(String groupId) {
        return Optional.ofNullable(restService.getGroup(groupId));
    }

    private Optional<User> getUser(String userId) {
        try {
            return Optional.of(userLookupService.ensureUserExists(userId));
        } catch (InvalidUunException e) {
            logger.error(e.getMessage(), e);
            // TODO this is probably the wrong thing to do here in an environment with access to the UoE LDAP server
            //  but without this fallback it would be impossible to build the roles & permissions framework in an
            //  environment without LDAP access. Additionally, this method wraps the returned user in an Optional solely
            //  to validate that the user exists in the DB - this is unnecessary with LDAP access as the lookup service
            //  persists any LDAP users that aren't yet present in the DB. In the event that someone tries to assign a
            //  role to a user who doesn't exist in LDAP an InvalidUunException will be thrown so the calling code
            //  should be refactored to add the user feedback on the exception rather than when this Optional
            //  is not present.
            return Optional.ofNullable(restService.getUser(userId));
        }
    }

    private Optional<RoleModel> getRole(long roleId) {
        return restService.getRole(roleId);
    }

    private void createNewRoleAssignment(RoleAssignment toCreate) {
        restService.createRoleAssignment(toCreate);
    }

    private Optional<RoleAssignment> getRoleAssignment(long assignmentId) {
        return restService.getRoleAssignment(assignmentId);
    }

    private void updateRoleAssignment(RoleAssignment originalRoleAssignment, RoleModel newRole) {
        originalRoleAssignment.setRole(newRole);
        restService.updateRoleAssignment(originalRoleAssignment);
    }

    private void deleteRoleAssignment(RoleAssignment toDelete) {
        restService.deleteRoleAssignment(toDelete.getId());
    }
}
