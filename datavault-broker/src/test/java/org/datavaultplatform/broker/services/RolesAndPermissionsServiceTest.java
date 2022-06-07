package org.datavaultplatform.broker.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import org.datavaultplatform.broker.test.TestUtils;
import org.datavaultplatform.common.model.Group;
import org.datavaultplatform.common.model.Permission;
import org.datavaultplatform.common.model.PermissionModel;
import org.datavaultplatform.common.model.PermissionModel.PermissionType;
import org.datavaultplatform.common.model.RoleAssignment;
import org.datavaultplatform.common.model.RoleModel;
import org.datavaultplatform.common.model.RoleType;
import org.datavaultplatform.common.model.dao.PermissionDAO;
import org.datavaultplatform.common.model.dao.RoleAssignmentDAO;
import org.datavaultplatform.common.model.dao.RoleDAO;
import org.junit.function.ThrowingRunnable;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class RolesAndPermissionsServiceTest {


    @Mock
    private RoleDAO mockRoleDao;

    @Mock
    private PermissionDAO mockPermissionDao;

    @Mock
    private RoleAssignmentDAO mockRoleAssignmentDao;

    @Mock
    private UsersService mockUsersService;

    @InjectMocks
    private RolesAndPermissionsService underTest;

    void checkIllegalStateExcepton(String message, ThrowingRunnable runnable){
        TestUtils.checkException(IllegalStateException.class, message, runnable);
    }

    void verifyNoMoreMockInteractions() {
        verifyNoMoreInteractions(mockRoleAssignmentDao, mockPermissionDao, mockPermissionDao, mockUsersService);
    }

    @Test
    public void createRoleShouldStoreANewRole() {
        // Given
        RoleModel toCreate = aRole(RoleType.VAULT);
        toCreate.addPermission(aPermission(Permission.VIEW_VAULT_ROLES, PermissionModel.PermissionType.VAULT));

        // When
        underTest.createRole(toCreate);

        // Then
        verify(mockRoleDao).save(toCreate);
    }

    @Test
    public void createRoleShouldThrowExceptionWhenRoleIdAlreadyExists() {
        // Given
        checkIllegalStateExcepton("Cannot create a role that already exists", () -> {

        RoleModel originalRole = aRole(RoleType.SCHOOL);
        originalRole.setId(123L);
        given(mockRoleDao.findById(123L)).willReturn(Optional.of(originalRole));

        RoleModel toCreate = aRole(RoleType.SCHOOL);
        toCreate.setId(123L);

        // When
        underTest.createRole(toCreate);

        // Then throws IllegalStateException
        });
    }

    @Test
    public void createRoleShouldThrowExceptionWhenSchoolPermissionIsAppliedToVaultRole() {
        checkIllegalStateExcepton("Unable to apply permission with type SCHOOL to role of type VAULT", () -> {

        RoleModel toCreate = aRole(RoleType.VAULT);
        toCreate.addPermission(aPermission(Permission.CAN_VIEW_RETRIEVES, PermissionModel.PermissionType.SCHOOL));

        // When
        underTest.createRole(toCreate);

        // Then throws IllegalStateException
        });
    }

    @Test
    public void createRoleShouldThrowExceptionWhenVaultPermissionIsAppliedToSchoolRole() {
        // Given
        checkIllegalStateExcepton("Unable to apply permission with type VAULT to role of type SCHOOL", () -> {

        RoleModel toCreate = aRole(RoleType.SCHOOL);
        toCreate.addPermission(aPermission(Permission.VIEW_VAULT_ROLES, PermissionModel.PermissionType.VAULT));

        // When
        underTest.createRole(toCreate);

        // Then throws IllegalStateException
        });
    }

    @Test
    public void createRoleAssignmentShouldStoreANewSchoolRoleAssignment() {
        // Given
        RoleAssignment toCreate = aRoleAssignment(aRole(RoleType.SCHOOL));
        toCreate.setSchoolId("school1");
        given(mockRoleAssignmentDao.roleAssignmentExists(toCreate)).willReturn(false);

        // When
        underTest.createRoleAssignment(toCreate);

        // Then
        verify(mockRoleAssignmentDao).save(toCreate);
    }

    @Test
    public void createRoleAssignmentShouldStoreANewVaultRoleAssignment() {
        // Given
        RoleAssignment toCreate = aRoleAssignment(aRole(RoleType.VAULT));
        toCreate.setVaultId("vault1");
        given(mockRoleAssignmentDao.roleAssignmentExists(toCreate)).willReturn(false);

        // When
        underTest.createRoleAssignment(toCreate);

        // Then
        verify(mockRoleAssignmentDao).save(toCreate);
    }

    @Test
    public void createRoleAssignmentShouldThrowExceptionWhenNoUserIsPresent() {
        // Given
        checkIllegalStateExcepton("Cannot create role assignment without user", () -> {

        RoleAssignment toCreate = aRoleAssignment(aRole(RoleType.SCHOOL));
        toCreate.setUserId(null);
        toCreate.setSchoolId("school1");

        // When
        underTest.createRoleAssignment(toCreate);

        // Then throws IllegalStateException
        });
    }

    @Test
    public void createRoleAssignmentShouldThrowExceptionWhenNoRoleIsPresent() {
        // Given
        checkIllegalStateExcepton("Cannot create role assignment without role", () -> {

        RoleAssignment toCreate = aRoleAssignment(null);

        // When
        underTest.createRoleAssignment(toCreate);

        // Then throws IllegalStateException
        });
    }

    @Test
    public void createRoleAssignmentShouldThrowExceptionWhenSchoolRoleAssignmentHasNoSchool() {
        // Given
        checkIllegalStateExcepton("Cannot create school role assignment without a school", () -> {

        RoleAssignment toCreate = aRoleAssignment(aRole(RoleType.SCHOOL));
        toCreate.setSchoolId(null);

        // When
        underTest.createRoleAssignment(toCreate);
        });
    }

    @Test
    public void createRoleAssignmentShouldThrowExceptionWhenVaultRoleAssignmentHasNoVault() {
        // Given
        checkIllegalStateExcepton("Cannot create vault role assignment without a vault", () -> {

        RoleAssignment toCreate = aRoleAssignment(aRole(RoleType.VAULT));
        toCreate.setVaultId(null);

        // When
        underTest.createRoleAssignment(toCreate);

        // Then throws IllegalStateException
        });
    }

    @Test
    public void createRoleAssignmentShouldhrowExceptionWhenRoleAssignmentAlreadyExists() {
        // Given
        checkIllegalStateExcepton("Role assignment already exists", () -> {

        RoleAssignment toCreate = aRoleAssignment(aRole(RoleType.SCHOOL));
        toCreate.setSchoolId("school1");
        given(mockRoleAssignmentDao.roleAssignmentExists(toCreate)).willReturn(true);

        // When
        underTest.createRoleAssignment(toCreate);

        // Then throws IllegalStateException
        });
    }

    @Test
    public void getSchoolPermissionsShouldReturnAllPermissionsApplicableToSchoolRoles() {
        // Given
        PermissionModel school1 = aPermission(Permission.CAN_VIEW_VAULTS_SIZE, PermissionModel.PermissionType.SCHOOL);
        PermissionModel school2 = aPermission(Permission.CAN_VIEW_IN_PROGRESS, PermissionModel.PermissionType.SCHOOL);
        PermissionModel school3 = aPermission(Permission.CAN_VIEW_QUEUES, PermissionModel.PermissionType.SCHOOL);

        List<PermissionModel> permissionModels = Arrays.asList(
                school1,
                school2,
                school3);

        given(mockPermissionDao.findByType(PermissionType.SCHOOL)).willReturn(permissionModels);
        // When
        Collection<PermissionModel> actual = underTest.getSchoolPermissions();

        // Then
        assertEquals(permissionModels, actual);
        verify(mockPermissionDao).findByType(PermissionType.SCHOOL);
        verifyNoMoreMockInteractions();
    }

    @Test
    public void getVaultPermissionsShouldReturnAllPermissionsApplicableToVaultRoles() {
        // Given
        PermissionModel vault1 = aPermission(Permission.VIEW_VAULT_ROLES, PermissionModel.PermissionType.VAULT);
        PermissionModel vault2 = aPermission(Permission.VIEW_VAULT_ROLES, PermissionModel.PermissionType.VAULT);
        PermissionModel vault3 = aPermission(Permission.VIEW_VAULT_ROLES, PermissionModel.PermissionType.VAULT);

        List<PermissionModel> allPermissions = Arrays.asList(
                vault1,
                vault2,
                vault3);

        given(mockPermissionDao.findByType(PermissionType.VAULT)).willReturn(allPermissions);

        // When
        Collection<PermissionModel> actual = underTest.getVaultPermissions();

        // Then
        assertEquals(allPermissions, actual);
        verify(mockPermissionDao).findByType(PermissionType.VAULT);
        verifyNoMoreMockInteractions();
    }

    @Test
    public void getEditableRolesShouldReturnAllCustomRoles() {
        // Given
        RoleModel schoolRole = aRole(RoleType.SCHOOL);
        RoleModel vaultRole = aRole(RoleType.VAULT);
        RoleModel adminRole = aRole(RoleType.ADMIN);

        given(mockRoleDao.findAllEditableRoles()).willReturn(Arrays.asList(schoolRole, vaultRole, adminRole));

        // When
        List<RoleModel> actual = underTest.getEditableRoles();

        // Then
        List<RoleModel> expected = Arrays.asList(schoolRole, vaultRole, adminRole);
        assertEquals(expected, actual);
        verify(mockRoleDao).findAllEditableRoles();
        verifyNoMoreMockInteractions();
    }

    @Test
    public void updateRoleShouldPersistChangesToRole() {
        // Given
        RoleModel originalRole = aRole(RoleType.SCHOOL);
        originalRole.setId(123L);
        given(mockRoleDao.findById(123L)).willReturn(Optional.of(originalRole));

        RoleModel toUpdate = aRole(RoleType.SCHOOL);
        toUpdate.setId(123L);
        toUpdate.addPermission(aPermission(Permission.CAN_VIEW_RETRIEVES, PermissionModel.PermissionType.SCHOOL));

        // When
        underTest.updateRole(toUpdate);

        // Then
        verify(mockRoleDao).update(toUpdate);
    }

    @Test
    public void updateRoleShouldThrowExceptionWhenRoleDoesNotHaveAnId() {
        // Given
        checkIllegalStateExcepton("Cannot update a role that does not exist", () -> {

        RoleModel toUpdate = aRole(RoleType.SCHOOL);
        toUpdate.setId(null);
        toUpdate.addPermission(aPermission(Permission.CAN_VIEW_RETRIEVES, PermissionModel.PermissionType.SCHOOL));

        // When
        underTest.updateRole(toUpdate);

        // Then throws IllegalStateException
        });
    }

    @Test
    public void updateRoleShouldThrowExceptionWhenRoleDoesNotExist() {
        // Given
        checkIllegalStateExcepton("Cannot update a role that does not exist", () -> {

        given(mockRoleDao.findById(123L)).willReturn(Optional.empty());

        RoleModel toUpdate = aRole(RoleType.SCHOOL);
        toUpdate.setId(123L);
        toUpdate.addPermission(aPermission(Permission.CAN_VIEW_RETRIEVES, PermissionModel.PermissionType.SCHOOL));

        // When
        underTest.updateRole(toUpdate);

        // Then
        verify(mockRoleDao).update(toUpdate);
        });
    }

    @Test
    public void updateRoleShouldThrowExceptionWhenUnsupportedPermissionTypeIsPresent() {
        // Given
        checkIllegalStateExcepton("Unable to apply permission with type VAULT to role of type SCHOOL", () -> {

        RoleModel originalRole = aRole(RoleType.SCHOOL);
        originalRole.setId(123L);
        given(mockRoleDao.findById(123L)).willReturn(Optional.of(originalRole));

        RoleModel toUpdate = aRole(RoleType.SCHOOL);
        toUpdate.setId(123L);
        toUpdate.addPermission(aPermission(Permission.VIEW_VAULT_ROLES, PermissionModel.PermissionType.VAULT));

        // When
        underTest.updateRole(toUpdate);

        // Then throws IllegalStateException
        });
    }

    @Test
    public void updateRoleAssignmentShouldPersistAnUpdate() {
        // Given
        Group school = new Group();
        String schoolId1 = "school1";
        String schoolId2 = "school2";

        RoleModel originalRole = aRole(RoleType.SCHOOL);
        RoleAssignment original = aRoleAssignment(originalRole);
        original.setSchoolId(schoolId1);
        original.setId(1L);

        given(mockRoleAssignmentDao.findById(1L)).willReturn(Optional.of(original));

        RoleModel newRole = aRole(RoleType.SCHOOL);

        RoleAssignment updatedRoleAssignment = aRoleAssignment(newRole);
        updatedRoleAssignment.setSchoolId(schoolId2);
        updatedRoleAssignment.setId(1L);

        given(mockRoleAssignmentDao.roleAssignmentExists(updatedRoleAssignment)).willReturn(false);

        // When
        underTest.updateRoleAssignment(updatedRoleAssignment);

        // Then
        verify(mockRoleAssignmentDao).findById(1L);
        verify(mockRoleAssignmentDao).update(updatedRoleAssignment);
        verify(mockRoleAssignmentDao).roleAssignmentExists(updatedRoleAssignment);
        verifyNoMoreMockInteractions();
    }

    @Test
    public void updateRoleAssignmentShouldThrowExceptionWhenOriginalRoleDoesNotExist() {
        // Given
        checkIllegalStateExcepton("Cannot update a role assignment that does not exist", () -> {

        given(mockRoleAssignmentDao.findById(1L)).willReturn(Optional.empty());

        RoleAssignment updatedRoleAssignment = aRoleAssignment(aRole(RoleType.SCHOOL));
        updatedRoleAssignment.setSchoolId("school1");
        updatedRoleAssignment.setId(1L);

        // When
        underTest.updateRoleAssignment(updatedRoleAssignment);

        // Then throws IllegalStateException
        });
    }

    @Test
    public void updateRoleAssignmentShouldNotPersistAnyUpdatesWhenNothingHasChanged() {
        // Given
        RoleAssignment roleAssignment = aRoleAssignment(aRole(RoleType.SCHOOL));
        roleAssignment.setSchoolId("school1");
        roleAssignment.setId(1L);

        given(mockRoleAssignmentDao.findById(1L)).willReturn(Optional.of(roleAssignment));

        // When
        underTest.updateRoleAssignment(roleAssignment);

        // Then
        verify(mockRoleAssignmentDao, never()).update(any(RoleAssignment.class));
    }

    @Test
    public void updateRoleAssignmentShouldThrowExceptionIfUpdatedStateIsInvalid() {
        // Given
        checkIllegalStateExcepton("Cannot create role assignment without user", () -> {

        RoleAssignment original = aRoleAssignment(aRole(RoleType.SCHOOL));
        original.setSchoolId("school1");
        original.setId(1L);
        given(mockRoleAssignmentDao.findById(1L)).willReturn(Optional.of(original));

        RoleAssignment update = aRoleAssignment(null);
        update.setUserId(null);
        update.setId(1L);

        // When
        underTest.updateRoleAssignment(update);

        // Then throws IllegalStateException
        });
    }

    @Test
    public void deleteRoleShouldRemoveTheRole() {
        // Given
        RoleModel originalRole = aRole(RoleType.SCHOOL);
        originalRole.setId(123L);
        given(mockRoleDao.findById(123L)).willReturn(Optional.of(originalRole));

        // When
        underTest.deleteRole(123L);

        // Then
        verify(mockRoleDao).deleteById(123L);
    }

    @Test
    public void deleteRoleShouldThrowExceptionWhenRoleDoesNotExist() {
        checkIllegalStateExcepton("Cannot delete a role that does not exist", () -> {

        given(mockRoleDao.findById(123L)).willReturn(Optional.empty());

        // When
        underTest.deleteRole(123L);

        // Then throws IllegalStateException
        });
    }

    @Test
    public void deleteRoleAssignmentShouldRemoveTheRoleAssignment() {
        // Given
        RoleAssignment original = aRoleAssignment(aRole(RoleType.ADMIN));
        original.setId(1L);

        given(mockRoleAssignmentDao.findById(1L)).willReturn(Optional.of(original));

        // When
        underTest.deleteRoleAssignment(1L);

        // Then
        verify(mockRoleAssignmentDao).deleteById(1L);
    }

    @Test
    public void deleteRoleAssignmentShouldThrowExceptionWhenRoleAssignmentDoesNotExist() {
        checkIllegalStateExcepton("Cannot delete a role assignment that does not exist", () -> {

        given(mockRoleAssignmentDao.findById(1L)).willReturn(null);

        // When
        underTest.deleteRoleAssignment(1L);

        // Then throws IllegalStateException
        });
    }

    private RoleModel aRole(RoleType roleType) {
        RoleModel role = new RoleModel();
        role.setName("Test Role");
        role.setDescription("Dummy role for testing");
        role.setType(roleType);
        role.setStatus("1");
        role.setPermissions(new HashSet<>());
        return role;
    }

    private PermissionModel aPermission(Permission permission, PermissionModel.PermissionType permissionType) {
        PermissionModel p = new PermissionModel();
        p.setType(permissionType);
        p.setPermission(permission);
        p.setLabel(permission.name());
        return p;
    }

    private RoleAssignment aRoleAssignment(RoleModel role) {
        RoleAssignment roleAssignment = new RoleAssignment();
        roleAssignment.setRole(role);
        roleAssignment.setUserId("user1");
        return roleAssignment;
    }

}
