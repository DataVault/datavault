package org.datavaultplatform.webapp.actuator;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.datavaultplatform.webapp.app.DataVaultWebApp;
import org.datavaultplatform.webapp.config.ActutatorConfig;
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK, classes = {
                DataVaultWebApp.class,
                ActutatorConfig.class})
@AutoConfigureMockMvc
@Slf4j
@TestPropertySource(properties = {
        "spring.main.allow-bean-definition-overriding=true",
        "logging.level.io.github.bucket4j=debug",
        "logging.level.com.github.benmanes.caffeine=debug"
})
@ActiveProfiles("greedy")
class RateLimitedEndpointMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    RateLimitedEndpoint rateLimitedEndpoint;

    @Value("${management.endpoints.web.exposure.include}")
    String actuatorEndPointsToInclude;
    
    final String EXPECTED_JSON = """
            {
              "enabled": true,
              "cache": {
                "expiration": "PT1H",
                "maxSize": 10000
              },
              "filter": {
                "pathPatterns": [
                  "/vaults/isuun/{uun}",
                  "/vaults/autocompleteuun/{term}",
                  "/api/protected/limited/{msg}",
                  "/actuator/info"
                ]
              },
              "bandwidths": [
                {
                  "type": "GREEDY",
                  "capacity": 5,
                  "refillPeriod": {
                    "duration": "PT10S",
                    "type": "SECONDS"
                  },
                  "refillUnit": 5,
                  "durationType": "SECONDS",
                  "refillRate": 0.5
                }
              ]
            }
            """;

    @Test
    @SneakyThrows
    @WithMockUser(username = "actuatoruser", roles = "ACTUATOR")
    void testRateLimitedActuatorEndpoint() {

        assertThat(actuatorEndPointsToInclude)
                .satisfiesAnyOf(
                        s -> assertThat(s).isEqualTo("*"),
                        s -> assertThat(s).contains("ratelimited")
                );
        
        MvcResult result = mockMvc.perform(get("/actuator/ratelimited"))
                .andExpect(status().isOk()).andReturn();

        String body = result.getResponse().getContentAsString();

        JSONAssert.assertEquals(EXPECTED_JSON, body, JSONCompareMode.STRICT);
    }

    @Test
    @SneakyThrows
    @WithMockUser(username = "actuatoruser", roles = "ACTUATOR")
    void testMemoryInfoActuatorEndpoint() {
        mockMvc.perform(get("/actuator/memoryinfo"))
                .andExpect(status().isOk()).andDo(print());
    }

    @Test
    @SneakyThrows
    @WithMockUser(username = "actuatoruser", roles = "ACTUATOR")
    void testActuatorEndpoint() {
        MvcResult result = mockMvc.perform(get("/actuator"))
                .andExpect(status().isOk())
                .andDo(print()).andReturn();
        
        String body = result.getResponse().getContentAsString();
        assertThat(body).contains("ratelimited");
    }
}
