package org.datavaultplatform.common.storage.impl;

import lombok.Getter;
import org.datavaultplatform.common.util.ProcessInfo;
import org.datavaultplatform.common.util.ProcessInfoExitStatusSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.retry.RetryCallback;
import org.springframework.retry.RetryContext;
import org.springframework.retry.RetryListener;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.util.Assert;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.TimeoutException;

/**
 * Abstracts away the pain of using Spring's Retry template with ProcessHelper/ProcessInfo which we use to run Unix processes.
 * Developed to work with "TivoliStorageManager.delete" - can be used in other places.
 * Handles the case where a unix process times out which would cause a retry
 * Handles the case where a unix process returns a non-zero error code and would cause a retry.
 * Handles the case where you want to provide a function that takes a ProcessInfo with process exitCode to determine whether to retry
 * Remember: not all non-zero exit codes from "TSM's dsmc command" are errors - some are warnings.
 * The use of ProcessInfoFactory is mainly used to help make this class flexible enough to work with existing tests.
 */
public class TSMProcessRetrier {

    private final String description;
    private static final Logger LOG = LoggerFactory.getLogger(TSMProcessRetrier.class);
    private final int maxRetries;
    private final String[] osCommand;
    private final int retryTimeSeconds;
    private final ProcessInfoExitStatusSupport processInfoExitStatusSupport;
    private final ProcessInfoFactory processInfoFactory;

    public TSMProcessRetrier(String description, int maxRetries, int retryTimeSeconds, ProcessInfoFactory processInfoFactory, String... osCommand) {
        this(description, maxRetries, retryTimeSeconds, processInfoFactory, ProcessInfoExitStatusSupport.DEFAULT, osCommand);
    }

    public TSMProcessRetrier(String description, int maxRetries, int retryTimeSeconds, ProcessInfoFactory processInfoFactory, ProcessInfoExitStatusSupport processInfoExitStatusSupport, String... osCommand) {
        Assert.isTrue(maxRetries >= 1, "The maxRetries cannot be less than 1");
        this.description = description;
        this.maxRetries = maxRetries;
        this.retryTimeSeconds = retryTimeSeconds;
        this.osCommand = osCommand;
        this.processInfoExitStatusSupport = processInfoExitStatusSupport;
        this.processInfoFactory = processInfoFactory;
    }

    public void execute() throws Exception {
        RetryTemplate template = RetryTemplate.builder().maxAttempts(maxRetries).fixedBackoff(Duration.ofSeconds(retryTimeSeconds)) // Delay between retries
                .retryOn(List.of(TimeoutException.class, ProcessInfoException.class)) // we should retry on TimeoutException as well as ProcessInfpException
                .withListener(new ProcessInfoRetryListener(description)).build();

        template.execute(
                // this callback is called on every attempt
                retryContext -> {
                    LOG.info("Executing TSM Delete (Attempt {})", retryContext.getRetryCount() + 1);

                    return processInfoFactory.createProcessinfo(description, osCommand);

                }, // this callback is called at the very end
                retryContext -> {
                    Throwable lastError = retryContext.getLastThrowable();

                    /*
                     we use ProcessInfoException to get non-success ProcessInfo to cause retry 
                     - but at the end of retrying we want to convert final ProcessInfoException back into ProcessInfo
                     */
                    if (lastError instanceof ProcessInfoException pie) {
                        return pie.getProcessInfo();
                    }

                    if (lastError instanceof Exception e) {
                        throw e;
                    } else if (lastError instanceof Error err) {
                        throw err;
                    } else {
                        // Fallback for types that are technically Throwable but 
                        // neither Exception nor Error (extremely rare)
                        throw new RuntimeException("Fatal error during retry", lastError);
                    }
                });
    }

    @FunctionalInterface
    public interface ProcessInfoFactory {
        ProcessInfo createProcessinfo(String desc, String... commands) throws Exception;
    }

    /**
     * This class is required because Retry template is Exception based.
     * If we get a ProcessInfo with an exitCode that we want to cause a retry - we have to throw some kind of exception.
     */
    public static class ProcessInfoException extends RuntimeException {
        @Getter
        private final ProcessInfo processInfo;

        public ProcessInfoException(ProcessInfo processInfo, String message) {
            super(message);
            this.processInfo = processInfo;
        }
    }

    public class ProcessInfoRetryListener implements RetryListener {
        private final String description;

        public ProcessInfoRetryListener(String description) {
            this.description = description;
        }

        /**
         * onSuccess - the name might be misleading - it means we got a result.
         * We look at that result - if it's a ProcessInfo - we check to consider whether it represents true success.
         * If the ProcessIinfo does not represent true success, we throw ProcessInfoException. This would force a retry.
         */
        @Override
        public <T, E extends Throwable> void onSuccess(RetryContext context, RetryCallback<T, E> callback, T result) {
            if (result instanceof ProcessInfo info) {
                if (processInfoExitStatusSupport.isProcessInfoFailure(info)) {
                    throw new ProcessInfoException(info, "Retry trigger: Exit code [%s]".formatted(info.getExitValue()));
                } else {
                    LOG.info("Process [{}] was Successful.", description);
                }
            }
        }

        /*
         * This runs after EVERY failed attempt - just logging
         */
        @Override
        public <T, E extends Throwable> void onError(RetryContext context, RetryCallback<T, E> callback, Throwable throwable) {
            int attempt = context.getRetryCount();
            LOG.warn("Attempt [{}/{}] failed for [{}]. Reason: {}. Waiting for next retry...", attempt, maxRetries, description, throwable.getMessage());
        }

        /*
         * For when we've exhausted retries, and we want to log stuff
         */
        @Override
        public <T, E extends Throwable> void close(RetryContext context, RetryCallback<T, E> callback, Throwable throwable) {
            // Check if we actually reached the limit without a successful result
            if (context.getRetryCount() >= maxRetries || context.isExhaustedOnly() || throwable != null) {
                LOG.info("Process for [{}] was skipped after multiple [{}] attempts.", description, context.getRetryCount());
                LOG.error("All [{}] attempts exhausted for [{}]. Final error: {}", context.getRetryCount(), description, throwable.getMessage());
            }
        }
    }
}
