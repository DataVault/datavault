package org.datavaultplatform.common.task;


import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

public class TaskExecutorTest {

  @Test
  void testSingleExecution() throws Exception {
    TaskExecutor<String> executor = new TaskExecutor<>(1, "Error");
    executor.add(() -> "one");
    executor.add(() -> "two");
    executor.execute(System.out::println);
    IllegalStateException ex = assertThrows(IllegalStateException.class,
        () -> executor.execute(System.out::println));
    assertEquals("Already executed", ex.getMessage());
  }

  @Test
  void testCannotAddAfterExecution() throws Exception {
    TaskExecutor<String> executor = new TaskExecutor<>(1, "Error");
    executor.add(() -> "one");
    executor.execute(System.out::println);
    IllegalStateException ex = assertThrows(IllegalStateException.class,
        () -> executor.add(() -> "two"));
    assertEquals("Already executed", ex.getMessage());
  }

  @Test
  void testCannotAddSameTaskTwice() {
    Callable<String> task = () -> "one";
    TaskExecutor<String> executor = new TaskExecutor<>(1, "Error");
    executor.add(task);
    IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
        () -> executor.add(task));
    assertEquals("The task is already added", ex.getMessage());
  }


  @Test
  void testTaskExecutionException() {
    TaskExecutor<String> executor = new TaskExecutor<>(1, "Error");
    executor.add(() -> {
      throw new IOException("bob123");
    });
    IOException ex = assertThrows(IOException.class,
        () -> executor.execute(System.out::println));
    assertEquals("bob123", ex.getMessage());
  }

  @Test
  void testNullTasksRejected() {
    TaskExecutor<String> executor = new TaskExecutor<>(1, "Error");
    IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
        () -> executor.add(null));
    assertEquals("Task cannot be null", ex.getMessage());
  }


  @Test
  void testHappyExecution() throws Exception {
    TaskExecutor<String> executor = new TaskExecutor<>(1, "Error");
    List<String> results = new ArrayList<>();
    executor.add(() -> "one");
    executor.add(() -> "two");
    executor.add(() -> "three");
    executor.execute(results::add);
    assertEquals(Arrays.asList("one", "two", "three"), results);
  }

  @Test
  void testParallelExecution() throws Exception {
    TaskExecutor<String> executor = new TaskExecutor<>(3, "Error");

    long start = System.currentTimeMillis();
    executor.add(getDelayedTask(1, "one"));
    executor.add(getDelayedTask(2, "two"));
    executor.add(getDelayedTask(3, "three"));
    executor.execute(System.out::println);
    long diff = System.currentTimeMillis() - start;

    assertTrue(diff >= 3000L);
    assertTrue(diff < 4000L);
  }

  private <T> Callable<T> getDelayedTask(int delaySecs, T result) {
    return () -> {
      TimeUnit.SECONDS.sleep(delaySecs);
      return result;
    };
  }

}
