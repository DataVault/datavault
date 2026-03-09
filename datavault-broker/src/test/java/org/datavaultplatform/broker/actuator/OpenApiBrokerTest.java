package org.datavaultplatform.broker.actuator;

import io.swagger.v3.oas.models.OpenAPI;
import lombok.extern.slf4j.Slf4j;
import org.datavaultplatform.broker.app.DataVaultBrokerApp;
import org.datavaultplatform.broker.services.AdminDepositService;
import org.datavaultplatform.broker.queue.TaskSender;
import org.datavaultplatform.broker.services.FileStoreService;
import org.datavaultplatform.broker.test.AddTestProperties;
import org.datavaultplatform.broker.test.BaseDatabaseTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.io.FileWriter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = DataVaultBrokerApp.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
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
class OpenApiBrokerTest extends BaseDatabaseTest {

    @Autowired
    MockMvc mvc;

    @MockBean
    TaskSender taskSender;

    @MockBean
    FileStoreService mFileStoreService;
    
    @MockBean
    AdminDepositService mAdminDepositService;

    @Autowired
    OpenAPI openApi;

    @Test
    void testOpenApi() {
        assertThat(openApi.getInfo().getTitle()).isEqualTo("DataVault Broker API");
        assertThat(openApi.getInfo().getDescription()).isEqualTo("""
                API documentation for the DataVault Broker module.
                All endpoints require authentication via the X-UserID and X-Client-Key headers.""");
    }

    @Test
    void testOpenApiAsJson() throws Exception {
        MvcResult mvcResult = mvc.perform(
                        get("/v3/api-docs"))
                .andExpect(content().contentTypeCompatibleWith("application/json"))
                .andExpect(status().is2xxSuccessful())
                .andExpect(jsonPath("$.openapi").value("3.1.0"))
                .andExpect(jsonPath("$.info.title").value("DataVault Broker API"))
                .andExpect(jsonPath("$.info.description").value("""
                    API documentation for the DataVault Broker module.
                    All endpoints require authentication via the X-UserID and X-Client-Key headers.
                    """.stripTrailing()))
                .andExpect(jsonPath("$.info.version").exists())
                .andExpect(jsonPath("$.paths['/permissions/role']").exists())
                .andDo(print())
                .andReturn();
    }

    @Test
    @EnabledIfSystemProperty(named = "generate.swagger.doc", matches = "true")
    @EnabledOnOs(OS.MAC)
    void testOpenApiAsYaml() throws Exception {
        MvcResult mvcResult = mvc.perform(
                        get("/v3/api-docs.yaml"))
                .andExpect(content().contentTypeCompatibleWith("application/vnd.oai.openapi;charset=UTF-8"))
                .andExpect(status().is2xxSuccessful())
                //.andDo(print())
                .andReturn();
        String body = mvcResult.getResponse().getContentAsString();
        try (FileWriter fw = new FileWriter("../SWAGGER_OPENAPI/datavault-broker-openapi.yaml")) {
            fw.write(body);
        }
        System.out.println(body);
    }

    @Test
    void testOpenApiAsSwaggerUI() throws Exception {
        MvcResult mvcResult = mvc.perform(
                        get("/swagger-ui/index.html"))
                .andExpect(content().contentTypeCompatibleWith("text/html"))
                .andExpect(status().is2xxSuccessful())
                .andDo(print())
                .andReturn();
    }


}
