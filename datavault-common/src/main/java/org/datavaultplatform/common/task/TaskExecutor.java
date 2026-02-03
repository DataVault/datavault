package org.datavaultplatform.common.task;

import org.datavaultplatform.common.util.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

public class TaskExecutor<T> {

    private static final Logger LOG = LoggerFactory.getLogger(TaskExecutor.class);
    private static final long TIMEOUT_MINUTES = 5;
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
    return () -> {
      TaskInterrupter.setInterrupterCheck(checker);
      try {
        return task.call();
      } finally {
        TaskInterrupter.setInterrupterCheck(null);
      }
    };
  }

  public synchronized void execute() throws Exception {
      execute(result -> {});
  }

    public synchronized void execute(Consumer<T> consumer) throws Exception {
        if (executed.getAndSet(true)) {
            throw new IllegalStateException("Already executed");
        }

        ExecutorService service = Executors.newFixedThreadPool(numThreads);

        List<Future<T>> futures = tasks.stream()
                .map(service::submit)
                .toList();
        try {
            // shutdown - prevents the submission of further Callable Tasks
            service.shutdown();

            for (Future<T> future : futures) {
                getResultFromFuture(future, consumer);
            }

        } catch (InterruptedException e) {
            LOG.error("Main thread interrupted!");
            // Restore the status so the calling code knows we were interrupted
            Thread.currentThread().interrupt();
        } finally {
            handleShutdown(service);
        }
    }

    private void getResultFromFuture(Future<T> future, Consumer<T> consumer) throws Exception {
        try {
            T result = future.get();
            consumer.accept(result);
        } catch (ExecutionException ee) {
            Utils.handleExecutionException(ee, errorLabel);
        }
    }

    private void handleShutdown(ExecutorService executor) {
        // If it's already fully closed, we're done.
        if (executor == null || executor.isTerminated()) return;

        try {
            // Only call shutdown if it hasn't been called yet
            if (!executor.isShutdown()) {
                executor.shutdown();
            }

            boolean success = executor.awaitTermination(TIMEOUT_MINUTES, TimeUnit.MINUTES);
            LOG.info("Tasks Finished within [{}] minute timeout ? {}", TIMEOUT_MINUTES, success);

        } catch (InterruptedException ie) {
            LOG.warn("Shutdown interrupted, forcing immediate termination.");
            Thread.currentThread().interrupt();
        } finally {
            // If the 5 minute passed OR we were interrupted, 
            // and tasks are STILL running, we kill them now.
            if (!executor.isTerminated()) {
                List<Runnable> notStarted = executor.shutdownNow();
                LOG.warn("ExecutorService[{}]Terminated. NotStartedCount[{}]", errorLabel, notStarted.size());
            }
        }
    }
}
