package org.datavaultplatform.broker.authentication;

import org.datavaultplatform.broker.controllers.DepositsController;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class DepositsControllerRetrieveNoAuthTest extends BaseControllerAuthTest {

  @MockBean
  DepositsController controller;
  
  @Test
  void testRestartRetrieveNoSecurity() throws Exception {
    when(controller.retrieveRestart("retrieve-id-1")).thenReturn(true);

    mvc.perform(
            post("/retrieve/{retrieveId}/restart", "retrieve-id-1")).andDo(print())
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith("application/json"))
            .andExpect(content().string("true"));

    verify(controller).retrieveRestart("retrieve-id-1");
  }
}
