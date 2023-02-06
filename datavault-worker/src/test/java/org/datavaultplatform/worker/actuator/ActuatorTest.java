package org.datavaultplatform.worker.actuator;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.Map;
import java.util.stream.Stream;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.datavaultplatform.common.event.RecordingEventSender;
import org.datavaultplatform.worker.app.DataVaultWorkerInstanceApp;
import org.datavaultplatform.worker.test.AddTestProperties;
import org.datavaultplatform.worker.test.TestClockConfig;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest(classes = DataVaultWorkerInstanceApp.class)
@AddTestProperties
@Slf4j
@TestPropertySource(properties = {
    "worker.rabbit.enabled=false",
    "management.endpoints.web.exposure.include=*",
    "management.health.rabbit.enabled=false"})
@AutoConfigureMockMvc
@Import(TestClockConfig.class)
public class ActuatorTest {

  @Autowired
  ObjectMapper mapper;

  @Autowired
  MockMvc mvc;

  @MockBean
  RecordingEventSender eventSender;

  @Test
  @SneakyThrows
  void testActuatorPublicAccess() {
    Stream.of("/actuator/info", "/actuator/health", "/actuator/metrics", "/actuator/memoryinfo").forEach(this::checkPublic);
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

  @Test
  void testMemoryInfo() throws Exception {
    MvcResult mvcResult = mvc.perform(
                    get("/actuator/memoryinfo"))
            .andExpect(content().contentTypeCompatibleWith("application/vnd.spring-boot.actuator.v3+json"))
            .andExpect(jsonPath("$.memory").exists())
            .andReturn();

    String json = mvcResult.getResponse().getContentAsString();
    Map<String,Object> infoMap = mapper.createParser(json).readValueAs(Map.class);

    String ct = (String)infoMap.get("timestamp");
    Assertions.assertEquals("2022-03-29T13:15:16.101Z",ct);

    assertTrue(infoMap.containsKey("memory"));
    Map<String,Object> innerMap = (Map<String,Object>)infoMap.get("memory");
    assertTrue(innerMap.containsKey("total"));
    assertTrue(innerMap.containsKey("free"));
    assertTrue(innerMap.containsKey("max"));

  }

}
