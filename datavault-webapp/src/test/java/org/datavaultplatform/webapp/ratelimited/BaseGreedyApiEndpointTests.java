package org.datavaultplatform.webapp.ratelimited;

import lombok.extern.slf4j.Slf4j;
import org.datavaultplatform.webapp.test.AddTestProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

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
public abstract class BaseGreedyApiEndpointTests extends BaseApiEndpointTests{

    @Value("${spring.application.name}")
    private String applicationName;

    @BeforeEach
    void setup() {
        assertThat(applicationName).isEqualTo("ratelimited-greedy");
        assertThat(proxyManager).isNotNull();
        
        if (clock instanceof MutableTestClock testClock) {
            testClock.setInstant(Instant.parse("2026-06-01T10:00:00Z"));
        }
        log.info("Resetting Clock to {}", clock.instant());
    }
    

    @Test
    @Override
    void testRateLimitedUnAuthenticatedEndpoint() {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        assertThat(auth).isNull();

        // Your configuration allows 5 requests per 10 seconds.

        checkBeforeAfterUnAuth(5, 4, "one");
        advanceTimeBySeconds(1);

        checkBeforeAfterUnAuth(4, 3, "two");
        advanceTimeBySeconds(1);

        //after 2 seconds - we get another token
        checkBeforeAfterUnAuth(4, 3, "three");
        advanceTimeBySeconds(1);

        checkBeforeAfterUnAuth(3, 2, "four");
        advanceTimeBySeconds(1);

        //after 4 seconds - we get another token
        checkBeforeAfterUnAuth(3, 2, "five");
        advanceTimeBySeconds(1);

        checkBeforeAfterUnAuth(2, 1, "six");
        advanceTimeBySeconds(1);

        //after 6 seconds - we get another token
        checkBeforeAfterUnAuth(2, 1, "seven");
        advanceTimeBySeconds(1);

        checkBeforeAfterUnAuth(1, 0, "eight");
        advanceTimeBySeconds(1);

        //after 10 seconds - we get another token
        checkBeforeAfterUnAuth(1, 0, "nine");
        advanceTimeBySeconds(1);

        assertThat(getUnAuthBucketProxy(UN_AUTH_URL).getAvailableTokens()).isZero();
        checkUnAuthUrlIsTooManyRequests("ten");
    }

    @Test
    @Override
    @WithMockUser(AUTHENTICATED_USER)
    void testRateLimitedAuthenticatedEndpoint() {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        assertThat(auth.getName()).isEqualTo(AUTHENTICATED_USER);
                
        // Your configuration allows 5 requests per 10 seconds.

        checkBeforeAfterAuthenticated(5, 4, "one");
        advanceTimeBySeconds(1);

        checkBeforeAfterAuthenticated(4, 3, "two");
        advanceTimeBySeconds(1);

        //after 2 seconds - we get another token
        checkBeforeAfterAuthenticated(4, 3, "three");
        advanceTimeBySeconds(1);

        checkBeforeAfterAuthenticated(3, 2, "four");
        advanceTimeBySeconds(1);

        //after 4 seconds - we get another token
        checkBeforeAfterAuthenticated(3, 2, "five");
        advanceTimeBySeconds(1);

        checkBeforeAfterAuthenticated(2, 1, "six");
        advanceTimeBySeconds(1);

        //after 6 seconds - we get another token
        checkBeforeAfterAuthenticated(2, 1, "seven");
        advanceTimeBySeconds(1);

        checkBeforeAfterAuthenticated(1, 0, "eight");
        advanceTimeBySeconds(1);

        //after 10 seconds - we get another token
        checkBeforeAfterAuthenticated(1, 0, "nine");
        advanceTimeBySeconds(1);

        assertThat(getAuthBucketProxy(AUTH_URL).getAvailableTokens()).isZero();
        checkAuthUrlIsTooManyRequests("ten");
    }

    @Test
    @Override
    @WithMockUser(AUTHENTICATED_USER)
    void testRateLimitedAuthenticatedVaultAutoCompleteEndpoint() {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        assertThat(auth.getName()).isEqualTo(AUTHENTICATED_USER);

        // Your configuration allows 5 requests per 10 seconds.

        checkBeforeAfterAuthenticatedAutocomplete(5, 4, "one");
        advanceTimeBySeconds(1);

        checkBeforeAfterAuthenticatedAutocomplete(4, 3, "two");
        advanceTimeBySeconds(1);

        //after 2 seconds - we get another token
        checkBeforeAfterAuthenticatedAutocomplete(4, 3, "three");
        advanceTimeBySeconds(1);

        checkBeforeAfterAuthenticatedAutocomplete(3, 2, "four");
        advanceTimeBySeconds(1);

        //after 4 seconds - we get another token
        checkBeforeAfterAuthenticatedAutocomplete(3, 2, "five");
        advanceTimeBySeconds(1);

        checkBeforeAfterAuthenticatedAutocomplete(2, 1, "six");
        advanceTimeBySeconds(1);

        //after 6 seconds - we get another token
        checkBeforeAfterAuthenticatedAutocomplete(2, 1, "seven");
        advanceTimeBySeconds(1);

        checkBeforeAfterAuthenticatedAutocomplete(1, 0, "eight");
        advanceTimeBySeconds(1);

        //after 10 seconds - we get another token
        checkBeforeAfterAuthenticatedAutocomplete(1, 0, "nine");
        advanceTimeBySeconds(1);

        assertThat(getAuthBucketProxy(VAULT_AUTOCOMPLETE_URL).getAvailableTokens()).isZero();
        checkAuthVaultAutocompleteUrlIsTooManyRequests("ten");
    }

    @Test
    @Override
    @WithMockUser(AUTHENTICATED_USER)
    void testRateLimitedAuthenticatedVaultIsUunEndpoint() {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        assertThat(auth.getName()).isEqualTo(AUTHENTICATED_USER);

        // Your configuration allows 5 requests per 10 seconds.

        checkBeforeAfterAuthenticatedisUun(5, 4, "one");
        advanceTimeBySeconds(1);

        checkBeforeAfterAuthenticatedisUun(4, 3, "two");
        advanceTimeBySeconds(1);

        //after 2 seconds - we get another token
        checkBeforeAfterAuthenticatedisUun(4, 3, "three");
        advanceTimeBySeconds(1);

        checkBeforeAfterAuthenticatedisUun(3, 2, "four");
        advanceTimeBySeconds(1);

        //after 4 seconds - we get another token
        checkBeforeAfterAuthenticatedisUun(3, 2, "five");
        advanceTimeBySeconds(1);

        checkBeforeAfterAuthenticatedisUun(2, 1, "six");
        advanceTimeBySeconds(1);

        //after 6 seconds - we get another token
        checkBeforeAfterAuthenticatedisUun(2, 1, "seven");
        advanceTimeBySeconds(1);

        checkBeforeAfterAuthenticatedisUun(1, 0, "eight");
        advanceTimeBySeconds(1);

        //after 10 seconds - we get another token
        checkBeforeAfterAuthenticatedisUun(1, 0, "nine");
        advanceTimeBySeconds(1);

        assertThat(getAuthBucketProxy(VAULT_ISUUN_URL).getAvailableTokens()).isZero();
        checkAuthVaultIsUunUrlIsTooManyRequests("ten");
    }
}