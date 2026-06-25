package org.datavaultplatform.webapp.ratelimited;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.datavaultplatform.webapp.app.DataVaultWebApp;
import org.datavaultplatform.webapp.test.AddTestProperties;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest(classes = DataVaultWebApp.class, webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@Slf4j
@AutoConfigureMockMvc
@ActiveProfiles({"database","greedy"})
@TestPropertySource(properties = "logging.level.org.springframework.security=DEBUG")
@AddTestProperties
@Import({RateLimitedApiEndpointTestController.class,VaultsTestController.class})
@TestPropertySource(properties = {
        "spring.main.allow-bean-definition-overriding=true",
        "logging.level.io.github.bucket4j=debug",
        "logging.level.com.github.benmanes.caffeine=debug"
})
class NonRateLimitedApiEndPointTests {

    @Autowired
    private MockMvc mockMvc;

    @Nested
    class SecuredEndpointTests {

        static final String PROTECTED_URL = "/api/protected/greeting/{msg}";

        @Test
        @WithMockUser(username="testuser", roles = "USER")
        @SneakyThrows
        void testNotRateLimitedAuthenticated() {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            assertThat(authentication.getName()).isEqualTo("testuser");

            //we can make 20 requests, without delay, we are not rate limited
            for (int i = 0; i < 20; i++) {
                mockMvc.perform(get(PROTECTED_URL, String.valueOf(i + 1)))
                        .andExpect(status().isOk());
            }
        }

        /**
         * prove that the endpoint is protected by spring security.
         */
        @Test
        @SneakyThrows
        void testNotAuthenticated() {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            assertThat(authentication).isNull();
            mockMvc.perform(post(PROTECTED_URL, "oops"))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    class UnSecuredEndpointTests {

        static final String UN_PROTECTED_URL = "/actuator/health";

        @Test
        @SneakyThrows
        void testNotRateLimitedUnAuthenticated() {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            assertThat(authentication).isNull();

            //we can make 20 requests, without delay, we are not rate limited
            for (int i = 0; i < 20; i++) {
                mockMvc.perform(get(UN_PROTECTED_URL, String.valueOf(i + 1)))
                        .andExpect(status().isOk());
            }
        }
    }
}
