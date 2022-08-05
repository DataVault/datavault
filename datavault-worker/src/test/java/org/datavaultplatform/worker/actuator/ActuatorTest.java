package org.datavaultplatform.worker.actuator;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.stream.Stream;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.datavaultplatform.worker.app.DataVaultWorkerInstanceApp;
import org.datavaultplatform.worker.queue.EventSender;
import org.datavaultplatform.worker.test.AddTestProperties;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(classes = DataVaultWorkerInstanceApp.class)
@AddTestProperties
@Slf4j
@TestPropertySource(properties = {
    "worker.rabbit.enabled=false",
    "management.endpoints.web.exposure.include=*",
    "management.health.rabbit.enabled=false"})
@AutoConfigureMockMvc
public class ActuatorTest {

  @Autowired
  MockMvc mvc;

  @MockBean
  EventSender sender;

  @Test
  @SneakyThrows
  void testActuatorPublicAccess() {
    Stream.of("/actuator/info", "/actuator/health").forEach(this::checkPublic);
  }

  @Test
  @SneakyThrows
  void testActuatorUnauthorized() {
    Stream.of("/actuator", "/actuator/", "/actuator/env")
        .forEach(this::checkUnauthorized);
  }

  @Test
  @SneakyThrows
  void testActuatorAuthorized() {
    Stream.of("/actuator", "/actuator/", "/actuator/env")
        .forEach(url -> checkAuthorized(url, "wactu", "wactupass"));
  }

  @SneakyThrows
  void checkUnauthorized(String url) {
    mvc.perform(get(url)).andDo(print()).andExpect(status().isUnauthorized());
  }

  @SneakyThrows
  void checkAuthorized(String url, String username, String password) {
    mvc.perform(get(url).with(httpBasic(username, password))).andDo(print())
        .andExpect(status().isOk());
  }

  @SneakyThrows
  void checkPublic(String url) {
    mvc.perform(get(url)).andDo(print()).andExpect(status().isOk());
  }
}
