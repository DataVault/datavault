package org.datavaultplatform.broker.authentication;

import org.datavaultplatform.broker.controllers.DepositsController;
import org.datavaultplatform.common.model.Permission;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
 
class DepositsControllerRetrieveAuthTest extends BaseControllerAuthTest {

  @MockBean
  DepositsController controller;
  
  @Test
  void testRestartRetrieveHasSecurity1() throws Exception {
    when(controller.retrieveRestart("retrieve-id-1")).thenReturn(true);

    MockHttpServletRequestBuilder builder = setupAuthentication(post("/retrieve/{retrieveId}/restart", "retrieve-id-1"));
    checkSuccessWhenAuthenticated(builder, Boolean.TRUE, HttpStatus.OK, false, Permission.CAN_MANAGE_DEPOSITS);

    verify(controller).retrieveRestart("retrieve-id-1");
  }

  @Test
  void testRestartRetrieveHasSecurity2() throws Exception {
    when(controller.retrieveRestart("retrieve-id-1")).thenReturn(true);

    MockHttpServletRequestBuilder builder = setupAuthentication(post("/retrieve/{retrieveId}/restart", "retrieve-id-1"));
    checkSuccessWhenAuthenticated(builder, Boolean.TRUE, HttpStatus.OK, true);

    verify(controller).retrieveRestart("retrieve-id-1");
  }
}
