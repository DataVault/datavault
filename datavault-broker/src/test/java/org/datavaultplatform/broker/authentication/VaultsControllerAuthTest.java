package org.datavaultplatform.broker.authentication;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import com.fasterxml.jackson.core.JsonProcessingException;
import java.util.Arrays;
import org.datavaultplatform.broker.controllers.VaultsController;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

public class VaultsControllerAuthTest extends BaseControllerAuthTest {

  @MockBean
  VaultsController controller;

  @Test
  void testGetDataManagers() throws Exception {

    when(controller.getDataManagers(USER_ID_1, "vault-id-1")).thenReturn(
        Arrays.asList(AuthTestData.DATA_MANAGER_1, AuthTestData.DATA_MANAGER_2));

    checkWorksWhenAuthenticatedFailsOtherwise(
        get("/vaults/{vaultid}/dataManagers", "vault-id-1"),
        Arrays.asList(AuthTestData.DATA_MANAGER_1, AuthTestData.DATA_MANAGER_2));

    verify(controller).getDataManagers(USER_ID_1, "vault-id-1");
  }

  @Test
  void testGetDeposits() throws Exception {
    when(controller.getDeposits(USER_ID_1, "vault-id-2")).thenReturn(
        Arrays.asList(AuthTestData.DEPOSIT_INFO_1, AuthTestData.DEPOSIT_INFO_2));

    checkWorksWhenAuthenticatedFailsOtherwise(
        get("/vaults/{vaultid}/deposits", "vault-id-2"),
        Arrays.asList(AuthTestData.DEPOSIT_INFO_1, AuthTestData.DEPOSIT_INFO_2));

    verify(controller).getDeposits(USER_ID_1, "vault-id-2");
  }

  @Test
  void testGetRoleEvents() throws Exception {
    when(controller.getRoleEvents(USER_ID_1, "vault-id-3")).thenReturn(
        Arrays.asList(AuthTestData.EVENT_INFO_1, AuthTestData.EVENT_INFO_2));

    checkWorksWhenAuthenticatedFailsOtherwise(
        get("/vaults/{vaultid}/roleEvents", "vault-id-3"),
        Arrays.asList(AuthTestData.EVENT_INFO_1, AuthTestData.EVENT_INFO_2));

    verify(controller).getRoleEvents(USER_ID_1, "vault-id-3");
  }

  @Test
  void testGetVaults() {
    when(controller.getVaults(USER_ID_1)).thenReturn(
        Arrays.asList(AuthTestData.VAULT_INFO_1, AuthTestData.VAULT_INFO_2));

    checkWorksWhenAuthenticatedFailsOtherwise(get("/vaults"),
        Arrays.asList(AuthTestData.VAULT_INFO_1, AuthTestData.VAULT_INFO_2));

    verify(controller).getVaults(USER_ID_1);
  }

  @Test
  void testGetVaultsForUser() {
    when(controller.getVaultsForUser("user123")).thenReturn(
        Arrays.asList(AuthTestData.VAULT_INFO_1, AuthTestData.VAULT_INFO_2));

    checkWorksWhenAuthenticatedFailsOtherwise(
        get("/vaults/user")
            .queryParam("userID", "user123"),
        Arrays.asList(AuthTestData.VAULT_INFO_1, AuthTestData.VAULT_INFO_2));

    verify(controller).getVaultsForUser("user123");
  }

  @Test
  void testGetSearchAllDeposits() {
    when(controller.searchAllDeposits(USER_ID_1, "query1", "sort1", "order1")).thenReturn(
        Arrays.asList(AuthTestData.DEPOSIT_INFO_1, AuthTestData.DEPOSIT_INFO_2));

    checkWorksWhenAuthenticatedFailsOtherwise(
        get("/vaults/deposits/search")
            .queryParam("query", "query1")
            .queryParam("sort", "sort1")
            .queryParam("order", "order1"),
        Arrays.asList(AuthTestData.DEPOSIT_INFO_1, AuthTestData.DEPOSIT_INFO_2));

    verify(controller).searchAllDeposits(USER_ID_1, "query1", "sort1", "order1");
  }

  @Test
  void testGetSearchAllDepositData() {
    when(controller.searchAllDepositsData(USER_ID_1, "query1", "sort1", "order1")).thenReturn(
        AuthTestData.DEPOSIT_DATA_1);

    checkWorksWhenAuthenticatedFailsOtherwise(
        get("/vaults/deposits/data/search")
            .queryParam("query", "query1")
            .queryParam("sort", "sort1")
            .queryParam("order", "order1"),
        AuthTestData.DEPOSIT_DATA_1);

    verify(controller).searchAllDepositsData(USER_ID_1, "query1", "sort1", "order1");
  }

  @Test
  void testGetSearchAllPendingVaults() {
    when(controller.searchAllPendingVaults(USER_ID_1, "query1", "sort1", "order1", "offset1",
        "confirmed1", "maxResult1")).thenReturn(AuthTestData.VAULTS_DATA);

    checkWorksWhenAuthenticatedFailsOtherwise(
        get("/pendingVaults/search")
            .queryParam("query", "query1")
            .queryParam("sort", "sort1")
            .queryParam("order", "order1")
            .queryParam("offset", "offset1")
            .queryParam("confirmed", "confirmed1")
            .queryParam("maxResult", "maxResult1"),
        AuthTestData.VAULTS_DATA);

    verify(controller).searchAllPendingVaults(USER_ID_1, "query1", "sort1", "order1", "offset1",
        "confirmed1", "maxResult1");
  }

  @Test
  void testGetSearchAllVaults() {
    when(controller.searchAllVaults(USER_ID_1, "query1", "sort1", "order1", "offset1",
        "maxResult1")).thenReturn(AuthTestData.VAULTS_DATA);

    checkWorksWhenAuthenticatedFailsOtherwise(
        get("/vaults/search")
            .queryParam("query", "query1")
            .queryParam("sort", "sort1")
            .queryParam("order", "order1")
            .queryParam("offset", "offset1")
            .queryParam("maxResult", "maxResult1"),
        AuthTestData.VAULTS_DATA);

    verify(controller).searchAllVaults(USER_ID_1, "query1", "sort1", "order1", "offset1",
        "maxResult1");
  }

  @Test
  void testGetCheckVaultRetentionPolicy() {
    when(controller.checkVaultRetentionPolicy(USER_ID_1, "vault-id-1")).thenReturn(
        AuthTestData.VAULT_1);

    checkWorksWhenAuthenticatedFailsOtherwise(
        get("/vaults/{vaultid}/checkretentionpolicy", "vault-id-1"),
        AuthTestData.VAULT_1);

    verify(controller).checkVaultRetentionPolicy(USER_ID_1, "vault-id-1");
  }

  @Test
  void testGetDataManager() throws Exception {
    when(controller.getDataManager(USER_ID_1, "vault-id-1", "uun-123")).thenReturn(
        AuthTestData.DATA_MANAGER_1);

    checkWorksWhenAuthenticatedFailsOtherwise(
        get("/vaults/{vaultid}/dataManager/{uun}", "vault-id-1", "uun-123"),
        AuthTestData.DATA_MANAGER_1);

    verify(controller).getDataManager(USER_ID_1, "vault-id-1", "uun-123");
  }

  @Test
  void testGetPendingVault() throws Exception {
    when(controller.getPendingVault(USER_ID_1, "vault-id-1")).thenReturn(AuthTestData.VAULT_INFO_1);

    checkWorksWhenAuthenticatedFailsOtherwise(
        get("/pendingVaults/vault-id-1"),
        AuthTestData.VAULT_INFO_1);

    verify(controller).getPendingVault(USER_ID_1, "vault-id-1");
  }

  @Test
  void testGetPendingVaultRecord() {
    when(controller.getPendingVaultRecord(USER_ID_1, "vault-id-1")).thenReturn(
        AuthTestData.PENDING_VAULT_1);

    checkWorksWhenAuthenticatedFailsOtherwise(get("/pendingVaults/vault-id-1/record"),
        AuthTestData.PENDING_VAULT_1);

    verify(controller).getPendingVaultRecord(USER_ID_1, "vault-id-1");
  }

  @Test
  void testGetPendingVaults() {
    when(controller.getPendingVaults(USER_ID_1)).thenReturn(
        Arrays.asList(AuthTestData.VAULT_INFO_1));

    checkWorksWhenAuthenticatedFailsOtherwise(
        get("/pendingVaults"),
        Arrays.asList(AuthTestData.VAULT_INFO_1));

    verify(controller).getPendingVaults(USER_ID_1);
  }

  @Test
  void testGetVault() throws Exception {
    when(controller.getVault(USER_ID_1, "vault-id-1")).thenReturn(AuthTestData.VAULT_INFO_1);

    checkWorksWhenAuthenticatedFailsOtherwise(
        get("/vaults/vault-id-1"),
        AuthTestData.VAULT_INFO_1);

    verify(controller).getVault(USER_ID_1, "vault-id-1");
  }

  @Test
  void testGetVaultRecord() {
    when(controller.getVaultRecord(USER_ID_1, "vault-id-1")).thenReturn(AuthTestData.VAULT_1);

    checkWorksWhenAuthenticatedFailsOtherwise(
        get("/vaults/vault-id-1/record"),
        AuthTestData.VAULT_1);

    verify(controller).getVaultRecord(USER_ID_1, "vault-id-1");
  }

  @Test
  void testPostAddDataManager() throws Exception {
    when(controller.addDataManager(USER_ID_1, "vault-id-1", "uun-001")).thenReturn(
        AuthTestData.VAULT_INFO_1);

    checkWorksWhenAuthenticatedFailsOtherwise(
        post("/vaults/vault-id-1/addDataManager")
            .content("uun-001"),
        AuthTestData.VAULT_INFO_1);

    verify(controller).addDataManager(USER_ID_1, "vault-id-1", "uun-001");
  }

  @Test
  void testPostAddPendingVault() throws Exception {
    when(controller.addPendingVault(USER_ID_1, API_KEY_1, AuthTestData.CREATE_VAULT)).thenReturn(
        AuthTestData.VAULT_INFO_1);

    checkWorksWhenAuthenticatedFailsOtherwise(
        post("/pendingVaults")
            .content(mapper.writeValueAsString(AuthTestData.CREATE_VAULT))
            .contentType(MediaType.APPLICATION_JSON),
        AuthTestData.VAULT_INFO_1);

    verify(controller).addPendingVault(USER_ID_1, API_KEY_1, AuthTestData.CREATE_VAULT);
  }

  @Test
  void testPostAddVault() throws Exception {
    when(controller.addVault(USER_ID_1, API_KEY_1, AuthTestData.CREATE_VAULT)).thenReturn(
        AuthTestData.VAULT_INFO_1);

    checkWorksWhenAuthenticatedFailsOtherwise(
        post("/vaults")
            .content(mapper.writeValueAsString(AuthTestData.CREATE_VAULT))
            .contentType(MediaType.APPLICATION_JSON),
        AuthTestData.VAULT_INFO_1);

    verify(controller).addVault(USER_ID_1, API_KEY_1, AuthTestData.CREATE_VAULT);
  }

  @Test
  void testDeleteDataManager() throws Exception {
    when(controller.deleteDataManager(USER_ID_1, "vault-id-1", "data-manager-id-1")).thenReturn(
        AuthTestData.VAULT_INFO_1);

    checkWorksWhenAuthenticatedFailsOtherwise(
        delete("/vaults/vault-id-1/deleteDataManager/data-manager-id-1"),
        AuthTestData.VAULT_INFO_1);

    verify(controller).deleteDataManager(USER_ID_1, "vault-id-1", "data-manager-id-1");
  }

  @Test
  void testPostTransferVault() throws JsonProcessingException {
    when(controller.transferVault(USER_ID_1, API_KEY_1, "vault-id-1",
        AuthTestData.TRANSFER_VAULT_1)).thenReturn(ResponseEntity.ok().build());

    checkWorksWhenAuthenticatedFailsOtherwise(post("/vaults/vault-id-1/transfer")
            .content(mapper.writeValueAsString(AuthTestData.TRANSFER_VAULT_1))
            .contentType(MediaType.APPLICATION_JSON),
        null);

    verify(controller).transferVault(USER_ID_1, API_KEY_1, "vault-id-1",
        AuthTestData.TRANSFER_VAULT_1);
  }

  @Test
  void testPostUpdatePendingVault() throws Exception {
    when(controller.updatePendingVault(USER_ID_1, API_KEY_1, AuthTestData.CREATE_VAULT)).thenReturn(
        AuthTestData.VAULT_INFO_1);

    checkWorksWhenAuthenticatedFailsOtherwise(
        post("/pendingVaults/update")
            .content(mapper.writeValueAsString(AuthTestData.CREATE_VAULT))
            .contentType(MediaType.APPLICATION_JSON),
        AuthTestData.VAULT_INFO_1);

    verify(controller).updatePendingVault(USER_ID_1, API_KEY_1, AuthTestData.CREATE_VAULT);

  }

  @Test
  void testPostUpdateVaultDescription() throws Exception {
    when(controller.updateVaultDescription(USER_ID_1, API_KEY_1, "vault-id-1",
        "description-1")).thenReturn(AuthTestData.VAULT_INFO_1);

    checkWorksWhenAuthenticatedFailsOtherwise(
        post("/vaults/vault-id-1/updateVaultDescription")
            .content("description-1"),
        AuthTestData.VAULT_INFO_1);

    verify(controller).updateVaultDescription(USER_ID_1, API_KEY_1, "vault-id-1", "description-1");
  }

  @Test
  void testPostUpdateVaultName() throws Exception {
    when(controller.updateVaultName(USER_ID_1, API_KEY_1, "vault-id-1", "name-1")).thenReturn(
        AuthTestData.VAULT_INFO_1);

    checkWorksWhenAuthenticatedFailsOtherwise(
        post("/vaults/vault-id-1/updateVaultName")
            .content("name-1"),
        AuthTestData.VAULT_INFO_1);

    verify(controller).updateVaultName(USER_ID_1, API_KEY_1, "vault-id-1", "name-1");
  }

  @Test
  void testPostUpdateVaultReviewDate() throws Exception {
    when(controller.updateVaultReviewDate(USER_ID_1, "vault-id-1", "review-date-1")).thenReturn(
        AuthTestData.VAULT_INFO_1);

    checkWorksWhenAuthenticatedFailsOtherwise(
        post("/vaults/vault-id-1/updatereviewdate")
            .content("review-date-1"),
        AuthTestData.VAULT_INFO_1);

    verify(controller).updateVaultReviewDate(USER_ID_1, "vault-id-1", "review-date-1");
  }

  @AfterEach
  void securityCheck() {
    checkHasSecurityUserAndClientUserRolesOnly();
  }

}
