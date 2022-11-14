package org.datavaultplatform.broker.authentication;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.datavaultplatform.broker.controllers.NotifyController;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;

public class NotifyControllerAuthTest extends BaseControllerAuthTest {

  @MockBean
  NotifyController controller;

  @Test
  void testPutNotifyLogin() throws Exception {
    when(controller.login(USER_ID_1, API_KEY_1, AuthTestData.CREATE_CLIENT_EVENT)).thenReturn(
        "a-response");

    checkWorksWhenAuthenticatedFailsOtherwise(
        put("/notify/login")
            .content(mapper.writeValueAsString(AuthTestData.CREATE_CLIENT_EVENT))
            .contentType(MediaType.APPLICATION_JSON),
        "a-response");

    verify(controller).login(USER_ID_1, API_KEY_1, AuthTestData.CREATE_CLIENT_EVENT);
  }

  @Test
  void testPutNotifyLogout() throws JsonProcessingException {
    when(controller.logout(USER_ID_1, API_KEY_1, AuthTestData.CREATE_CLIENT_EVENT)).thenReturn(
        "a-response-2");

    checkWorksWhenAuthenticatedFailsOtherwise(
        put("/notify/logout")
            .content(mapper.writeValueAsString(AuthTestData.CREATE_CLIENT_EVENT))
            .contentType(MediaType.APPLICATION_JSON),
        "a-response-2");

    verify(controller).logout(USER_ID_1, API_KEY_1, AuthTestData.CREATE_CLIENT_EVENT);
  }

  @AfterEach
  void securityCheck() {
    checkHasSecurityUserAndClientUserRolesOnly();
  }

}
