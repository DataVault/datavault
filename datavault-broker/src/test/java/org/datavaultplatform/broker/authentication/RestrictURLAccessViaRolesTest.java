package org.datavaultplatform.broker.authentication;

import static org.datavaultplatform.broker.authentication.AuthTestData.ARCHIVE_STORE_1;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import java.util.Arrays;
import java.util.List;
import org.datavaultplatform.broker.controllers.UsersController;
import org.datavaultplatform.broker.controllers.admin.AdminArchiveStoreController;
import org.datavaultplatform.broker.controllers.admin.AdminUsersController;
import org.datavaultplatform.common.model.ArchiveStore;
import org.datavaultplatform.common.model.Permission;
import org.datavaultplatform.common.model.RoleName;
import org.datavaultplatform.common.model.User;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public class RestrictURLAccessViaRolesTest extends BaseControllerAuthTest{

  @MockBean
  AdminArchiveStoreController archiveStoreController;

  @MockBean
  AdminUsersController adminUsersController;

  @MockBean
  UsersController userController;

  User user1 = new User();
  User user2 = new User();

  @Test
  void testGetArchiveStores() {

    ResponseEntity<List<ArchiveStore>> result = ResponseEntity.ok(
        Arrays.asList(ARCHIVE_STORE_1, AuthTestData.ARCHIVE_STORE_2));

    when(archiveStoreController.getArchiveStores(USER_ID_1)).thenReturn(result);

    checkSuccessWhenAuthenticated(get("/admin/archivestores"), Arrays.asList(
            ARCHIVE_STORE_1, AuthTestData.ARCHIVE_STORE_2), HttpStatus.OK,
        false, Permission.CAN_MANAGE_ARCHIVE_STORES);

    verify(archiveStoreController).getArchiveStores(USER_ID_1);

    checkSecurityRoles(RoleName.ROLE_USER, RoleName.ROLE_CLIENT_USER, RoleName.ROLE_ADMIN_ARCHIVESTORES);
  }

  @Test
  void testAdminUserSearch() {

    List<User> result = Arrays.asList(user1, user2);

    when(adminUsersController.getUsers(USER_ID_1,"")).thenReturn(result);

    checkSuccessWhenAuthenticated(get("/admin/users/search?query="), Arrays.asList(user1, user2), HttpStatus.OK,
        true);

    verify(adminUsersController).getUsers(USER_ID_1, "");

    checkSecurityRoles(RoleName.ROLE_CLIENT_USER, RoleName.ROLE_ADMIN);
  }

  @Test
  void testUserSearch() {

    List<User> result = Arrays.asList(user1, user2);

    when(userController.getUsers(USER_ID_1)).thenReturn(result);

    checkSuccessWhenAuthenticated(get("/users"), Arrays.asList(user1, user2), HttpStatus.OK,
        false);

    verify(userController).getUsers(USER_ID_1);

    checkSecurityRoles(RoleName.ROLE_CLIENT_USER, RoleName.ROLE_USER);
  }
}
