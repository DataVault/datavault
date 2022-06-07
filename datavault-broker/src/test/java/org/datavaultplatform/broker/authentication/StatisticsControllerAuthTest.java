package org.datavaultplatform.broker.authentication;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import java.util.Arrays;
import org.datavaultplatform.broker.controllers.StatisticsController;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;

public class StatisticsControllerAuthTest extends BaseControllerAuthTest {

  @MockBean
  StatisticsController controller;

  @Test
  void testGetDepositsCount() {
    when(controller.getDepositsCount(USER_ID_1)).thenReturn(1234);

    checkWorksWhenAuthenticatedFailsOtherwise(
        get("/statistics/depositcount"),
        1234);

    verify(controller).getDepositsCount(USER_ID_1);
  }

  @Test
  void testGetDepositsInProgress() {
    when(controller.getDepositsInProgress(USER_ID_1)).thenReturn(
        Arrays.asList(AuthTestData.DEPOSIT_1, AuthTestData.DEPOSIT_2));

    checkWorksWhenAuthenticatedFailsOtherwise(
        get("/vaults/depositinprogress"),
        Arrays.asList(AuthTestData.DEPOSIT_1, AuthTestData.DEPOSIT_2));

    verify(controller).getDepositsInProgress(USER_ID_1);
  }

  @Test
  void testGetDepositsQueueCount() {
    when(controller.getDepositsQueueCount(USER_ID_1)).thenReturn(1234);

    checkWorksWhenAuthenticatedFailsOtherwise(
        get("/vaults/depositqueuecount"),
        1234);

    verify(controller).getDepositsQueueCount(USER_ID_1);
  }

  @Test
  void testGetDepositsInProgressCount() {
    when(controller.getDepositsInProgressCount(USER_ID_1)).thenReturn(1234);

    checkWorksWhenAuthenticatedFailsOtherwise(
        get("/statistics/depositinprogresscount"),
        1234);

    verify(controller).getDepositsInProgressCount(USER_ID_1);
  }

  @Test
  void testGetEventCount() {
    when(controller.getEventCount(USER_ID_1)).thenReturn(1234L);

    checkWorksWhenAuthenticatedFailsOtherwise(
        get("/statistics/eventcount"),
        1234);

    verify(controller).getEventCount(USER_ID_1);
  }

  @Test
  void testGetPolicyStatusCount() {

    when(controller.getPolicyStatusCount(USER_ID_1, 111)).thenReturn(1234);

    checkWorksWhenAuthenticatedFailsOtherwise(
        get("/vaults/retentionpolicycount/{status}", "111"),
        1234);

    verify(controller).getPolicyStatusCount(USER_ID_1, 111);
  }

  @Test
  void testGetRetrievesCount() {
    when(controller.getRetrievesCount(USER_ID_1)).thenReturn(1234);

    checkWorksWhenAuthenticatedFailsOtherwise(
        get("/statistics/retrievecount"),
        1234);

    verify(controller).getRetrievesCount(USER_ID_1);
  }

  @Test
  void testGetRetrievesInProgress() {
    when(controller.getRetrievesInProgress(USER_ID_1)).thenReturn(
        Arrays.asList(AuthTestData.RETRIEVE_1, AuthTestData.RETRIEVE_2));

    checkWorksWhenAuthenticatedFailsOtherwise(
        get("/vaults/retrieveinprogress"),
        Arrays.asList(AuthTestData.RETRIEVE_1, AuthTestData.RETRIEVE_2));

    verify(controller).getRetrievesInProgress(USER_ID_1);
  }

  @Test
  void testGetRetrievesInProgressCount() {
    when(controller.getRetrievesInProgressCount(USER_ID_1)).thenReturn(1234);

    checkWorksWhenAuthenticatedFailsOtherwise(
        get("/statistics/retrieveinprogresscount"),
        1234);

    verify(controller).getRetrievesInProgressCount(USER_ID_1);
  }

  @Test
  void testGetRetrievesQueuedCount() {
    when(controller.getRetrievesQueuedCount(USER_ID_1)).thenReturn(1234);

    checkWorksWhenAuthenticatedFailsOtherwise(
        get("/vaults/retrievequeuecount"),
        1234);

    verify(controller).getRetrievesQueuedCount(USER_ID_1);
  }

  @Test
  void testGetTotalNumberOfPendingVaults() {
    when(controller.getTotalNumberOfPendingVaults()).thenReturn(1234);

    checkWorksWhenAuthenticatedFailsOtherwise(
        get("/statistics/pendingVaultsTotal"),
        1234);

    verify(controller).getTotalNumberOfPendingVaults();
  }

  @Test
  void testVaultsCount() {
    when(controller.getVaultsCount(USER_ID_1)).thenReturn(1234);

    checkWorksWhenAuthenticatedFailsOtherwise(
        get("/statistics/count"),
        1234);

    verify(controller).getVaultsCount(USER_ID_1);
  }

  @Test
  void testVaultsSize() {
    when(controller.getVaultsSize(USER_ID_1)).thenReturn(1234L);

    checkWorksWhenAuthenticatedFailsOtherwise(
        get("/statistics/size"),
        1234);

    verify(controller).getVaultsSize(USER_ID_1);
  }

  @AfterEach
  void securityCheck() {
    checkHasSecurityUserAndClientUserRolesOnly();
  }
}
