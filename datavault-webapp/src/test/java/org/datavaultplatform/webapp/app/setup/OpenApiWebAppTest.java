package org.datavaultplatform.webapp.app.setup;

import io.swagger.v3.oas.models.OpenAPI;
import lombok.extern.slf4j.Slf4j;
import org.datavaultplatform.webapp.test.ProfileDatabase;
import org.datavaultplatform.webapp.test.ProfileStandalone;
import org.datavaultplatform.webapp.test.TestClockConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestClockConfig.class)
@ProfileDatabase
@Slf4j
@TestPropertySource(properties = "management.endpoints.web.exposure.include=*")
public class OpenApiWebAppTest {

    @Autowired
    MockMvc mvc;

    @Autowired
    OpenAPI openApi;

    @Test
    void testOpenApi() {
        assertThat(openApi.getInfo().getTitle()).isEqualTo("DataVault WebApp");
        assertThat(openApi.getInfo().getDescription()).isEqualTo("webapp application");
    }

    @Test
    void testOpenApiAsJson() throws Exception {
        MvcResult mvcResult = mvc.perform(
                        get("http://localhost:8080/v3/api-docs"))
                .andExpect(content().contentTypeCompatibleWith("application/json"))
                .andExpect(status().is2xxSuccessful())
                .andExpect(jsonPath("$.openapi").value("3.0.1"))
                .andExpect(jsonPath("$.info.title").value("DataVault WebApp"))
                .andExpect(jsonPath("$.info.description").value("webapp application"))
                .andExpect(jsonPath("$.info.version").value("v0.0.1"))
                .andExpect(jsonPath("$.paths['/filestores/sftp']").exists())
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
