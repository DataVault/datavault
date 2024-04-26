package org.datavaultplatform.common.task;

import org.datavaultplatform.common.util.Utils;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

public class TaskExecutor<T> {

  private final int numThreads;
  private final String errorLabel;

  private final List<Callable<T>> tasks = new ArrayList<>();

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
    if (tasks.contains(task)) {
      throw new IllegalArgumentException("The task is already added");
    }
    tasks.add(task);
  }

  public synchronized void execute(Consumer<T> consumer) throws Exception {
    if (executed.getAndSet(true)) {
      throw new IllegalStateException("Already executed");
    }

    ExecutorService service = Executors.newFixedThreadPool(numThreads);

    List<Future<T>> futures = tasks.stream()
        .map(service::submit)
        .toList();

    service.shutdown();

    // service.awaitTermination(1, TimeUnit.MINUTES);

    for (Future<T> future : futures) {
      try {
        T result = future.get();
        consumer.accept(result);
      } catch (ExecutionException ee) {
        Utils.handleExecutionException(ee, errorLabel);
      }
    }

    service.shutdownNow();
  }
}
