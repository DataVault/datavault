package org.datavaultplatform.worker.retry;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.retry.RetryCallback;
import org.springframework.retry.support.RetryTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Slf4j
public class TwoSpeedRetryTest {

    private static final ThreadLocal<CountAttemptsBackoffContext> CTX = ThreadLocal.withInitial(CountAttemptsBackoffContext::new);

    private static final long DELAY_MS_1 = 1000;
    private static final long DELAY_MS_2 = 2000;

    private static final int TOTAL_ATTEMPTS = 10;

    private TwoSpeedRetry templateHelper;

    @SneakyThrows
    void simulateSleep(long delayMs) {
        log.info("Simulate Sleeping for [{}]", DurationHelper.formatHoursMinutesSeconds(delayMs));
    }

    @BeforeEach
    void setup() {
        CTX.remove();
        templateHelper = new TwoSpeedRetry(TOTAL_ATTEMPTS, DELAY_MS_1, DELAY_MS_2 );
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 2, 3, 4, 5, 6, 7, 8, 10})
    void testSucceedsWhenNthAttemptSucceeds(int attemptWillSucceed) throws Exception {
        RetryTemplate template = templateHelper.getRetryTemplate("test-task-1",this::simulateSleep);
        String value = template.execute(willSucceedOnNthAttempt(attemptWillSucceed));
        assertThat(value).isEqualTo("succeededOn" + attemptWillSucceed);

    }

    @SuppressWarnings("CodeBlock2Expr")
    @Test
    void testFailsWhen10thAttemptFails() {
        CTX.remove();
        RetryTemplate template = templateHelper.getRetryTemplate("test-task-2",this::simulateSleep, CTX::get);
        assertThatThrownBy(() -> {
            template.execute(willSucceedOnNthAttempt(11));
        }).isInstanceOf(Exception.class).hasMessage("ATTEMPT [10] has failed");
        long expectedDelay = (4 * DELAY_MS_1) + (5 * DELAY_MS_2);
        assertThat(CTX.get().getTotalDelay()).isEqualTo(expectedDelay);
    }

    private RetryCallback<String, Exception> willSucceedOnNthAttempt(int attemptWillSucceed) {
        assertThat(attemptWillSucceed).isGreaterThan(0);
        return context -> {
            int attempts = context.getRetryCount() + 1;
            log.info("EXECUTE : attempt number[{}]", attempts);
            if (attempts < attemptWillSucceed) {
                throw new Exception(String.format("ATTEMPT [%d] has failed", attempts));
            }
            return "succeededOn" + attemptWillSucceed;
        };
    }
}
