package org.datavaultplatform.webapp.ratelimited;

import lombok.extern.slf4j.Slf4j;

import org.datavaultplatform.webapp.test.AddTestProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
@AutoConfigureMockMvc
@ActiveProfiles({"database","interval"})
@TestPropertySource(properties = "logging.level.org.springframework.security=DEBUG")
@AddTestProperties
@Import({RateLimitedApiEndpointTestController.class,VaultsTestController.class})
@TestPropertySource(properties = {
        "spring.main.allow-bean-definition-overriding=true",
        "logging.level.io.github.bucket4j=debug",
        "logging.level.com.github.benmanes.caffeine=debug"
})
public abstract class BaseIntervalApiEndpointTests extends BaseApiEndpointTests{
    
    @Value("${spring.application.name}")
    private String applicationName;
    
    @BeforeEach
    void setup() {
        assertThat(applicationName).isEqualTo("ratelimited-interval");
        
        if (clock instanceof MutableTestClock testClock) {
            testClock.setInstant(Instant.parse("2026-06-01T10:00:00Z"));
        }
        log.info("Resetting Clock to {}", clock.instant());
    }

    @Test
    @Override
    void testRateLimitedUnAuthenticatedEndpoint() {

        // Requests 1-5: Should be allowed.
        for (int i = 1; i <= 5; i++) {
            log.info("Making request #{}", i);
            checkBeforeAfterUnAuth(6-i, 5-i, String.valueOf(i));
            advanceTimeBySeconds(1); // Advance time by 1s between requests
        }
        // Request 6: Should be rate-limited (429 Too Many Requests).
        log.info("Making request #6, expecting it to be rate-limited");
        super.checkUnAuthUrlIsTooManyRequests("6");

        // Advance the clock past the 10-second window.
        log.info("Advancing time past the rate-limit window");
        advanceTimeBySeconds(11);

        checkBeforeAfterUnAuth(5,4, "7");
    }

    @Test
    @Override
    @WithMockUser(AUTHENTICATED_USER)
    void testRateLimitedAuthenticatedEndpoint() {

        // Requests 1-5: Should be allowed.
        for (int i = 1; i <= 5; i++) {
            log.info("Making request #{}", i);
            checkBeforeAfterAuthenticated(6-i, 5-i, String.valueOf(i));
            advanceTimeBySeconds(1); // Advance time by 1s between requests
        }
        // Request 6: Should be rate-limited (429 Too Many Requests).
        log.info("Making request #6, expecting it to be rate-limited");
        super.checkAuthUrlIsTooManyRequests("6");

        // Advance the clock past the 10-second window.
        log.info("Advancing time past the rate-limit window");
        advanceTimeBySeconds(11);

        checkBeforeAfterAuthenticated(5,4, "7");
    }

    @Test
    @Override
    @WithMockUser(AUTHENTICATED_USER)
    void testRateLimitedAuthenticatedVaultAutoCompleteEndpoint() {

        // Requests 1-5: Should be allowed.
        for (int i = 1; i <= 5; i++) {
            log.info("Making request #{}", i);
            checkBeforeAfterAuthenticatedAutocomplete(6-i, 5-i, String.valueOf(i));
            advanceTimeBySeconds(1); // Advance time by 1s between requests
        }
        // Request 6: Should be rate-limited (429 Too Many Requests).
        log.info("Making request #6, expecting it to be rate-limited");
        super.checkAuthVaultAutocompleteUrlIsTooManyRequests("6");

        // Advance the clock past the 10-second window.
        log.info("Advancing time past the rate-limit window");
        advanceTimeBySeconds(11);

        checkBeforeAfterAuthenticatedAutocomplete(5,4, "7");
    }

    @Test
    @Override
    @WithMockUser(AUTHENTICATED_USER)
    void testRateLimitedAuthenticatedVaultIsUunEndpoint() {

        // Requests 1-5: Should be allowed.
        for (int i = 1; i <= 5; i++) {
            log.info("Making request #{}", i);
            checkBeforeAfterAuthenticatedisUun(6-i, 5-i, String.valueOf(i));
            advanceTimeBySeconds(1); // Advance time by 1s between requests
        }
        // Request 6: Should be rate-limited (429 Too Many Requests).
        log.info("Making request #6, expecting it to be rate-limited");
        super.checkAuthVaultIsUunUrlIsTooManyRequests("6");

        // Advance the clock past the 10-second window.
        log.info("Advancing time past the rate-limit window");
        advanceTimeBySeconds(11);

        checkBeforeAfterAuthenticatedisUun(5,4, "7");
    }

    public abstract void advanceTimeBySeconds(int seconds);

    public abstract void advanceTimeByMillis(long ms);
}