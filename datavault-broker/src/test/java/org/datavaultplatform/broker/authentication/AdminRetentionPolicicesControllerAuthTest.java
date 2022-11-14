package org.datavaultplatform.broker.authentication;

import static org.datavaultplatform.broker.authentication.AuthTestData.CREATE_RETENTION_POLICY;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

import lombok.SneakyThrows;
import org.datavaultplatform.broker.controllers.admin.AdminRetentionPoliciesController;
import org.datavaultplatform.common.request.CreateRetentionPolicy;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

public class AdminRetentionPolicicesControllerAuthTest extends BaseControllerAuthTest {

  @MockBean
  AdminRetentionPoliciesController controller;

  @Captor
  ArgumentCaptor<String> argUserId;

  @Captor
  ArgumentCaptor<String> argClientKey;

  @Captor
  ArgumentCaptor<String> argPolicyId;

  @Captor
  ArgumentCaptor<CreateRetentionPolicy> argPolicy;

  @Test
  void testGetAdminRetentionPolicies() {

    when(controller.getRetentionPolicy(
        argUserId.capture(),
        argClientKey.capture(), argPolicyId.capture())).thenReturn(ResponseEntity.ok(
        CREATE_RETENTION_POLICY));

    checkWorksWhenAuthenticatedFailsOtherwise(
        get("/admin/retentionpolicies/{id}", "123"),
        CREATE_RETENTION_POLICY);

    verify(controller).getRetentionPolicy(USER_ID_1, API_KEY_1, "123");
  }

  @Test
  @SneakyThrows
  void testPutRetentionPolicy() {

    when(controller.editRetentionPolicy(
        argUserId.capture(),
        argClientKey.capture(),
        argPolicy.capture())).thenReturn(ResponseEntity.ok(CREATE_RETENTION_POLICY));

    checkWorksWhenAuthenticatedFailsOtherwise(
        put("/admin/retentionpolicies")
            .content(mapper.writeValueAsString(CREATE_RETENTION_POLICY))
            .contentType(MediaType.APPLICATION_JSON),
        CREATE_RETENTION_POLICY);

    verify(controller).editRetentionPolicy(USER_ID_1, API_KEY_1, CREATE_RETENTION_POLICY);

  }

  @Test
  @SneakyThrows
  void testAddRetentionPolicy() {

    when(controller.addRetentionPolicy(
        argUserId.capture(),
        argClientKey.capture(),
        argPolicy.capture())).thenReturn(
        ResponseEntity.status(HttpStatus.CREATED).body(CREATE_RETENTION_POLICY));

    checkWorksWhenAuthenticatedFailsOtherwise(
        post("/admin/retentionpolicies")
            .content(mapper.writeValueAsString(CREATE_RETENTION_POLICY))
            .contentType(MediaType.APPLICATION_JSON),
        CREATE_RETENTION_POLICY,
        HttpStatus.CREATED,
        false);

    verify(controller).addRetentionPolicy(USER_ID_1, API_KEY_1, CREATE_RETENTION_POLICY);

  }

  @Test
  void testDeleteAdminPolicy() {
    doNothing().when(controller).deleteRetentionPolicy(
        argUserId.capture(),
        argPolicyId.capture());

    checkWorksWhenAuthenticatedFailsOtherwise(
        delete("/admin/retentionpolicies/delete/{id}", "123"),
        null);

    verify(controller).deleteRetentionPolicy(USER_ID_1, "123");
  }

  @AfterEach
  void securityCheck() {
    checkHasSecurityUserAndClientUserRolesOnly();
  }
}
