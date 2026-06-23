package org.datavaultplatform.webapp.ratelimited.api.testclock;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.datavaultplatform.webapp.app.DataVaultWebApp;
import org.datavaultplatform.webapp.config.ratelimited.ClockUtils;
import org.datavaultplatform.webapp.ratelimited.BaseIntervalApiEndpointTests;
import org.datavaultplatform.webapp.ratelimited.MutableTestClock;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes =
        {DataVaultWebApp.class, IntervalApiEndpointTestClockTests.TestClockConfig.class})
public class IntervalApiEndpointTestClockTests extends BaseIntervalApiEndpointTests {
    
    @Override
    @SneakyThrows
    public void advanceTimeBySeconds(int seconds) {
        if (clock instanceof MutableTestClock testClock) {
            Instant newInstant = testClock.instant().plus(Duration.ofSeconds(seconds));
            testClock.setInstant(newInstant);
            log.info("Advanced Clock to {}", newInstant);
        } else {
            throw new IllegalStateException("Clock is not an instance of MutableTestClock");
        }
    }

    @Override
    @SneakyThrows
    public void advanceTimeByMillis(long ms) {
        if (clock instanceof MutableTestClock testClock) {
            Instant newInstant = testClock.instant().plus(Duration.ofMillis(ms));
            testClock.setInstant(newInstant);
            log.info("Advanced Clock to {}", newInstant);
        } else {
            throw new IllegalStateException("Clock is not an instance of MutableTestClock");
        }
    }

    @TestConfiguration
    static class TestClockConfig {
        // By providing a @Primary Clock bean, we ensure that RateLimitConfig
        // and all other auto-configurations will use our test clock.
        @Bean
        @Primary
        public Clock clock() {
            return new MutableTestClock(Instant.parse("2026-06-01T10:00:00Z"));
        }
    }

    @Test
    void testBeanWiring() {
        assertThat(clock).isInstanceOf(MutableTestClock.class);
        MutableTestClock mutableClock = (MutableTestClock) clock;

        assertThat(mutableClock.instant()).isEqualTo(Instant.parse("2026-06-01T10:00:00Z"));

        long clockNanos = ClockUtils.toEpochNanos(clock);

        long timeMeterNanos = timeMeter.currentTimeNanos();
        assertThat(timeMeterNanos).isEqualTo(clockNanos);
    }
}