package org.datavaultplatform.webapp.authentication.database;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import org.datavaultplatform.common.model.*;
import org.datavaultplatform.common.request.ValidateUser;
import org.datavaultplatform.webapp.authentication.AuthenticationUtils;
import org.datavaultplatform.webapp.authentication.shib.ShibWebAuthenticationDetails;
import org.datavaultplatform.webapp.model.AdminDashboardPermissionsModel;
import org.datavaultplatform.webapp.security.ScopedGrantedAuthority;
import org.datavaultplatform.webapp.services.PermissionsService;
import org.datavaultplatform.webapp.services.RestService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DatabaseAuthenticationProviderTest {

    @Mock
    RestService mRestService;

    @Mock
    PermissionsService mPermissionService;

    @Mock
    ShibWebAuthenticationDetails mDetails;

    @Captor
    ArgumentCaptor<User> argUser;
    
    @Captor
    ArgumentCaptor<ValidateUser> argValidateUser1;

    @Captor
    ArgumentCaptor<ValidateUser> argValidateUser2;

    DatabaseAuthenticationProvider provider;
    RoleAssignment ra1;
    RoleAssignment ra2;

    List<PermissionModel> getModelsForAllPermissions(int skip) {
        return Arrays.stream(Permission.values()).skip(skip).limit(3).map(p -> {
            var pm = new PermissionModel();
            pm.setPermission(p);
            return pm;
        }).collect(Collectors.toList());
    }

    @BeforeEach
    void setup() {
        provider = new DatabaseAuthenticationProvider(mRestService, mPermissionService);

        RoleModel role1 = new RoleModel();
        role1.setName("role-one");
        role1.setPermissions(getModelsForAllPermissions(0));

        ra1 = new RoleAssignment();
        ra1.setUserId("test-user-id");
        ra1.setRole(role1);
        ra1.setVaultId("vault-1");

        RoleModel role2 = new RoleModel();
        role2.setName("role-two");
        role2.setPermissions(getModelsForAllPermissions(3));

        ra2 = new RoleAssignment();
        ra2.setUserId("test-user-id");
        ra2.setRole(role2);
        ra2.setSchoolId("group-1");
    }

    @Nested
    class UserExists {

        @Test
        void testAdminUserExists() {
            Collection<GrantedAuthority> authorities = Collections.emptyList();
            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken("test-user-id", null, authorities);
            authentication.setDetails(mDetails);

            when(mRestService.isValid(argValidateUser1.capture())).thenReturn(true);

            when(mRestService.getRoleAssignmentsForUser("test-user-id")).thenReturn(List.of(ra1, ra2));

            when(mRestService.isAdmin(argValidateUser2.capture())).thenReturn(true);

            Multimap<PermissionModel, String> model = ArrayListMultimap.create();
            model.put(PermissionModel.createDefault(Permission.CAN_MANAGE_DEPOSITS), "blah");
            model.put(PermissionModel.createDefault(Permission.CAN_MANAGE_ROLES), "blah");
            model.put(PermissionModel.createDefault(Permission.CAN_MANAGE_VAULTS), "blah");
            AdminDashboardPermissionsModel adminDashboardPermissionsModel = new AdminDashboardPermissionsModel(model, authentication);
            when(mPermissionService.getDashboardPermissions(authentication)).thenReturn(adminDashboardPermissionsModel);

            Authentication result = provider.authenticate(authentication);

            assertThat(result).isInstanceOfSatisfying(UsernamePasswordAuthenticationToken.class, preAuthToken -> {
                assertThat(preAuthToken).isNotNull();
                assertThat(preAuthToken.isAuthenticated()).isTrue();
                assertThat(preAuthToken.getPrincipal()).isEqualTo("test-user-id");
                assertThat(preAuthToken.getCredentials()).isNull();
                assertThat(preAuthToken.getDetails()).isNull();
                Collection<GrantedAuthority> actualAuthorities = preAuthToken.getAuthorities();
                assertThat(actualAuthorities).hasSize(8);
                assertThat(actualAuthorities).contains(new SimpleGrantedAuthority("ROLE_IS_ADMIN"));
                assertThat(actualAuthorities).contains(new SimpleGrantedAuthority("ROLE_ADMIN"));
                assertThat(actualAuthorities).contains(new SimpleGrantedAuthority("ROLE_ADMIN_DEPOSITS"));

                assertThat(actualAuthorities).contains(new SimpleGrantedAuthority("ROLE_ADMIN_ROLES"));
                assertThat(actualAuthorities).contains(new SimpleGrantedAuthority("ROLE_ADMIN_VAULTS"));
                assertThat(actualAuthorities).contains(new SimpleGrantedAuthority("ROLE_USER"));

                assertThat(actualAuthorities).anyMatch(ga -> ga instanceof ScopedGrantedAuthority scopedGa && scopedGa.getType().equals(Vault.class) && scopedGa.getId().equals("vault-1") && scopedGa.getAuthority().equals("n/a"));
                assertThat(actualAuthorities).anyMatch(ga -> ga instanceof ScopedGrantedAuthority scopedGa && scopedGa.getType().equals(Group.class) && scopedGa.getId().equals("group-1") && scopedGa.getAuthority().equals("n/a"));

            });

            ValidateUser actualUser1 = argValidateUser1.getValue();
            assertThat(actualUser1.getUserid()).isEqualTo("test-user-id");
            assertThat(actualUser1.getPassword()).isNull();

            ValidateUser actualUser2 = argValidateUser2.getValue();
            assertThat(actualUser2.getUserid()).isEqualTo("test-user-id");
            assertThat(actualUser2.getPassword()).isNull();


            verify(mRestService).isValid(actualUser1);
            verify(mRestService).getRoleAssignmentsForUser("test-user-id");
            verify(mPermissionService).getDashboardPermissions(authentication);
            verify(mRestService).isAdmin(actualUser2);

            verifyNoMoreInteractions(mRestService, mPermissionService);
        }

        @Test
        void testAdminUserExistsWithBadAuthority() {
            Collection<GrantedAuthority> authorities = Collections.emptyList();
            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken("test-user-id", null, authorities);
            authentication.setDetails(mDetails);

            when(mRestService.isValid(argValidateUser1.capture())).thenReturn(true);

            when(mRestService.getRoleAssignmentsForUser("test-user-id")).thenReturn(List.of(ra1, ra2));

            when(mRestService.isAdmin(argValidateUser2.capture())).thenReturn(true);

            Multimap<PermissionModel, String> model = ArrayListMultimap.create();
            model.put(PermissionModel.createDefault(Permission.CAN_MANAGE_DEPOSITS), "blah");
            model.put(PermissionModel.createDefault(Permission.CAN_MANAGE_ROLES), "blah");
            model.put(PermissionModel.createDefault(Permission.CAN_MANAGE_VAULTS), "blah");
            AdminDashboardPermissionsModel adminDashboardPermissionsModel = new AdminDashboardPermissionsModel(model, authentication);
            when(mPermissionService.getDashboardPermissions(authentication)).thenReturn(adminDashboardPermissionsModel);
            
            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
                try(MockedStatic<AuthenticationUtils> authUtils = Mockito.mockStatic(AuthenticationUtils.class)){
                    authUtils.when(() -> AuthenticationUtils.validateGrantedAuthorities(any())).thenThrow(new IllegalArgumentException("oops"));
                    provider.authenticate(authentication);
                }
            });
            
            assertThat(ex).hasMessage("oops");

            ValidateUser actualUser1 = argValidateUser1.getValue();
            assertThat(actualUser1.getUserid()).isEqualTo("test-user-id");
            assertThat(actualUser1.getPassword()).isNull();

            ValidateUser actualUser2 = argValidateUser2.getValue();
            assertThat(actualUser2.getUserid()).isEqualTo("test-user-id");
            assertThat(actualUser2.getPassword()).isNull();


            verify(mRestService).isValid(actualUser1);
            verify(mRestService).getRoleAssignmentsForUser("test-user-id");
            verify(mPermissionService).getDashboardPermissions(authentication);
            verify(mRestService).isAdmin(actualUser2);

            verifyNoMoreInteractions(mRestService, mPermissionService);
        }

    }

    @Test
    void testUserDoesNotExist() {
        Collection<GrantedAuthority> authorities = Collections.emptyList();
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken("test-user-id", null, authorities);
        authentication.setDetails(mDetails);

        when(mRestService.isValid(argValidateUser1.capture())).thenReturn(false);

        Exception ex = assertThrows(Exception.class, () -> provider.authenticate(authentication));

        assertThat(ex).hasMessage("Invalid userid or password");

        ValidateUser actualUser1 = argValidateUser1.getValue();
        assertThat(actualUser1.getUserid()).isEqualTo("test-user-id");
        assertThat(actualUser1.getPassword()).isNull();

        verify(mRestService).isValid(actualUser1);

        verifyNoMoreInteractions(mRestService, mPermissionService);
    }

    @Nested
    class SupportTests  {

        @Test
        void testPreAuthenticationToken() {
            assertThat(provider.supports(UsernamePasswordAuthenticationToken.class)).isTrue();
        }

        @Test
        void testNonPreAuthenticationToken() {
            assertThat(provider.supports(PreAuthenticatedAuthenticationToken.class)).isFalse();
        }

        @Test
        void testNullAuthentication() {
            assertThat(provider.supports(null)).isFalse();
        }
    }

}