package org.datavaultplatform.worker.tasks;

import com.jcraft.jsch.SftpException;
import org.datavaultplatform.common.PropNames;
import org.datavaultplatform.worker.retry.TwoSpeedRetry;
import org.junit.jupiter.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

public class RetrieveTwoSpeedRetryTest {
    
    private static final Logger log = LoggerFactory.getLogger(RetrieveTwoSpeedRetryTest.class);

    @BeforeAll
    static void setupLogging() {
        RetryTestUtils.setLoggingLevelTrace(log);
    }
    @AfterAll
    static void tearDownLogging() {
        RetryTestUtils.resetLoggingLevel(log);
    }

    @Test
    void testRetrieveTwoSpeedRetry() {
        Map<String, String> properties = new HashMap<>();
        properties.put(PropNames.USER_FS_RETRY_DELAY_MS_1, "1000");
        properties.put(PropNames.USER_FS_RETRY_DELAY_MS_2, "1000");
        properties.put(PropNames.USER_FS_RETRY_MAX_ATTEMPTS, "4");
        TwoSpeedRetry retry = Retrieve.getUserFsTwoSpeedRetry(properties);

        AtomicInteger counter = new AtomicInteger(1);
        TwoSpeedRetry.DvRetryTemplate template = retry.getRetryTemplate(String.format("toUserFs - [%s]", "TEST!"));

        try {
            template.execute(retryContext -> {
                int attempt = counter.getAndIncrement();
                throw new SftpException(attempt, "oops" + attempt);
            });
            Assertions.fail("exception expected");
        } catch (Exception ex) {
            assertThat(ex.getMessage()).isEqualTo("task[toUserFs - [TEST!]]failed after[4] attempts");
            assertThat(ex.getCause().getClass()).isEqualTo(SftpException.class);
            assertThat(ex.getCause().toString()).isEqualTo("4: oops4");
            log.error("AFTER FOUR ATTEMPTS", ex);
        }
    }
    
    public static void main(String... args) {
        RetrieveTwoSpeedRetryTest test = new RetrieveTwoSpeedRetryTest();
        test.testRetrieveTwoSpeedRetry();
    }
}
