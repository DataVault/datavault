package org.datavaultplatform.webapp.ratelimited.api.systemclock;

import lombok.SneakyThrows;
import org.datavaultplatform.webapp.app.DataVaultWebApp;
import org.datavaultplatform.webapp.ratelimited.BaseIntervalApiEndpointTests;
import org.junit.jupiter.api.Disabled;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.concurrent.TimeUnit;

/**
 * This is a slower test using a real-time clock.
 * The same test using a simulated clock is at IntervalApiEndpointTestClockTests
 */
//@Disabled("This is a slower test using real time clock.")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = DataVaultWebApp.class)
public class IntervalApiEndpointSystemClockTests extends BaseIntervalApiEndpointTests {

    @Override
    @SneakyThrows
    public void advanceTimeBySeconds(int seconds) {
        TimeUnit.SECONDS.sleep(seconds);
    }

    @Override
    @SneakyThrows
    public void advanceTimeByMillis(long ms) {
        TimeUnit.MILLISECONDS.sleep(ms);
    }
}