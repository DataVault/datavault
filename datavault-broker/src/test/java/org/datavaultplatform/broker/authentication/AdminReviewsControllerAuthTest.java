package org.datavaultplatform.broker.authentication;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

import org.datavaultplatform.broker.controllers.admin.AdminReviewsController;
import org.datavaultplatform.common.model.DepositReview;
import org.datavaultplatform.common.model.Permission;
import org.datavaultplatform.common.model.VaultReview;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;

public class AdminReviewsControllerAuthTest extends BaseControllerAuthTest {

  @MockBean
  AdminReviewsController controller;

  @Captor
  ArgumentCaptor<VaultReview> argVaultReview;

  @Captor
  ArgumentCaptor<DepositReview> argDepositReview;

  @Captor
  ArgumentCaptor<String> argClientKey;

  @Captor
  ArgumentCaptor<String> argUserId;

  @Test
  void testGetCurrentReview() throws Exception {
    when(controller.getCurrentReview(
        USER_ID_1, "2112")).thenReturn(AuthTestData.REVIEW_INFO_1);

    checkWorksWhenAuthenticatedFailsOtherwise(
        get("/admin/vaults/{vaultid}/vaultreviews/current", "2112"),
        AuthTestData.REVIEW_INFO_1,
        Permission.CAN_MANAGE_VAULTS);

    verify(controller).getCurrentReview(USER_ID_1, "2112");
  }

  @Test
  void testGetVaultsForReview() {
    when(controller.getVaultsForReview(
        USER_ID_1)).thenReturn(AuthTestData.VAULTS_DATA);

    checkWorksWhenAuthenticatedFailsOtherwise(
        get("/admin/vaultsForReview"),
        AuthTestData.VAULTS_DATA,
        Permission.CAN_MANAGE_VAULTS);

    verify(controller).getVaultsForReview(USER_ID_1);
  }

  @Test
  void testPostCreateCurrentReview() throws Exception {
    when(controller.createCurrentReview(
        USER_ID_1, "vaultID1")).thenReturn(AuthTestData.REVIEW_INFO_1);

    checkWorksWhenAuthenticatedFailsOtherwise(
        post("/admin/vaults/vaultreviews/current")
            .content("vaultID1")
            .contentType(MediaType.APPLICATION_JSON),
        AuthTestData.REVIEW_INFO_1,
        Permission.CAN_MANAGE_VAULTS);

    verify(controller).createCurrentReview(USER_ID_1, "vaultID1");
  }

  @Test
  void testPutEditCurrentReview() throws Exception {

    //we have to use Captor for VaultReview cos it doesn't have decent equals method
    //and as it's an @Entity class.
    when(controller.editVaultReview(
        argUserId.capture(),
        argClientKey.capture(),
        argVaultReview.capture())).thenReturn(AuthTestData.VAULT_REVIEW_1);

    checkWorksWhenAuthenticatedFailsOtherwise(
        put("/admin/vaults/vaultreviews")
            .content(mapper.writeValueAsString(AuthTestData.VAULT_REVIEW_1))
            .contentType(MediaType.APPLICATION_JSON),
        AuthTestData.VAULT_REVIEW_1,
        Permission.CAN_MANAGE_VAULTS);

    verify(controller).editVaultReview(USER_ID_1, API_KEY_1, argVaultReview.getValue());
    assertEquals(AuthTestData.VAULT_REVIEW_1.getId(), argVaultReview.getValue().getId());
  }

  @Test
  void testPutEditDepositReview() throws Exception {

    //we have to use Captor for DepositReview cos it doesn't have decent equals method
    //and as it's an @Entity class.
    when(controller.editDepositReview(
        argUserId.capture(),
        argDepositReview.capture())).thenReturn(AuthTestData.DEPOSIT_REVIEW_1);

    checkWorksWhenAuthenticatedFailsOtherwise(
        put("/admin/vaultreviews/depositreviews")
            .content(mapper.writeValueAsString(AuthTestData.DEPOSIT_REVIEW_1))
            .contentType(MediaType.APPLICATION_JSON),
        AuthTestData.DEPOSIT_REVIEW_1,
        Permission.CAN_MANAGE_VAULTS);

    verify(controller).editDepositReview(USER_ID_1, argDepositReview.getValue());
    assertEquals(AuthTestData.DEPOSIT_REVIEW_1.getId(), argDepositReview.getValue().getId());
  }

  @AfterEach
  void securityCheck() {
    checkHasSecurityAdminVaultsRole();
  }
}
