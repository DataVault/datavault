package org.datavaultplatform.worker.actuator;

import io.swagger.v3.oas.models.OpenAPI;
import lombok.extern.slf4j.Slf4j;
import org.datavaultplatform.common.event.RecordingEventSender;
import org.datavaultplatform.worker.app.DataVaultWorkerInstanceApp;
import org.datavaultplatform.worker.test.AddTestProperties;
import org.datavaultplatform.worker.test.TestClockConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = DataVaultWorkerInstanceApp.class)
@AddTestProperties
@Slf4j
@TestPropertySource(properties = {
        "worker.rabbit.enabled=false",
        "management.endpoints.web.exposure.include=*",
        "worker.security.enabled=true",
        "management.endpoints.web.base-path=/actuator",
        "management.endpoints.enabled-by-default=true",
        "management.health.rabbit.enabled=false"})
@AutoConfigureMockMvc
@Import(TestClockConfig.class)
public class OpenApiWorkerTest {

    @Autowired
    MockMvc mvc;

    @MockBean
    RecordingEventSender eventSender;

    @Autowired
    OpenAPI openApi;

    @Test
    void testOpenApi() {
        assertThat(openApi.getInfo().getTitle()).isEqualTo("DataVault Worker");
        assertThat(openApi.getInfo().getDescription()).isEqualTo("worker application");
    }

    @Test
    void testOpenApiAsJson() throws Exception {
        MvcResult mvcResult = mvc.perform(
                        get("http://localhost:8080/v3/api-docs"))
                .andExpect(content().contentTypeCompatibleWith("application/json"))
                .andExpect(status().is2xxSuccessful())
                .andExpect(jsonPath("$.info.title").value("DataVault Worker"))
                .andExpect(jsonPath("$.info.description").value("worker application"))
                .andExpect(jsonPath("$.info.version").value("v0.0.1"))
                .andExpect(jsonPath("$.paths").isEmpty())
                .andDo(print())
                .andReturn();

    }

    @Test
    void testOpenApiAsSwaggerUI() throws Exception {
        MvcResult mvcResult = mvc.perform(
                        get("http://localhost:8080/swagger-ui/index.html"))
                .andExpect(content().contentTypeCompatibleWith("text/html"))
                .andExpect(status().is2xxSuccessful())
                .andDo(print())
                .andReturn();
    }

}
