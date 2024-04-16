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
        Assert.isTrue(delay1 >= 0, "delay1 cannot be negative");
        Assert.isTrue(delay2 >= 0, "delay2 cannot be negative");
        this.delay1 = delay1;
        this.delay2 = delay2;
        log.info("delay1ms [{}]", delay1);
        log.info("delay2ms [{}]", delay2);
        log.info("totalNumberOfAttempts [{}]", totalNumberOfAttempts);
    }

    @SneakyThrows
    void defaultSleep(String taskLabel, long delayMs) {
        log.info("Task[{}] Sleeping for [{}]", taskLabel, DurationHelper.formatHoursMinutesSeconds(delayMs));
        Thread.sleep(delayMs);
    }

    public DvRetryTemplate getRetryTemplate(String taskLabel) {
        return getRetryTemplate(taskLabel, this::defaultSleep);
    }

    public DvRetryTemplate getRetryTemplate(String taskLabel, BiConsumer<String,Long> sleeper) {
        return getRetryTemplate( taskLabel, sleeper, CountAttemptsBackoffContext::new);
    }

    public DvRetryTemplate getRetryTemplate(String taskLabel, BiConsumer<String,Long> sleeper, Supplier<CountAttemptsBackoffContext> ctxSupplier) {
        BackOffPolicy bop = new TwoSpeedBackoffPolicy(taskLabel, delay1, delay2, totalNumberOfAttempts / 2, sleeper, ctxSupplier);
        RetryTemplate template = new RetryTemplateBuilder()
                .customBackoff(bop)
                .maxAttempts(totalNumberOfAttempts)
                .withListener(new CustomRetryListener(totalNumberOfAttempts, taskLabel))
                .build();
        template.setThrowLastExceptionOnExhausted(true);
        log.info("created RetryTemplate [{}]", taskLabel);
        return new DvRetryTemplate(taskLabel, totalNumberOfAttempts, template);
    }

    public static class DvRetryTemplate {

        private final String taskLabel;
        private final int attempts;
        private final RetryTemplate template;

        public DvRetryTemplate(String taskLabel, int attempts, RetryTemplate retryTemplate) {
            this.attempts = attempts;
            this.taskLabel = taskLabel;
            this.template = retryTemplate;
        }

        public final <T> T execute(RetryCallback<T, Exception> retryCallback) throws Exception {
            try {
                return template.execute(retryCallback);
            } catch (Exception ex) {
                String msg = String.format("task[%s]failed after[%s] attempts", taskLabel, attempts);
                throw new DvRetryException(msg, ex);
            }
        }
    }

    private static class CustomRetryListener implements RetryListener {

        private final String taskLabel;
        private final int maxAttempts;

        public CustomRetryListener(int maxAttempts, String taskLabel) {
            this.taskLabel = taskLabel;
            this.maxAttempts = maxAttempts;
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
            Throwable th = retryContext.getLastThrowable();
            if (log.isTraceEnabled()) {
                log.trace("Task[{}] attempt[{}/{}] failed", taskLabel, retryContext.getRetryCount(), maxAttempts, th);
            } else {
                log.warn("Task[{}] attempt[{}/{}] failed : Throwable[{}]msg[{}]", taskLabel, retryContext.getRetryCount(), maxAttempts, th.getClass().getSimpleName(), th.toString());
            }
        }

    }
}

