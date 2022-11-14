package org.datavaultplatform.broker.authentication;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.datavaultplatform.broker.controllers.RolesAndPermissionsController;
import org.datavaultplatform.common.model.PermissionModel;
import org.datavaultplatform.common.model.RoleAssignment;
import org.datavaultplatform.common.model.RoleModel;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

public class RolesAndPermissonsControllerAuthTest extends BaseControllerAuthTest {

  private static final RoleModel[] ROLE_MODEL_ARR = {AuthTestData.ROLE_MODEL};

  private static final RoleAssignment[] ROLE_ASSIGNMENT_ARR = {AuthTestData.ROLE_ASSIGNMENT};

  private static final PermissionModel[] PERMISSION_MODEL_ARR = {AuthTestData.PERMISSION_MODEL};

  @MockBean
  RolesAndPermissionsController controller;

  @Test
  void testPostCreateRole() throws JsonProcessingException {
    when(controller.createRole(AuthTestData.ROLE_MODEL)).thenReturn(AuthTestData.ROLE_MODEL);

    checkWorksWhenAuthenticatedFailsOtherwise(
        post("/permissions/role")
            .contentType(MediaType.APPLICATION_JSON)
            .content(mapper.writeValueAsString(AuthTestData.ROLE_MODEL)),
        AuthTestData.ROLE_MODEL);

    verify(controller).createRole(AuthTestData.ROLE_MODEL);
  }

  @Test
  void testPostCreateRoleAssignment() throws Exception {
    when(controller.createRoleAssignment(USER_ID_1, API_KEY_1,
        AuthTestData.ROLE_ASSIGNMENT)).thenReturn(AuthTestData.ROLE_ASSIGNMENT);

    checkWorksWhenAuthenticatedFailsOtherwise(
        post("/permissions/roleAssignment")
            .contentType(MediaType.APPLICATION_JSON)
            .content(mapper.writeValueAsString(AuthTestData.ROLE_ASSIGNMENT)),
        AuthTestData.ROLE_ASSIGNMENT);

    verify(controller).createRoleAssignment(USER_ID_1, API_KEY_1, AuthTestData.ROLE_ASSIGNMENT);
  }

  @Test
  void testDeleteRole() {
    when(controller.deleteRole(1234L)).thenReturn(ResponseEntity.ok().build());

    checkWorksWhenAuthenticatedFailsOtherwise(
        delete("/permissions/role/{roleId}", "1234"),
        null);
    verify(controller).deleteRole(1234L);
  }

  @Test
  void testDeleteRoleAssignment() {
    when(controller.deleteRoleAssignment(USER_ID_1, API_KEY_1, 1234L)).thenReturn(
        ResponseEntity.ok().build());

    checkWorksWhenAuthenticatedFailsOtherwise(
        delete("/permissions/roleAssignment/{roleAssignmentId}", "1234"),
        null);

    verify(controller).deleteRoleAssignment(USER_ID_1, API_KEY_1, 1234L);
  }

  @Test
  void testGetAllSchoolRoles() {
    when(controller.getAllSchoolRoles()).thenReturn(ROLE_MODEL_ARR);

    checkWorksWhenAuthenticatedFailsOtherwise(
        get("/permissions/roles/school"),
        ROLE_MODEL_ARR);

    verify(controller).getAllSchoolRoles();
  }

  @Test
  void testGetAllVaultRoles() {
    when(controller.getAllVaultRoles()).thenReturn(ROLE_MODEL_ARR);

    checkWorksWhenAuthenticatedFailsOtherwise(
        get("/permissions/roles/vault"),
        ROLE_MODEL_ARR);

    verify(controller).getAllVaultRoles();

  }

  @Test
  void testGetEditableRoles() {
    when(controller.getEditableRoles()).thenReturn(ROLE_MODEL_ARR);

    checkWorksWhenAuthenticatedFailsOtherwise(
        get("/permissions/roles"),
        ROLE_MODEL_ARR);

    verify(controller).getEditableRoles();
  }

  @Test
  void testGetIsAdmin() {
    when(controller.getIsAdmin()).thenReturn(AuthTestData.ROLE_MODEL);

    checkWorksWhenAuthenticatedFailsOtherwise(
        get("/permissions/role/isAdmin"),
        AuthTestData.ROLE_MODEL);

    verify(controller).getIsAdmin();
  }

  @Test
  void testGetRole() {
    when(controller.getRole(1234L)).thenReturn(AuthTestData.ROLE_MODEL);

    checkWorksWhenAuthenticatedFailsOtherwise(
        get("/permissions/role/{roleId}", "1234"),
        AuthTestData.ROLE_MODEL);

    verify(controller).getRole(1234L);
  }

  @Test
  void testGetRoleAssignment() {
    when(controller.getRoleAssignment(1234L)).thenReturn(AuthTestData.ROLE_ASSIGNMENT);

    checkWorksWhenAuthenticatedFailsOtherwise(
        get("/permissions/roleAssignment/{roleAssignmentId}", "1234"),
        AuthTestData.ROLE_ASSIGNMENT);

    verify(controller).getRoleAssignment(1234L);
  }

  @Test
  void testGetRoleAssignmentsForRole() {
    when(controller.getRoleAssignmentsForRole(1234L)).thenReturn(ROLE_ASSIGNMENT_ARR);

    checkWorksWhenAuthenticatedFailsOtherwise(
        get("/permissions/roleAssignments/role/{roleId}", "1234"),
        ROLE_ASSIGNMENT_ARR);

    verify(controller).getRoleAssignmentsForRole(1234L);
  }

  @Test
  void testGetRoleAssignmentForSchool() {
    when(controller.getRoleAssignmentsForSchool("school-id-1")).thenReturn(ROLE_ASSIGNMENT_ARR);

    checkWorksWhenAuthenticatedFailsOtherwise(
        get("/permissions/roleAssignments/school/{schoolId}", "school-id-1"),
        ROLE_ASSIGNMENT_ARR);

    verify(controller).getRoleAssignmentsForSchool("school-id-1");
  }

  @Test
  void testGetRoleAssignmentForUser() {
    when(controller.getRoleAssignmentsForUser("user-id-1")).thenReturn(ROLE_ASSIGNMENT_ARR);

    checkWorksWhenAuthenticatedFailsOtherwise(
        get("/permissions/roleAssignments/user/{userId}", "user-id-1"),
        ROLE_ASSIGNMENT_ARR);

    verify(controller).getRoleAssignmentsForUser("user-id-1");
  }

  @Test
  void testGetRoleAssignmentForVault() {
    when(controller.getRoleAssignmentsForVault("vault-id-1")).thenReturn(
        ROLE_ASSIGNMENT_ARR);

    checkWorksWhenAuthenticatedFailsOtherwise(
        get("/permissions/roleAssignments/vault/{vaultId}", "vault-id-1"),
        ROLE_ASSIGNMENT_ARR);

    verify(controller).getRoleAssignmentsForVault("vault-id-1");
  }

  @Test
  void testGetSchoolPermissions() {
    when(controller.getSchoolPermissions()).thenReturn(PERMISSION_MODEL_ARR);

    checkWorksWhenAuthenticatedFailsOtherwise(
        get("/permissions/school"),
        PERMISSION_MODEL_ARR);

    verify(controller).getSchoolPermissions();
  }

  @Test
  void testGetVaultPermissions() {
    when(controller.getVaultPermissions()).thenReturn(PERMISSION_MODEL_ARR);

    checkWorksWhenAuthenticatedFailsOtherwise(
        get("/permissions/vault"),
        PERMISSION_MODEL_ARR);

    verify(controller).getVaultPermissions();
  }

  @Test
  void testGetViewableRoles() {
    when(controller.getViewableRoles()).thenReturn(ROLE_MODEL_ARR);

    checkWorksWhenAuthenticatedFailsOtherwise(
        get("/permissions/roles/readOnly"),
        ROLE_MODEL_ARR);

    verify(controller).getViewableRoles();
  }

  @Test
  void testPutUpdateRole() throws JsonProcessingException {
    when(controller.updateRole(AuthTestData.ROLE_MODEL)).thenReturn(AuthTestData.ROLE_MODEL);

    checkWorksWhenAuthenticatedFailsOtherwise(
        put("/permissions/role")
            .content(mapper.writeValueAsString(AuthTestData.ROLE_MODEL))
            .contentType(MediaType.APPLICATION_JSON),
        AuthTestData.ROLE_MODEL);

    verify(controller).updateRole(AuthTestData.ROLE_MODEL);
  }

  @Test
  void testPutUpdateRoleAssignment() throws Exception {
    when(controller.updateRoleAssignment(USER_ID_1, API_KEY_1,
        AuthTestData.ROLE_ASSIGNMENT)).thenReturn(AuthTestData.ROLE_ASSIGNMENT);

    checkWorksWhenAuthenticatedFailsOtherwise(
        put("/permissions/roleAssignment")
            .content(mapper.writeValueAsString(AuthTestData.ROLE_ASSIGNMENT))
            .contentType(MediaType.APPLICATION_JSON),
        AuthTestData.ROLE_ASSIGNMENT);

    verify(controller).updateRoleAssignment(USER_ID_1, API_KEY_1, AuthTestData.ROLE_ASSIGNMENT);
  }

  @AfterEach
  void securityCheck() {
    checkHasSecurityUserAndClientUserRolesOnly();
  }
}
