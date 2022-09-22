package org.datavaultplatform.worker.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import lombok.SneakyThrows;
import org.apache.commons.io.IOUtils;
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

  @Test
  @SneakyThrows
  void testCheckFileHash() {

    File temp = Files.createTempFile("test", ".txt").toFile();
    try(FileOutputStream fos = new FileOutputStream(temp)){
      IOUtils.write("this is a test", fos);
    }

    String expectedHash1 = "fa26be19de6bff93f70bc2308434e4a440bbad02";
    String expectedHash2 = expectedHash1.toUpperCase();

    Utils.checkFileHash("works", temp, expectedHash1);
    Utils.checkFileHash("WORKS", temp, expectedHash2);
    Utils.checkFileHash("<NULLTEST>", temp, null);

    Exception ex = assertThrows(Exception.class, () ->
        Utils.checkFileHash("fails", temp, "wrong_hash"));

    assertEquals("Checksum check failed for ["+temp.getCanonicalPath()+"],(fails), FA26BE19DE6BFF93F70BC2308434E4A440BBAD02 != WRONG_HASH", ex.getMessage());
  }
}
