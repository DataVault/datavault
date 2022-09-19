package org.datavaultplatform.worker.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

public class UtilsTest {

  @Nested
  class ExecutionExceptionHandlerTests {

    @Test
    void testUnCheckedException() {
      Callable<Object> callable = () -> {
        throw new RuntimeException("UnChecked");
      };
      checkCallable(callable, RuntimeException.class, "UnChecked");
    }

    @Test
    void testCheckedException() {
      Callable<Object> callable = () -> {
        throw new IOException("Checked");
      };
      checkCallable(callable, IOException.class, "Checked");
    }

    @Test
    void testError() {
      Callable<Object> callable = () -> {
        throw new Error("error");
      };
      checkCallable(callable, Error.class, "error");
    }

    <T extends Throwable> void checkCallable(Callable<Object> callable, Class<T> clazz,
        String expectedMessage) {
      ExecutorService executor = Executors.newSingleThreadExecutor();
      Future<Object> async = executor.submit(callable);
      boolean handledEE = false;
      try {
        async.get();
        fail("Expected ExecutionException");
      } catch (InterruptedException ex) {
        fail("Expected ExecutionException");
      } catch (ExecutionException ee) {
        T th = assertThrows(clazz, () -> Utils.handleExecutionException(ee, "ErrorTest"));
        assertEquals(expectedMessage, th.getMessage());
        handledEE = true;
      } finally {
        assertTrue(handledEE);
      }
    }
  }
}
