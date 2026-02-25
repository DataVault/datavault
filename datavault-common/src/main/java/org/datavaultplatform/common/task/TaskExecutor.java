package org.datavaultplatform.common.task;

import org.datavaultplatform.common.util.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

public class TaskExecutor<T> {

    private static final Logger LOG = LoggerFactory.getLogger(TaskExecutor.class);
    private final int numThreads;
    private final String errorLabel;

    private final List<Callable<T>> tasks = new ArrayList<>();
    private final List<Callable<T>> origTasks = new ArrayList<>();

    private final AtomicBoolean executed = new AtomicBoolean(false);

    public TaskExecutor(int numThreads, String errorLabel) {
        this.numThreads = numThreads;
        this.errorLabel = errorLabel;
    }

    public synchronized void add(Callable<T> task) {
        Assert.isTrue(task != null, "Task cannot be null");
        if (executed.get()) {
            throw new IllegalStateException("Already executed");
        }
        if (origTasks.contains(task)) {
            throw new IllegalArgumentException("The task is already added");
        }
        origTasks.add(task);
        tasks.add(wrap(task));
    }

    private Callable<T> wrap(Callable<T> task) {
        TaskInterrupter.Checker checker = TaskInterrupter.getInterrupterCheck();
        TaskConfig config = TaskConfigTL.get();
        return () -> {
            TaskConfigTL.set(config);
            TaskInterrupter.setInterrupterCheck(checker);
            try {
                return task.call();
            } finally {
                TaskInterrupter.setInterrupterCheck(null);
                TaskConfigTL.reset();
            }
        };
    }

    public synchronized void execute() throws Exception {
        execute(result -> {});
    }

    public synchronized void execute(Consumer<T> consumer) throws Exception {
        this.execute(consumer, null);
    }

    public synchronized void execute(Consumer<T> consumer, Duration executorTimeout) throws Exception {
        if (executed.getAndSet(true)) {
            throw new IllegalStateException("Already executed");
        }

        ExecutorService service = Executors.newFixedThreadPool(numThreads);

        try {
            final List<Future<T>> futures;

            if (TaskConfigTL.get().isExecutorProperShutdownEnabled() && executorTimeout != null) {
                futures = service.invokeAll(tasks, executorTimeout.toMillis(), TimeUnit.MILLISECONDS);
            } else {
                futures = new ArrayList<>();
                for (Callable<T> task : tasks) {
                    futures.add(service.submit(task));
                }
            }

            service.shutdown();

            for (Future<T> future : futures) {
                // this might cause a cancellation exception if Future is cancelled because of TaskExecutor timeout
                getResultFromFuture(future, consumer);
            }

        } catch (InterruptedException e) {
            LOG.error("Main thread interrupted!");
            // Restore the status so the calling code knows we were interrupted
            Thread.currentThread().interrupt();
        } catch (CancellationException ce) {
            // we get here when Future.get() is called on Future that has been cancelled due to executorTimeout
            TimeoutException te = new TimeoutException("The executor [%s] has timed out after [%s]".formatted(errorLabel, executorTimeout));
            te.initCause(ce);
            throw te;
        } finally {
            handleShutdown(service);
        }
    }

    private void getResultFromFuture(Future<T> future, Consumer<T> consumer) throws Exception {
        try {
            T result = future.get(); // we have added per-tsm/dsmc timeouts in ProcessHelper
            consumer.accept(result);
        } catch (ExecutionException ee) {
            Utils.handleExecutionException(ee, errorLabel);
        }
    }

    private void handleShutdown(ExecutorService executor) {
        try {
            handleShutdownInternal(executor);
        } finally {
            LOG.warn("ExecutorService[{}]Terminated?[{}]", errorLabel, executor.isTerminated());}
    }

    private void handleShutdownInternal(ExecutorService executor) {
        LOG.info("IN: handleShutdownInternal");
        // If it's already fully closed, we're done.
        if (executor == null || executor.isTerminated()) return;

        if (!TaskConfigTL.get().isExecutorProperShutdownEnabled()) {
            return;
        }

        try {
            // Only call shutdown if it hasn't been called yet
            if (!executor.isShutdown()) {

                // shutdown indicates no more tasks coming and executor can clean up threads when current tasks are done
                executor.shutdown();
            }

            long executorPreShutdownNowMinutes = TaskConfigTL.get().getExecutorPreShutdownNowDuration().toMinutes();
            boolean success = executor.awaitTermination(executorPreShutdownNowMinutes, TimeUnit.MINUTES);
            LOG.info("Tasks Finished within [{}] minute timeout ? {}", executorPreShutdownNowMinutes, success);

        } catch (InterruptedException ie) {
            LOG.warn("Shutdown interrupted, forcing immediate termination.");
            Thread.currentThread().interrupt();
        } finally {
            // If the 5 minute passed OR we were interrupted, 
            // and tasks are STILL running, we kill them now.
            if (!executor.isTerminated()) {
                try {
                    // this is what will Interrupt any still running tasks and get them to stop
                    List<Runnable> notStarted = executor.shutdownNow();
                    LOG.warn("ExecutorService[{}]Terminated?[{}]. NotStartedCount[{}]", executor.isTerminated(), errorLabel, notStarted.size());
                    if (!executor.awaitTermination(TaskConfigTL.get().getExecutorShutdownDuration().getSeconds(), TimeUnit.SECONDS)) {
                        LOG.error("TaskExecutor [{}] did not terminate!", errorLabel);
                    }
                } catch (InterruptedException ex) {
                    executor.shutdownNow();
                    Thread.currentThread().interrupt();
                }
            }
        }
        LOG.info("OUT: handleShutdownInternal");
    }
}
