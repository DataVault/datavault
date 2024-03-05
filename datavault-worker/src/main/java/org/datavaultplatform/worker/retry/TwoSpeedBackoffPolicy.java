package org.datavaultplatform.worker.retry;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.retry.RetryContext;
import org.springframework.retry.backoff.BackOffContext;
import org.springframework.retry.backoff.BackOffInterruptedException;
import org.springframework.retry.backoff.BackOffPolicy;

import java.util.function.Consumer;
import java.util.function.Supplier;

/*
 A custom back off policy which will attempt to perform a task (2 times N) times
 between first N attempts - will sleep delay1 ms during backoff.
 between Nth and final attempts - will sleep delay2 ms during backoff.
 Uses CountAttemptsBackoffContext to keep track of number of attempts made.
 Uses a lambda for the 'sleeping' part - this is to allow tests to override the actual sleep for faster tests.
 */
@Slf4j
public class TwoSpeedBackoffPolicy implements BackOffPolicy {
    private final String taskLabel;
    private final long firstDelayMs;
    private final long secondDelayMs;

    private final int firstDelayAttempts;
    private final Consumer<Long> sleeper;
    private final Supplier<CountAttemptsBackoffContext> ctxSupplier;

    public TwoSpeedBackoffPolicy(String taskLabel, long firstDelayMs, long secondDelayMs, int firstDelayAttempts, Consumer<Long> sleeper, Supplier<CountAttemptsBackoffContext> ctxSupplier) {
        this.taskLabel = taskLabel;
        this.firstDelayMs = firstDelayMs;
        this.secondDelayMs = secondDelayMs;
        this.firstDelayAttempts = firstDelayAttempts;
        this.sleeper = sleeper;
        this.ctxSupplier = ctxSupplier;
    }

    @Override
    public BackOffContext start(RetryContext context) {
        return ctxSupplier.get();
    }

    @Override
    public void backOff(BackOffContext backOffContext) throws BackOffInterruptedException {
        if (backOffContext instanceof CountAttemptsBackoffContext ) {
            CountAttemptsBackoffContext countingContext = (CountAttemptsBackoffContext) backOffContext;
            sleep(countingContext);
        } else {
            throw new RuntimeException(String.format("Unexpected type of BackOffContext[%s]",backOffContext.getClass().getName()));
        }
    }

    @SneakyThrows
    void sleep(CountAttemptsBackoffContext context) {
        int attemptsSoFar = ++context.attempts;
        long delayMs = attemptsSoFar < this.firstDelayAttempts ? firstDelayMs : secondDelayMs;
        context.totalDelay += delayMs;
        log.info("Task[{}] Backoff! attempts so far [{}], retry in [{}]", taskLabel, attemptsSoFar, DurationHelper.formatHoursMinutesSeconds(delayMs));
        sleeper.accept(delayMs);
    }


}
