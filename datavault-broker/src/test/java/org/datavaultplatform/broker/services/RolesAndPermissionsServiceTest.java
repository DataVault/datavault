package org.datavaultplatform.broker.services;

import org.datavaultplatform.common.model.*;
import org.datavaultplatform.common.model.dao.PermissionDAO;
import org.datavaultplatform.common.model.dao.RoleAssignmentDAO;
import org.datavaultplatform.common.model.dao.RoleDAO;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class RolesAndPermissionsServiceTest {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Mock
    private RoleDAO mockRoleDao;

    @Mock
    private PermissionDAO mockPermissionDao;

    @Mock
    private RoleAssignmentDAO mockRoleAssignmentDao;

    private RolesAndPermissionsService underTest;

    @Before
    public void setup() {
        underTest = new RolesAndPermissionsService();
        underTest.setPermissionDao(mockPermissionDao);
        underTest.setRoleDao(mockRoleDao);
        underTest.setRoleAssignmentDao(mockRoleAssignmentDao);
    }

    @Test
    public void createRoleShouldStoreANewRole() {
        // Given
        RoleModel toCreate = aRole(RoleType.VAULT);
        toCreate.addPermission(aPermission(Permission.MANAGE_ROLES, PermissionModel.PermissionType.VAULT));

        // When
        underTest.createRole(toCreate);

        // Then
        verify(mockRoleDao).store(toCreate);
    }

    @Test
    public void createRoleShouldThrowExceptionWhenRoleIdAlreadyExists() {
        // Given
        expectedException.expect(IllegalStateException.class);
        expectedException.expectMessage("Cannot create a role that already exists");

        RoleModel originalRole = aRole(RoleType.SCHOOL);
        originalRole.setId(123L);
        given(mockRoleDao.find(123L)).willReturn(originalRole);

        RoleModel toCreate = aRole(RoleType.SCHOOL);
        toCreate.setId(123L);

        // When
        underTest.createRole(toCreate);

        // Then throws IllegalStateException
    }

    @Test
    public void createRoleShouldThrowExceptionWhenSchoolPermissionIsAppliedToVaultRole() {
        // Given
        expectedException.expect(IllegalStateException.class);
        expectedException.expectMessage("Unable to apply permission with type SCHOOL to role of type VAULT");

        RoleModel toCreate = aRole(RoleType.VAULT);
        toCreate.addPermission(aPermission(Permission.CAN_MANAGE_SCHOOL_USERS, PermissionModel.PermissionType.SCHOOL));

        // When
        underTest.createRole(toCreate);

        // Then throws IllegalStateException
    }

    @Test
    public void createRoleShouldThrowExceptionWhenVaultPermissionIsAppliedToSchoolRole() {
        // Given
        expectedException.expect(IllegalStateException.class);
        expectedException.expectMessage("Unable to apply permission with type VAULT to role of type SCHOOL");

        RoleModel toCreate = aRole(RoleType.SCHOOL);
        toCreate.addPermission(aPermission(Permission.MANAGE_ROLES, PermissionModel.PermissionType.VAULT));

        // When
        underTest.createRole(toCreate);

        // Then throws IllegalStateException
    }

    @Test
    public void createRoleAssignmentShouldStoreANewSchoolRoleAssignment() {
        // Given
        RoleAssignment toCreate = aRoleAssignment(aRole(RoleType.SCHOOL), new User());
        toCreate.setSchool(new Group());
        given(mockRoleAssignmentDao.roleAssignmentExists(toCreate)).willReturn(false);

        // When
        underTest.createRoleAssignment(toCreate);

        // Then
        verify(mockRoleAssignmentDao).store(toCreate);
    }

    @Test
    public void createRoleAssignmentShouldStoreANewVaultRoleAssignment() {
        // Given
        RoleAssignment toCreate = aRoleAssignment(aRole(RoleType.VAULT), new User());
        toCreate.setVault(new Vault());
        given(mockRoleAssignmentDao.roleAssignmentExists(toCreate)).willReturn(false);

        // When
        underTest.createRoleAssignment(toCreate);

        // Then
        verify(mockRoleAssignmentDao).store(toCreate);
    }

    @Test
    public void createRoleAssignmentShouldThrowExceptionWhenNoUserIsPresent() {
        // Given
        expectedException.expect(IllegalStateException.class);
        expectedException.expectMessage("Cannot create role assignment without user");

        RoleAssignment toCreate = aRoleAssignment(aRole(RoleType.SCHOOL), null);
        toCreate.setSchool(new Group());

        // When
        underTest.createRoleAssignment(toCreate);

        // Then throws IllegalStateException
    }

    @Test
    public void createRoleAssignmentShouldThrowExceptionWhenNoRoleIsPresent() {
        // Given
        expectedException.expect(IllegalStateException.class);
        expectedException.expectMessage("Cannot create role assignment without role");

        RoleAssignment toCreate = aRoleAssignment(null, new User());

        // When
        underTest.createRoleAssignment(toCreate);

        // Then throws IllegalStateException
    }

    @Test
    public void createRoleAssignmentShouldThrowExceptionWhenSchoolRoleAssignmentHasNoSchool() {
        // Given
        expectedException.expect(IllegalStateException.class);
        expectedException.expectMessage("Cannot create school role assignment without a school");

        RoleAssignment toCreate = aRoleAssignment(aRole(RoleType.SCHOOL), new User());
        toCreate.setSchool(null);

        // When
        underTest.createRoleAssignment(toCreate);

        // Then throws IllegalStateException
    }

    @Test
    public void createRoleAssignmentShouldThrowExceptionWhenVaultRoleAssignmentHasNoVault() {
        // Given
        expectedException.expect(IllegalStateException.class);
        expectedException.expectMessage("Cannot create vault role assignment without a vault");

        RoleAssignment toCreate = aRoleAssignment(aRole(RoleType.VAULT), new User());
        toCreate.setVault(null);

        // When
        underTest.createRoleAssignment(toCreate);

        // Then throws IllegalStateException
    }

    @Test
    public void createRoleAssignmentShouldhrowExceptionWhenRoleAssignmentAlreadyExists() {
        // Given
        expectedException.expect(IllegalStateException.class);
        expectedException.expectMessage("Role assignment already exists");

        RoleAssignment toCreate = aRoleAssignment(aRole(RoleType.SCHOOL), new User());
        toCreate.setSchool(new Group());
        given(mockRoleAssignmentDao.roleAssignmentExists(toCreate)).willReturn(true);

        // When
        underTest.createRoleAssignment(toCreate);

        // Then throws IllegalStateException
    }

    @Test
    public void getSchoolPermissionsShouldReturnAllPermissionsApplicableToSchoolRoles() {
        // Given
        PermissionModel school1 = aPermission(Permission.CAN_VIEW_VAULTS_SIZE, PermissionModel.PermissionType.SCHOOL);
        PermissionModel school2 = aPermission(Permission.CAN_VIEW_IN_PROGRESS_DEPOSITS, PermissionModel.PermissionType.SCHOOL);
        PermissionModel school3 = aPermission(Permission.CAN_VIEW_DEPOSIT_QUEUE, PermissionModel.PermissionType.SCHOOL);

        List<PermissionModel> allPermissions = Arrays.asList(
                school1,
                school2,
                school3,
                aPermission(Permission.MANAGE_ROLES, PermissionModel.PermissionType.VAULT),
                aPermission(Permission.CAN_MANAGE_ROLES, PermissionModel.PermissionType.ADMIN),
                aPermission(Permission.CAN_MANAGE_BILLING_DETAILS, PermissionModel.PermissionType.ADMIN),
                aPermission(Permission.CAN_VIEW_EVENTS, PermissionModel.PermissionType.ADMIN));

        given(mockPermissionDao.findAll()).willReturn(allPermissions);

        // When
        Collection<PermissionModel> actual = underTest.getSchoolPermissions();

        // Then
        List<PermissionModel> expected = Arrays.asList(school1, school2, school3);
        assertEquals(expected, actual);
    }

    @Test
    public void getVaultPermissionsShouldReturnAllPermissionsApplicableToVaultRoles() {
        // Given
        PermissionModel vault1 = aPermission(Permission.MANAGE_ROLES, PermissionModel.PermissionType.VAULT);
        PermissionModel vault2 = aPermission(Permission.MANAGE_ROLES, PermissionModel.PermissionType.VAULT);
        PermissionModel vault3 = aPermission(Permission.MANAGE_ROLES, PermissionModel.PermissionType.VAULT);

        List<PermissionModel> allPermissions = Arrays.asList(
                aPermission(Permission.CAN_VIEW_VAULTS_SIZE, PermissionModel.PermissionType.SCHOOL),
                aPermission(Permission.CAN_VIEW_IN_PROGRESS_DEPOSITS, PermissionModel.PermissionType.SCHOOL),
                aPermission(Permission.CAN_VIEW_DEPOSIT_QUEUE, PermissionModel.PermissionType.SCHOOL),
                vault1,
                vault2,
                vault3,
                aPermission(Permission.CAN_MANAGE_BILLING_DETAILS, PermissionModel.PermissionType.ADMIN),
                aPermission(Permission.CAN_MANAGE_ROLES, PermissionModel.PermissionType.ADMIN),
                aPermission(Permission.CAN_MANAGE_RETENTION_POLICIES, PermissionModel.PermissionType.ADMIN));

        given(mockPermissionDao.findAll()).willReturn(allPermissions);

        // When
        Collection<PermissionModel> actual = underTest.getVaultPermissions();

        // Then
        List<PermissionModel> expected = Arrays.asList(vault1, vault2, vault3);
        assertEquals(expected, actual);
    }

    @Test
    public void getEditableRolesShouldReturnAllCustomRoles() {
        // Given
        RoleModel schoolRole = aRole(RoleType.SCHOOL);
        RoleModel vaultRole = aRole(RoleType.VAULT);
        RoleModel adminRole = aRole(RoleType.ADMIN);

        given(mockRoleDao.findAll()).willReturn(Arrays.asList(schoolRole, vaultRole, adminRole));

        // When
        List<RoleModel> actual = underTest.getEditableRoles();

        // Then
        List<RoleModel> expected = Arrays.asList(schoolRole, vaultRole);
        assertEquals(expected, actual);
    }

    @Test
    public void updateRoleShouldPersistChangesToRole() {
        // Given
        RoleModel originalRole = aRole(RoleType.SCHOOL);
        originalRole.setId(123L);
        given(mockRoleDao.find(123L)).willReturn(originalRole);

        RoleModel toUpdate = aRole(RoleType.SCHOOL);
        toUpdate.setId(123L);
        toUpdate.addPermission(aPermission(Permission.CAN_MANAGE_SCHOOL_USERS, PermissionModel.PermissionType.SCHOOL));

        // When
        underTest.updateRole(toUpdate);

        // Then
        verify(mockRoleDao).update(toUpdate);
    }

    @Test
    public void updateRoleShouldThrowExceptionWhenRoleDoesNotHaveAnId() {
        // Given
        expectedException.expect(IllegalStateException.class);
        expectedException.expectMessage("Cannot update a role that does not exist");

        RoleModel toUpdate = aRole(RoleType.SCHOOL);
        toUpdate.setId(null);
        toUpdate.addPermission(aPermission(Permission.CAN_MANAGE_SCHOOL_USERS, PermissionModel.PermissionType.SCHOOL));

        // When
        underTest.updateRole(toUpdate);

        // Then throws IllegalStateException
    }

    @Test
    public void updateRoleShouldThrowExceptionWhenRoleDoesNotExist() {
        // Given
        expectedException.expect(IllegalStateException.class);
        expectedException.expectMessage("Cannot update a role that does not exist");

        given(mockRoleDao.find(123L)).willReturn(null);

        RoleModel toUpdate = aRole(RoleType.SCHOOL);
        toUpdate.setId(123L);
        toUpdate.addPermission(aPermission(Permission.CAN_MANAGE_SCHOOL_USERS, PermissionModel.PermissionType.SCHOOL));

        // When
        underTest.updateRole(toUpdate);

        // Then
        verify(mockRoleDao).update(toUpdate);
    }

    @Test
    public void updateRoleShouldThrowExceptionWhenUnsupportedPermissionTypeIsPresent() {
        // Given
        expectedException.expect(IllegalStateException.class);
        expectedException.expectMessage("Unable to apply permission with type VAULT to role of type SCHOOL");

        RoleModel originalRole = aRole(RoleType.SCHOOL);
        originalRole.setId(123L);
        given(mockRoleDao.find(123L)).willReturn(originalRole);

        RoleModel toUpdate = aRole(RoleType.SCHOOL);
        toUpdate.setId(123L);
        toUpdate.addPermission(aPermission(Permission.MANAGE_ROLES, PermissionModel.PermissionType.VAULT));

        // When
        underTest.updateRole(toUpdate);

        // Then throws IllegalStateException
    }

    @Test
    public void updateRoleAssignmentShouldPersistAnUpdate() {
        // Given
        Group school = new Group();
        User user = new User();

        RoleModel originalRole = aRole(RoleType.SCHOOL);
        originalRole.setName("Role 1");
        RoleAssignment original = aRoleAssignment(originalRole, user);
        original.setSchool(school);
        original.setId(1L);

        given(mockRoleAssignmentDao.find(1L)).willReturn(original);

        RoleModel newRole = aRole(RoleType.SCHOOL);
        newRole.setName("Role 2");
        RoleAssignment updatedRoleAssignment = aRoleAssignment(newRole, user);
        updatedRoleAssignment.setSchool(school);
        updatedRoleAssignment.setId(1L);

        // When
        underTest.updateRoleAssignment(updatedRoleAssignment);

        // Then
        verify(mockRoleAssignmentDao).update(updatedRoleAssignment);
    }

    @Test
    public void updateRoleAssignmentShouldThrowExceptionWhenOriginalRoleDoesNotExist() {
        // Given
        expectedException.expect(IllegalStateException.class);
        expectedException.expectMessage("Cannot update a role assignment that does not exist");

        given(mockRoleAssignmentDao.find(1L)).willReturn(null);

        RoleAssignment updatedRoleAssignment = aRoleAssignment(aRole(RoleType.SCHOOL), new User());
        updatedRoleAssignment.setSchool(new Group());
        updatedRoleAssignment.setId(1L);

        // When
        underTest.updateRoleAssignment(updatedRoleAssignment);

        // Then throws IllegalStateException
    }

    @Test
    public void updateRoleAssignmentShouldNotPersistAnyUpdatesWhenNothingHasChanged() {
        // Given
        RoleAssignment roleAssignment = aRoleAssignment(aRole(RoleType.SCHOOL), new User());
        roleAssignment.setSchool(new Group());
        roleAssignment.setId(1L);

        given(mockRoleAssignmentDao.find(1L)).willReturn(roleAssignment);

        // When
        underTest.updateRoleAssignment(roleAssignment);

        // Then
        verify(mockRoleAssignmentDao, never()).update(any(RoleAssignment.class));
    }

    @Test
    public void updateRoleAssignmentShouldThrowExceptionIfUpdatedStateIsInvalid() {
        // Given
        expectedException.expect(IllegalStateException.class);
        expectedException.expectMessage("Cannot create role assignment without user");

        RoleAssignment original = aRoleAssignment(aRole(RoleType.SCHOOL), new User());
        original.setSchool(new Group());
        original.setId(1L);
        given(mockRoleAssignmentDao.find(1L)).willReturn(original);

        RoleAssignment update = aRoleAssignment(null, null);
        update.setId(1L);

        // When
        underTest.updateRoleAssignment(update);

        // Then throws IllegalStateException
    }

    @Test
    public void deleteRoleShouldRemoveTheRole() {
        // Given
        RoleModel originalRole = aRole(RoleType.SCHOOL);
        originalRole.setId(123L);
        given(mockRoleDao.find(123L)).willReturn(originalRole);

        // When
        underTest.deleteRole(123L);

        // Then
        verify(mockRoleDao).delete(123L);
    }

    @Test
    public void deleteRoleShouldThrowExceptionWhenRoleDoesNotExist() {
        // Given
        expectedException.expect(IllegalStateException.class);
        expectedException.expectMessage("Cannot delete a role that does not exist");

        given(mockRoleDao.find(123L)).willReturn(null);

        // When
        underTest.deleteRole(123L);

        // Then throws IllegalStateException
    }

    @Test
    public void deleteRoleAssignmentShouldRemoveTheRoleAssignment() {
        // Given
        RoleAssignment original = aRoleAssignment(aRole(RoleType.ADMIN), new User());
        original.setId(1L);

        given(mockRoleAssignmentDao.find(1L)).willReturn(original);

        // When
        underTest.deleteRoleAssignment(1L);

        // Then
        verify(mockRoleAssignmentDao).delete(1L);
    }

    @Test
    public void deleteRoleAssignmentShouldThrowExceptionWhenRoleAssignmentDoesNotExist() {
        // Given
        expectedException.expect(IllegalStateException.class);
        expectedException.expectMessage("Cannot delete a role assignment that does not exist");

        given(mockRoleAssignmentDao.find(1L)).willReturn(null);

        // When
        underTest.deleteRoleAssignment(1L);

        // Then throws IllegalStateException
    }

    private RoleModel aRole(RoleType roleType) {
        RoleModel role = new RoleModel();
        role.setName("Test Role");
        role.setDescription("Dummy role for testing");
        role.setType(roleType);
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

    private RoleAssignment aRoleAssignment(RoleModel role, User user) {
        RoleAssignment roleAssignment = new RoleAssignment();
        roleAssignment.setRole(role);
        roleAssignment.setUser(user);
        return roleAssignment;
    }

}
