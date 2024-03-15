package org.datavaultplatform.worker.retry;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.retry.RetryCallback;
import org.springframework.retry.RetryContext;
import org.springframework.retry.RetryListener;
import org.springframework.retry.backoff.BackOffPolicy;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.retry.support.RetryTemplateBuilder;
import org.springframework.util.Assert;

import java.util.function.BiConsumer;
import java.util.function.Supplier;

@Slf4j
public class TwoSpeedRetry {

    private final long delay1;
    private final long delay2;
    private final int totalNumberOfAttempts;

    public TwoSpeedRetry( int totalNumberOfAttempts, long delay1, long delay2) {
        this.totalNumberOfAttempts = totalNumberOfAttempts;
        Assert.isTrue(totalNumberOfAttempts > 0, "The totalNumber of attempts must be greater than zero");
        Assert.isTrue(totalNumberOfAttempts % 2 == 0, "The totalNumber of attempts must be even");
        this.delay1 = delay1;
        this.delay2 = delay2;
    }

    @SneakyThrows
    void defaultSleep(String taskLabel, long delayMs) {
        log.info("Task[{}] Sleeping for [{}]", taskLabel, DurationHelper.formatHoursMinutesSeconds(delayMs));
        Thread.sleep(delayMs);
    }

    public RetryTemplate getRetryTemplate(String taskLabel) {
        return getRetryTemplate(taskLabel, this::defaultSleep);
    }

    public RetryTemplate getRetryTemplate(String taskLabel, BiConsumer<String,Long> sleeper) {
        return getRetryTemplate( taskLabel, sleeper, CountAttemptsBackoffContext::new);
    }

    public RetryTemplate getRetryTemplate(String taskLabel, BiConsumer<String,Long> sleeper, Supplier<CountAttemptsBackoffContext> ctxSupplier) {
        BackOffPolicy bop = new TwoSpeedBackoffPolicy(taskLabel, delay1, delay2, totalNumberOfAttempts / 2, sleeper, ctxSupplier);
        RetryTemplate template = new RetryTemplateBuilder()
                .customBackoff(bop)
                .maxAttempts(totalNumberOfAttempts)
                .withListener(new CustomRetryListener(taskLabel))
                .build();
        template.setThrowLastExceptionOnExhausted(true);
        return template;
    }

    private static class CustomRetryListener implements RetryListener {

        private final String taskLabel;

        public CustomRetryListener(String taskLabel) {
            this.taskLabel = taskLabel;
        }

        @Override
        public <T, E extends Throwable> boolean open(RetryContext context, RetryCallback<T, E> callback) {
            log.info("Task[{}] Initial attempt", taskLabel);
            return true;
        }

        @Override
        public <T, E extends Throwable> void close(RetryContext context, RetryCallback<T, E> callback,
                                                   Throwable throwable) {
            boolean success = throwable == null;
            String outcome = success ? "Succeeded" : "Failed";
            int attempts = success ? context.getRetryCount() + 1 : context.getRetryCount();
            log.info("Task[{}] {} after [{}] attempts", taskLabel, outcome, attempts);
        }

        public <T, E extends Throwable> void onError(RetryContext retryContext, RetryCallback<T, E> retryCallback, Throwable throwable) {
        }

    }
}

