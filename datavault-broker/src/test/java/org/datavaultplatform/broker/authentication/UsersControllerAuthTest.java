package org.datavaultplatform.broker.authentication;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import com.fasterxml.jackson.core.JsonProcessingException;
import java.util.Arrays;
import lombok.extern.slf4j.Slf4j;
import org.datavaultplatform.broker.controllers.UsersController;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;

@Slf4j
public class UsersControllerAuthTest extends BaseControllerAuthTest {

  @MockBean
  UsersController controller;

  @Test
  void testGetUsers() {
    when(controller.getUsers(USER_ID_1)).thenReturn(
        Arrays.asList(AuthTestData.USER_1, AuthTestData.USER_2));

    checkWorksWhenAuthenticatedFailsOtherwise(
        get("/users"),
        Arrays.asList(AuthTestData.USER_1, AuthTestData.USER_2));

    verify(controller).getUsers(USER_ID_1);
  }

  @Test
  void testGetUser() {
    when(controller.getUser("query-user-id")).thenReturn(AuthTestData.USER_1);

    checkWorksWhenAuthenticatedFailsOtherwise(
        get("/users/{userid}", "query-user-id"),
        AuthTestData.USER_1);

    verify(controller).getUser("query-user-id");
  }

  @Test
  void testPostUserExists() throws JsonProcessingException {
    when(controller.exists(AuthTestData.VALIDATE_USER)).thenReturn(true);

    checkWorksWhenAuthenticatedFailsOtherwise(
        post("/auth/users/exists")
            .content(mapper.writeValueAsString(AuthTestData.VALIDATE_USER))
            .contentType(MediaType.APPLICATION_JSON),
        true);

    verify(controller).exists(AuthTestData.VALIDATE_USER);
  }

  @Test
  void testPostAddUser() throws JsonProcessingException {
    when(controller.addUser(AuthTestData.USER_1)).thenReturn(AuthTestData.USER_2);

    checkWorksWhenAuthenticatedFailsOtherwise(
        post("/users")
            .content(mapper.writeValueAsString(AuthTestData.USER_1))
            .contentType(MediaType.APPLICATION_JSON),
        AuthTestData.USER_2);

    verify(controller).addUser(AuthTestData.USER_1);
  }

  @Test
  void testPostIsAdmin() throws JsonProcessingException {

    when(controller.isAdmin(AuthTestData.VALIDATE_USER)).thenReturn(true);

    checkWorksWhenAuthenticatedFailsOtherwise(
        post("/auth/users/isadmin")
            .content(mapper.writeValueAsString(AuthTestData.VALIDATE_USER))
            .contentType(MediaType.APPLICATION_JSON),
        true);

    verify(controller).isAdmin(AuthTestData.VALIDATE_USER);
  }

  @Test
  void testPostValidateUser() throws JsonProcessingException {
    when(controller.validateUser(AuthTestData.VALIDATE_USER)).thenReturn(true);

    checkWorksWhenAuthenticatedFailsOtherwise(
        post("/auth/users/isvalid")
            .content(mapper.writeValueAsString(AuthTestData.VALIDATE_USER))
            .contentType(MediaType.APPLICATION_JSON),
        true);

    verify(controller).validateUser(AuthTestData.VALIDATE_USER);
  }

  @AfterEach
  void securityCheck() {
    checkHasSecurityUserAndClientUserRolesOnly();
  }
}
