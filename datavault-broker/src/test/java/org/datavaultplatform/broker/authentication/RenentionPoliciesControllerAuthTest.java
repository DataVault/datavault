package org.datavaultplatform.broker.authentication;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import java.util.Arrays;
import org.datavaultplatform.broker.controllers.RetentionPoliciesController;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;

public class RenentionPoliciesControllerAuthTest extends BaseControllerAuthTest {

  @MockBean
  RetentionPoliciesController controller;

  @Test
  void testGetPolicies() {
    when(controller.getPolicies(USER_ID_1, API_KEY_1)).thenReturn(
        Arrays.asList(AuthTestData.RETENTION_1, AuthTestData.RETENTION_2));

    checkWorksWhenAuthenticatedFailsOtherwise(
        get("/retentionpolicies"),
        Arrays.asList(AuthTestData.RETENTION_1, AuthTestData.RETENTION_2));

    verify(controller).getPolicies(USER_ID_1, API_KEY_1);
  }

  @AfterEach
  void securityCheck() {
    checkHasSecurityUserAndClientUserRolesOnly();
  }

}
