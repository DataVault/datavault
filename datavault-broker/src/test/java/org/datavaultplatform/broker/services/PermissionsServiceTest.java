package org.datavaultplatform.broker.services;

import org.datavaultplatform.common.model.Permission;
import org.datavaultplatform.common.model.PermissionModel;
import org.datavaultplatform.common.model.RoleModel;
import org.datavaultplatform.common.model.RoleType;
import org.datavaultplatform.common.model.dao.PermissionDAO;
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
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class PermissionsServiceTest {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Mock
    private RoleDAO mockRoleDao;

    @Mock
    private PermissionDAO mockPermissionDao;

    private PermissionsService underTest;

    @Before
    public void setup() {
        underTest = new PermissionsService();
        underTest.setPermissionDao(mockPermissionDao);
        underTest.setRoleDao(mockRoleDao);
    }

    @Test
    public void createRoleShouldStoreANewRole() {
        // Given
        RoleModel toCreate = aRole(RoleType.VAULT);
        toCreate.addPermission(aPermission(Permission.VAULT_PERMISSION_1, PermissionModel.PermissionType.VAULT));

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
        originalRole.setId("123");
        given(mockRoleDao.find("123")).willReturn(originalRole);

        RoleModel toCreate = aRole(RoleType.SCHOOL);
        toCreate.setId("123");

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
        toCreate.addPermission(aPermission(Permission.SCHOOL_PERMISSION_1, PermissionModel.PermissionType.SCHOOL));

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
        toCreate.addPermission(aPermission(Permission.VAULT_PERMISSION_1, PermissionModel.PermissionType.VAULT));

        // When
        underTest.createRole(toCreate);

        // Then throws IllegalStateException
    }

    @Test
    public void getSchoolPermissionsShouldReturnAllPermissionsApplicableToSchoolRoles() {
        // Given
        PermissionModel school1 = aPermission(Permission.SCHOOL_PERMISSION_1, PermissionModel.PermissionType.SCHOOL);
        PermissionModel school2 = aPermission(Permission.SCHOOL_PERMISSION_2, PermissionModel.PermissionType.SCHOOL);
        PermissionModel school3 = aPermission(Permission.SCHOOL_PERMISSION_3, PermissionModel.PermissionType.SCHOOL);

        List<PermissionModel> allPermissions = Arrays.asList(
                school1,
                school2,
                school3,
                aPermission(Permission.VAULT_PERMISSION_1, PermissionModel.PermissionType.VAULT),
                aPermission(Permission.VAULT_PERMISSION_2, PermissionModel.PermissionType.VAULT),
                aPermission(Permission.VAULT_PERMISSION_3, PermissionModel.PermissionType.VAULT),
                aPermission(Permission.ADMIN_PERMISSION_1, PermissionModel.PermissionType.ADMIN),
                aPermission(Permission.ADMIN_PERMISSION_2, PermissionModel.PermissionType.ADMIN),
                aPermission(Permission.ADMIN_PERMISSION_3, PermissionModel.PermissionType.ADMIN));

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
        PermissionModel vault1 = aPermission(Permission.VAULT_PERMISSION_1, PermissionModel.PermissionType.VAULT);
        PermissionModel vault2 = aPermission(Permission.VAULT_PERMISSION_1, PermissionModel.PermissionType.VAULT);
        PermissionModel vault3 = aPermission(Permission.VAULT_PERMISSION_1, PermissionModel.PermissionType.VAULT);

        List<PermissionModel> allPermissions = Arrays.asList(
                aPermission(Permission.SCHOOL_PERMISSION_1, PermissionModel.PermissionType.SCHOOL),
                aPermission(Permission.SCHOOL_PERMISSION_2, PermissionModel.PermissionType.SCHOOL),
                aPermission(Permission.SCHOOL_PERMISSION_3, PermissionModel.PermissionType.SCHOOL),
                vault1,
                vault2,
                vault3,
                aPermission(Permission.ADMIN_PERMISSION_1, PermissionModel.PermissionType.ADMIN),
                aPermission(Permission.ADMIN_PERMISSION_2, PermissionModel.PermissionType.ADMIN),
                aPermission(Permission.ADMIN_PERMISSION_3, PermissionModel.PermissionType.ADMIN));

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
        originalRole.setId("123");
        given(mockRoleDao.find("123")).willReturn(originalRole);

        RoleModel toUpdate = aRole(RoleType.SCHOOL);
        toUpdate.setId("123");
        toUpdate.addPermission(aPermission(Permission.SCHOOL_PERMISSION_1, PermissionModel.PermissionType.SCHOOL));

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
        toUpdate.addPermission(aPermission(Permission.SCHOOL_PERMISSION_1, PermissionModel.PermissionType.SCHOOL));

        // When
        underTest.updateRole(toUpdate);

        // Then throws IllegalStateException
    }

    @Test
    public void updateRoleShouldThrowExceptionWhenRoleDoesNotExist() {
        // Given
        expectedException.expect(IllegalStateException.class);
        expectedException.expectMessage("Cannot update a role that does not exist");

        given(mockRoleDao.find("123")).willReturn(null);

        RoleModel toUpdate = aRole(RoleType.SCHOOL);
        toUpdate.setId("123");
        toUpdate.addPermission(aPermission(Permission.SCHOOL_PERMISSION_1, PermissionModel.PermissionType.SCHOOL));

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
        originalRole.setId("123");
        given(mockRoleDao.find("123")).willReturn(originalRole);

        RoleModel toUpdate = aRole(RoleType.SCHOOL);
        toUpdate.setId("123");
        toUpdate.addPermission(aPermission(Permission.VAULT_PERMISSION_1, PermissionModel.PermissionType.VAULT));

        // When
        underTest.updateRole(toUpdate);

        // Then throws IllegalStateException
    }

    @Test
    public void deleteRoleShouldRemoveTheRole() {
        // Given
        RoleModel originalRole = aRole(RoleType.SCHOOL);
        originalRole.setId("123");
        given(mockRoleDao.find("123")).willReturn(originalRole);

        // When
        underTest.deleteRole("123");

        // Then
        verify(mockRoleDao).delete("123");
    }

    @Test
    public void deleteRoleShouldThrowExceptionWhenRoleDoesNotExist() {
        // Given
        expectedException.expect(IllegalStateException.class);
        expectedException.expectMessage("Cannot delete a role that does not exist");

        given(mockRoleDao.find("123")).willReturn(null);

        // When
        underTest.deleteRole("123");

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

}