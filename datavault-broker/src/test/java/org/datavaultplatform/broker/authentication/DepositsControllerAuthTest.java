package org.datavaultplatform.broker.authentication;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import java.util.Arrays;
import org.datavaultplatform.broker.controllers.DepositsController;
import org.datavaultplatform.common.model.Retrieve;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

public class DepositsControllerAuthTest extends BaseControllerAuthTest {

  @MockBean
  DepositsController controller;

  @Captor
  ArgumentCaptor<String> argUserId;

  @Captor
  ArgumentCaptor<String> argDepositId;

  @Captor
  ArgumentCaptor<Retrieve> argRetrieve;

  @Test
  void testPostAddDeposit() throws Exception {

    when(controller.addDeposit(USER_ID_1, AuthTestData.CREATE_DEPOSIT_1)).thenReturn(
        ResponseEntity.ok(AuthTestData.DEPOSIT_INFO_1));

    checkWorksWhenAuthenticatedFailsOtherwise(
        post("/deposits")
            .contentType(MediaType.APPLICATION_JSON)
            .content(mapper.writeValueAsString(AuthTestData.CREATE_DEPOSIT_1)),
        AuthTestData.DEPOSIT_INFO_1);

    verify(controller).addDeposit(USER_ID_1, AuthTestData.CREATE_DEPOSIT_1);
  }

  @Test
  void testGetDeposit() throws Exception {
    when(controller.getDeposit(USER_ID_1, "deposit-id-1")).thenReturn(AuthTestData.DEPOSIT_INFO_1);

    checkWorksWhenAuthenticatedFailsOtherwise(
        get("/deposits/{depositid}", "deposit-id-1"),
        AuthTestData.DEPOSIT_INFO_1);

    verify(controller).getDeposit(USER_ID_1, "deposit-id-1");
  }

  @Test
  void testGetDepositEvents() throws Exception {
    when(controller.getDepositEvents(USER_ID_1, "deposit-id-1")).thenReturn(
        Arrays.asList(AuthTestData.EVENT_INFO_1, AuthTestData.EVENT_INFO_2));

    checkWorksWhenAuthenticatedFailsOtherwise(
        get("/deposits/{depositid}/events", "deposit-id-1"),
        Arrays.asList(AuthTestData.EVENT_INFO_1, AuthTestData.EVENT_INFO_2));

    verify(controller).getDepositEvents(USER_ID_1, "deposit-id-1");
  }

  @Test
  void testGetDepositJobs() throws Exception {
    when(controller.getDepositJobs(USER_ID_1, "deposit-id-1")).thenReturn(
        Arrays.asList(AuthTestData.JOB_1, AuthTestData.JOB_2));

    checkWorksWhenAuthenticatedFailsOtherwise(
        get("/deposits/{depositid}/jobs", "deposit-id-1"),
        Arrays.asList(AuthTestData.JOB_1, AuthTestData.JOB_2));

    verify(controller).getDepositJobs(USER_ID_1, "deposit-id-1");
  }

  @Test
  void testGetDepositManifest() throws Exception {
    when(controller.getDepositManifest(USER_ID_1, "deposit-id-1")).thenReturn(
        Arrays.asList(AuthTestData.FILE_FIXITY_1,
            AuthTestData.FILE_FIXITY_2));

    checkWorksWhenAuthenticatedFailsOtherwise(
        get("/deposits/{depositid}/manifest", "deposit-id-1"),
        Arrays.asList(AuthTestData.FILE_FIXITY_1, AuthTestData.FILE_FIXITY_2));

    verify(controller).getDepositManifest(USER_ID_1, "deposit-id-1");
  }

  @Test
  void testGetDepositRetrieves() throws Exception {
    when(controller.getDepositRetrieves(USER_ID_1, "deposit-id-1")).thenReturn(
        Arrays.asList(AuthTestData.RETRIEVE_1,
            AuthTestData.RETRIEVE_2));

    checkWorksWhenAuthenticatedFailsOtherwise(
        get("/deposits/{depositid}/retrieves", "deposit-id-1"),
        Arrays.asList(AuthTestData.RETRIEVE_1, AuthTestData.RETRIEVE_2));

    verify(controller).getDepositRetrieves(USER_ID_1, "deposit-id-1");
  }

  @Test
  void testPostRestartDeposit() throws Exception {
    when(controller.restartDeposit(USER_ID_1, "deposit-id-1")).thenReturn(AuthTestData.DEPOSIT_1);

    checkWorksWhenAuthenticatedFailsOtherwise(
        post("/deposits/{depositid}/restart", "deposit-id-1"),
        AuthTestData.DEPOSIT_1);

    verify(controller).restartDeposit(USER_ID_1, "deposit-id-1");
  }

  @Test
  void testPostDepositRetrieve() throws Exception {
    when(controller.retrieveDeposit(argUserId.capture(), argDepositId.capture(),
        argRetrieve.capture())).thenReturn(false);

    checkWorksWhenAuthenticatedFailsOtherwise(
        post("/deposits/{depositid}/retrieve", "deposit-id-1")
            .content(mapper.writeValueAsString(AuthTestData.RETRIEVE_1))
            .contentType(MediaType.APPLICATION_JSON),
        false);

    verify(controller).retrieveDeposit(argUserId.getValue(), argDepositId.getValue(),
        argRetrieve.getValue());
    assertEquals(USER_ID_1, argUserId.getValue());
    assertEquals("deposit-id-1", argDepositId.getValue());
    assertEquals(AuthTestData.RETRIEVE_1.getID(), argRetrieve.getValue().getID());
  }


  @AfterEach
  void securityCheck() {
    checkHasSecurityUserAndClientUserRolesOnly();
  }
}
