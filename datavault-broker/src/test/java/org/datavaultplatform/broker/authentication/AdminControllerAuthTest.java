package org.datavaultplatform.broker.authentication;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import java.util.Arrays;
import jakarta.servlet.http.HttpServletRequest;
import org.datavaultplatform.broker.controllers.admin.AdminController;
import org.datavaultplatform.common.model.Permission;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.ResponseEntity;

public class AdminControllerAuthTest extends BaseControllerAuthTest {

  @MockBean
  AdminController controller;
  @Captor
  ArgumentCaptor<String> argUserId;

  @Captor
  ArgumentCaptor<HttpServletRequest> argHttpRequest;

  @Test
  void testDeleteDeposit() throws Exception {
    when(controller.deleteDeposit(USER_ID_1, "deposit-id-123")).thenReturn(ResponseEntity.ok(null));

    checkWorksWhenAuthenticatedFailsOtherwise(
        delete("/admin/deposits/{depositID}", "deposit-id-123"),
        null, Permission.CAN_MANAGE_DEPOSITS);

    verify(controller).deleteDeposit(USER_ID_1, "deposit-id-123");
    checkHasSecurityAdminDepositsRole();
  }

  @Test
  void testGetAuditsAll() {

    when(controller.getAuditsAll(USER_ID_1)).thenReturn(
        Arrays.asList(AuthTestData.AUDIT_INFO_1, AuthTestData.AUDIT_INFO_2));

    checkWorksWhenAuthenticatedFailsOtherwise(
        get("/admin/audits"),
        Arrays.asList(AuthTestData.AUDIT_INFO_1, AuthTestData.AUDIT_INFO_2));

    verify(controller).getAuditsAll(USER_ID_1);
    checkHasSecurityUserAndClientUserRolesOnly();
  }

  @Test
  void testGetDepositsAll() {

    when(controller.getDepositsAll(USER_ID_1, "query1", "sort1", "order1", 123, 1234)).thenReturn(
        Arrays.asList(AuthTestData.DEPOSIT_INFO_1, AuthTestData.DEPOSIT_INFO_2));

    checkWorksWhenAuthenticatedFailsOtherwise(
        get("/admin/deposits")
            .queryParam("query", "query1")
            .queryParam("sort", "sort1")
            .queryParam("order", "order1")
            .queryParam("offset", "123")
            .queryParam("maxResult", "1234"),
        Arrays.asList(AuthTestData.DEPOSIT_INFO_1, AuthTestData.DEPOSIT_INFO_2),
        Permission.CAN_MANAGE_DEPOSITS);

    verify(controller).getDepositsAll(USER_ID_1, "query1", "sort1", "order1", 123, 1234);
    checkHasSecurityAdminDepositsRole();
  }

  @Test
  void testGetAllDepositsCount() {
    when(controller.getDepositsCount(USER_ID_1, "query1")).thenReturn(2112);

    checkWorksWhenAuthenticatedFailsOtherwise(
        get("/admin/deposits/count")
            .queryParam("query", "query1"),
        2112, Permission.CAN_MANAGE_DEPOSITS);

    verify(controller).getDepositsCount(USER_ID_1, "query1");
    checkHasSecurityAdminDepositsRole();
  }

  @Test
  void testGetDepositsAllData() {
    when(controller.getDepositsAllData(USER_ID_1, "sort1")).thenReturn(AuthTestData.DEPOSIT_DATA_1);

    checkWorksWhenAuthenticatedFailsOtherwise(
        get("/admin/deposits/data")
            .queryParam("sort", "sort1"),
        AuthTestData.DEPOSIT_DATA_1,
        Permission.CAN_MANAGE_DEPOSITS);

    verify(controller).getDepositsAllData(USER_ID_1, "sort1");
    checkHasSecurityAdminDepositsRole();
  }

  @Test
  void testGetAdminEvents() {

    when(controller.getEventsAll(USER_ID_1, "sort1")).thenReturn(
        Arrays.asList(AuthTestData.EVENT_INFO_1, AuthTestData.EVENT_INFO_2));

    checkWorksWhenAuthenticatedFailsOtherwise(
        get("/admin/events")
            .queryParam("sort", "sort1"),
        Arrays.asList(AuthTestData.EVENT_INFO_1, AuthTestData.EVENT_INFO_2),
        Permission.CAN_VIEW_EVENTS);

    verify(controller).getEventsAll(USER_ID_1, "sort1");
    checkHasSecurityAdminEventsRole();
  }

  @Test
  void testGetRetrievesAll() {

    when(controller.getRetrievesAll(USER_ID_1)).thenReturn(Arrays.asList(AuthTestData.RETRIEVE_1,
        AuthTestData.RETRIEVE_2));

    checkWorksWhenAuthenticatedFailsOtherwise(
        get("/admin/retrieves"),
        Arrays.asList(AuthTestData.RETRIEVE_1, AuthTestData.RETRIEVE_2),
        Permission.CAN_VIEW_RETRIEVES);

    verify(controller).getRetrievesAll(USER_ID_1);
    checkHasSecurityAdminRetrievesRole();
  }

  @Test
  void testGetVaultsAll() {
    when(controller.getVaultsAll(USER_ID_1, "sort1", "order1", "123", "1234")).thenReturn(
        AuthTestData.VAULTS_DATA);

    checkWorksWhenAuthenticatedFailsOtherwise(
        get("/admin/vaults")
            .queryParam("sort", "sort1")
            .queryParam("order", "order1")
            .queryParam("offset", "123")
            .queryParam("maxResult", "1234"),
        AuthTestData.VAULTS_DATA,
        Permission.CAN_MANAGE_VAULTS);

    verify(controller).getVaultsAll(USER_ID_1, "sort1", "order1", "123", "1234");
    checkHasSecurityAdminVaultsRole();
  }

  @Test
  void testRunAudit() {

    when(controller.runDepositAudit(argUserId.capture(), argHttpRequest.capture())).thenReturn(
        "Success");

    checkWorksWhenAuthenticatedFailsOtherwise(
        get("/admin/deposits/audit"),
        "Success",
        Permission.CAN_MANAGE_DEPOSITS);

    verify(controller).runDepositAudit(USER_ID_1, argHttpRequest.getValue());
    checkHasSecurityAdminDepositsRole();
  }
  @Test
  void testGetAdminDepositsCount() {

    when(controller.getDepositsCount(USER_ID_1, "query1")).thenReturn(2112);

    checkWorksWhenAuthenticatedFailsOtherwise(
        get("/admin/deposits/count")
            .queryParam("query", "query1"),
        2112,
        Permission.CAN_MANAGE_DEPOSITS);

    verify(controller).getDepositsCount(USER_ID_1, "query1");
    checkHasSecurityAdminDepositsRole();
  }
}
