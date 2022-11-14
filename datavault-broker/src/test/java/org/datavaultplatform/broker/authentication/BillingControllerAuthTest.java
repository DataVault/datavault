package org.datavaultplatform.broker.authentication;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.datavaultplatform.broker.controllers.admin.BillingController;
import org.datavaultplatform.common.model.Permission;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;

public class BillingControllerAuthTest extends BaseControllerAuthTest {

  @MockBean
  BillingController controller;

  @Test
  void testGetAdminBilling() {
    when(controller.getVaultBillingInfo(
        USER_ID_1, "2112")).thenReturn(AuthTestData.BILLING_INFO_1);

    checkWorksWhenAuthenticatedFailsOtherwise(
        get("/admin/billing/{vaultId}", "2112"),
        AuthTestData.BILLING_INFO_1,
        Permission.CAN_MANAGE_BILLING_DETAILS);

    verify(controller).getVaultBillingInfo(USER_ID_1, "2112");
  }

  @Test
  void testGetSearchAllBillingVaults() {
    when(controller.searchAllBillingVaults(
        USER_ID_1, "query1", "sort1", "order1", "offset1", "maxResult1")).thenReturn(
        AuthTestData.VAULTS_DATA);

    checkWorksWhenAuthenticatedFailsOtherwise(
        get("/admin/billing/search")
            .param("query", "query1")
            .param("sort", "sort1")
            .param("order", "order1")
            .param("offset", "offset1")
            .param("maxResult", "maxResult1"),
        AuthTestData.VAULTS_DATA,
        Permission.CAN_MANAGE_BILLING_DETAILS);

    verify(controller).searchAllBillingVaults(USER_ID_1, "query1", "sort1", "order1", "offset1",
        "maxResult1");
  }


  @Test
  void testPostAdminBilling() throws JsonProcessingException {
    when(controller.updateBillingDetails(
        USER_ID_1, "2112", AuthTestData.BILLING_INFO_1)).thenReturn(AuthTestData.BILLING_INFO_1);

    checkWorksWhenAuthenticatedFailsOtherwise(
        post("/admin/billing/{vaultid}/updateBilling", "2112")
            .content(mapper.writeValueAsString(AuthTestData.BILLING_INFO_1))
            .contentType(MediaType.APPLICATION_JSON),
        AuthTestData.BILLING_INFO_1,
        Permission.CAN_MANAGE_BILLING_DETAILS);

    verify(controller).updateBillingDetails(USER_ID_1, "2112", AuthTestData.BILLING_INFO_1);
  }

  @AfterEach
  void securityCheck() {
    checkHasSecurityAdminBillingRole();
  }
}
