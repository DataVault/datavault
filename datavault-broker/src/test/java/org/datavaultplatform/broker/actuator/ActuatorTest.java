package org.datavaultplatform.broker.actuator;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.stream.Stream;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.datavaultplatform.broker.app.DataVaultBrokerApp;
import org.datavaultplatform.broker.queue.Sender;
import org.datavaultplatform.broker.test.AddTestProperties;
import org.datavaultplatform.broker.test.BaseDatabaseTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(classes = DataVaultBrokerApp.class)
@AddTestProperties
@Slf4j
@TestPropertySource(properties = {
    "broker.email.enabled=false",
    "broker.controllers.enabled=true",
    "broker.initialise.enabled=true",
    "broker.rabbit.enabled=false",
    "broker.scheduled.enabled=false",
    "management.endpoints.web.exposure.include=*",
    "management.health.rabbit.enabled=false"})
@AutoConfigureMockMvc
public class ActuatorTest extends BaseDatabaseTest {

  @Autowired
  MockMvc mvc;

  @MockBean
  Sender sender;

  @Test
  @SneakyThrows
  void testActuatorPublicAccess() {
    Stream.of("/actuator/info", "/actuator/health").forEach(this::checkPublic);
  }

  @Test
  @SneakyThrows
  void testActuatorUnauthorized() {
    Stream.of("/actuator", "/actuator/", "/actuator/env", "/users")
        .forEach(this::checkUnauthorized);
  }

  @Test
  @SneakyThrows
  void testActuatorAuthorized() {
    Stream.of("/actuator", "/actuator/", "/actuator/env")
        .forEach(url -> checkAuthorized(url, "bactor", "bactorpass"));
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
