package org.datavaultplatform.broker.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

import org.junit.function.ThrowingRunnable;

public abstract class TestUtils {

  public static <T extends Exception> void checkException(Class<T> exceptionClass, String message, ThrowingRunnable runnable) {
    T ex = assertThrows(exceptionClass, runnable);
    assertEquals(message, ex.getMessage());
  }

}
