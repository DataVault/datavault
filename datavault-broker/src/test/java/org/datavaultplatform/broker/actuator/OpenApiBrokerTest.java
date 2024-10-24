package org.datavaultplatform.broker.actuator;

import io.swagger.v3.oas.models.OpenAPI;
import lombok.extern.slf4j.Slf4j;
import org.datavaultplatform.broker.app.DataVaultBrokerApp;
import org.datavaultplatform.broker.queue.Sender;
import org.datavaultplatform.broker.services.FileStoreService;
import org.datavaultplatform.broker.test.AddTestProperties;
import org.datavaultplatform.broker.test.BaseDatabaseTest;
import org.datavaultplatform.broker.test.TestClockConfig;
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

@SpringBootTest(classes = DataVaultBrokerApp.class)
@Import(TestClockConfig.class)
@AddTestProperties
@Slf4j
@TestPropertySource(properties = {
        "broker.email.enabled=true",
        "broker.controllers.enabled=true",
        "broker.initialise.enabled=true",
        "broker.rabbit.enabled=false",
        "broker.scheduled.enabled=false",
        "management.endpoints.web.exposure.include=*",
        "management.health.rabbit.enabled=false"})
@AutoConfigureMockMvc
public class OpenApiBrokerTest extends BaseDatabaseTest {

    @Autowired
    MockMvc mvc;

    @MockBean
    Sender sender;

    @MockBean
    FileStoreService mFileStoreService;

    @Autowired
    OpenAPI openApi;

    @Test
    void testOpenApi() {
        assertThat(openApi.getInfo().getTitle()).isEqualTo("DataVault Broker");
        assertThat(openApi.getInfo().getDescription()).isEqualTo("broker application");
    }

    @Test
    void testOpenApiAsJson() throws Exception {
        MvcResult mvcResult = mvc.perform(
                        get("http://localhost:8080/v3/api-docs"))
                .andExpect(content().contentTypeCompatibleWith("application/json"))
                .andExpect(status().is2xxSuccessful())
                .andExpect(jsonPath("$.openapi").value("3.0.1"))
                .andExpect(jsonPath("$.info.title").value("DataVault Broker"))
                .andExpect(jsonPath("$.info.description").value("broker application"))
                .andExpect(jsonPath("$.info.version").value("v0.0.1"))
                .andExpect(jsonPath("$.paths['/permissions/role']").exists())
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
