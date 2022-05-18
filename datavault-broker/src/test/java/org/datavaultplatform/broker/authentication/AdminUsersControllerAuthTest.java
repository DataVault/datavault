package org.datavaultplatform.broker.authentication;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

import com.fasterxml.jackson.core.JsonProcessingException;
import java.util.Arrays;
import org.datavaultplatform.broker.controllers.admin.AdminUsersController;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

public class AdminUsersControllerAuthTest extends BaseControllerAuthTest {

  @MockBean
  AdminUsersController controller;

  @Test
  void testGetAdminUsers() {

    when(controller.getUsers(
        USER_ID_1, "query123")).thenReturn(Arrays.asList(AuthTestData.USER_1, AuthTestData.USER_2));

    checkWorksWhenAuthenticatedFailsOtherwise(
        get("/admin/users/search")
            .param("query", "query123"),
        Arrays.asList(AuthTestData.USER_1, AuthTestData.USER_2),
        HttpStatus.OK,
        true);

    verify(controller).getUsers(USER_ID_1, "query123");
  }

  @Test
  void testGetAdminUsersCount() {

    when(controller.getUsersCount(
        USER_ID_1)).thenReturn(2112);

    checkWorksWhenAuthenticatedFailsOtherwise(
        get("/admin/users/count"),
        2112,
        HttpStatus.OK,
        true);

    verify(controller).getUsersCount(USER_ID_1);
  }

  @Test
  void testPostAdminUsers() throws JsonProcessingException {

    when(controller.addUser(
        USER_ID_1, AuthTestData.USER_1)).thenReturn(AuthTestData.USER_2);

    checkWorksWhenAuthenticatedFailsOtherwise(
        post("/admin/users")
            .content(mapper.writeValueAsString(AuthTestData.USER_1))
            .contentType(MediaType.APPLICATION_JSON),
        AuthTestData.USER_2,
        HttpStatus.OK,
        true);

    verify(controller).addUser(USER_ID_1, AuthTestData.USER_1);
  }

  @Test
  void testPutAdminUsers() throws JsonProcessingException {

    when(controller.editUser(
        USER_ID_1, AuthTestData.USER_1)).thenReturn(AuthTestData.USER_2);

    checkWorksWhenAuthenticatedFailsOtherwise(
        put("/admin/users")
            .content(mapper.writeValueAsString(AuthTestData.USER_1))
            .contentType(MediaType.APPLICATION_JSON),
        AuthTestData.USER_2,
        HttpStatus.OK,
        true);

    verify(controller).editUser(USER_ID_1, AuthTestData.USER_1);
  }


  @AfterEach
  void securityCheck() {
    checkHasSecurityAdminRole();
  }
}
