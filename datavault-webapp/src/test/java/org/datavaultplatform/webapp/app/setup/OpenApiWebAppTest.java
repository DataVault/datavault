package org.datavaultplatform.webapp.app.setup;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.swagger.v3.oas.models.OpenAPI;
import lombok.extern.slf4j.Slf4j;
import org.datavaultplatform.webapp.controllers.groups.GroupVaultsController;
import org.datavaultplatform.webapp.services.RestService;
import org.datavaultplatform.common.actuator.WithMockActuatorUser;
import org.datavaultplatform.webapp.test.ProfileDatabase;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.io.FileWriter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ProfileDatabase
@Slf4j
@TestPropertySource(properties = "management.endpoints.web.exposure.include=*")
class OpenApiWebAppTest {
    
    @MockBean
    RestService restService;
    
    @Autowired
    MockMvc mvc;

    @Autowired
    OpenAPI openApi;
    
    @Autowired
    GroupVaultsController groupVaultsController;

    @Test
    void testOpenApi() {
        assertThat(openApi.getInfo().getTitle()).isEqualTo("DataVault WebApp API");
        assertThat(openApi.getInfo().getDescription()).isEqualTo("API documentation for the DataVault WebApp module.");
    }

    @Test
    @WithMockActuatorUser
    void testOpenApiAsJson() throws Exception {
        MvcResult mvcResult = mvc.perform(
                        get("/v3/api-docs"))
                .andExpect(content().contentTypeCompatibleWith("application/json"))
                .andExpect(status().is2xxSuccessful())
                .andExpect(jsonPath("$.openapi").value("3.1.0"))
                .andExpect(jsonPath("$.info.title").value("DataVault WebApp API"))
                .andExpect(jsonPath("$.info.description").value("API documentation for the DataVault WebApp module."))
                .andExpect(jsonPath("$.info.version").exists())
                .andExpect(jsonPath("$.paths['/filestores/sftp']").exists())
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
        try (FileWriter fw = new FileWriter("../SWAGGER_OPENAPI/datavault-webapp-openapi.yaml")) {
            fw.write(body);
        }
        System.out.println(body);
    }

    @Test
    @WithMockActuatorUser
    void testSpringMvcMappings() throws Exception {
        MvcResult mvcResult = mvc.perform(
                        get("/actuator/mappings"))
                .andExpect(status().is2xxSuccessful())
                //.andDo(print())
                .andReturn();
        ObjectMapper mapper = new ObjectMapper();

        JsonNode rootNode = mapper.readTree(mvcResult.getResponse().getContentAsString());
        JsonNode dispatcherServletsNode = rootNode.path("contexts").path("datavault-webapp").path("mappings").path("dispatcherServlets").path("dispatcherServlet");

        ArrayNode predicates = mapper.createArrayNode();
        
        if (dispatcherServletsNode.isArray()) {
            ArrayNode filteredMappings = mapper.createArrayNode();
            for (JsonNode mappingNode : dispatcherServletsNode) {
                JsonNode patternsNode = mappingNode.path("details").path("requestMappingConditions").path("patterns");
                boolean exclude = false;
                if (patternsNode.isArray()) {
                    for (JsonNode pattern : patternsNode) {
                        String path = pattern.asText();
                        if (path.startsWith("/actuator") || path.startsWith("/swagger-ui") || path.startsWith("/v3/api-docs")) {
                            exclude = true;
                            break;
                        }
                    }
                }
                if (!exclude) {
                    JsonNode predicate = mappingNode.get("predicate");
                    if(predicate != null){
                        predicates.add(predicate);
                    }
                    filteredMappings.add(mappingNode);
                }
            }
            System.out.println(filteredMappings.size());
            // Replace the original dispatcherServlet array with the filtered one
            ((ObjectNode)rootNode.path("contexts").path("datavault-webapp").path("mappings").path("dispatcherServlets")).replace("dispatcherServlet", filteredMappings);
            assertThat(filteredMappings.size()).isGreaterThan(0);
        }

        String prettyJson = mapper.writerWithDefaultPrettyPrinter()
                .writeValueAsString(predicates);

        System.out.println(prettyJson);
        
    }

    @Test
    @WithMockUser(username = "actuator-user", roles = {"ACTUATOR"})
    void testOpenApiAsSwaggerUI() throws Exception {
        MvcResult mvcResult = mvc.perform(
                        get("/swagger-ui/index.html"))
                .andExpect(content().contentTypeCompatibleWith("text/html"))
                .andExpect(status().is2xxSuccessful())
                .andDo(print())
                .andReturn();
    }
}
