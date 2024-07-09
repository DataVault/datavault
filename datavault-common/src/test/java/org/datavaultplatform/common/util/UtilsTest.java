package org.datavaultplatform.common.util;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@Slf4j
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
    try (FileOutputStream fos = new FileOutputStream(temp)) {
      IOUtils.write("this is a test", fos, StandardCharsets.UTF_8);
    }

    String expectedHash1 = "fa26be19de6bff93f70bc2308434e4a440bbad02";
    String expectedHash2 = expectedHash1.toUpperCase();

    Utils.checkFileHash("works", temp, expectedHash1);
    Utils.checkFileHash("WORKS", temp, expectedHash2);
    Utils.checkFileHash("<NULLTEST>", temp, null);

    Exception ex = assertThrows(Exception.class, () ->
            Utils.checkFileHash("fails", temp, "wrong_hash"));

    assertEquals("Checksum check failed for [" + temp.getCanonicalPath() + "],(fails), FA26BE19DE6BFF93F70BC2308434E4A440BBAD02 != WRONG_HASH", ex.getMessage());
  }

  @Nested
  class ToCommaSeparatedStringTests {

    @Test
    void testNull() {
      assertThat(Utils.toCommaSeparatedString(null)).isEqualTo("");
    }

    @Test
    void testEmpty() {
      assertThat(Utils.toCommaSeparatedString(Collections.emptyList())).isEqualTo("");
    }

    @Test
    void testNullsAreIgnored() {
      ArrayList<Integer> values = new ArrayList<>();
      values.add(3);
      values.add(2);
      values.add(null);
      values.add(null);
      values.add(1);
      assertThat(Utils.toCommaSeparatedString(values)).isEqualTo("3,2,1");
    }

    @Test
    void testSuccess() {
      assertThat(Utils.toCommaSeparatedString(List.of(1, 2, 3, 4))).isEqualTo("1,2,3,4");
      assertThat(Utils.toCommaSeparatedString(List.of(4, 3, 2, 1))).isEqualTo("4,3,2,1");
    }
  }

  @Nested
  class FromCommandSeparatedStringTests {

    @Test
    void testNullParser() {
      IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> Utils.fromCommaSeparatedString("", null));
      assertThat(ex).hasMessage("The parser cannot be null");
    }

    @Test
    void testEmptyString() {
      assertThat(Utils.fromCommaSeparatedString("", Integer::parseInt)).isEqualTo(Collections.emptyList());
    }

    @Test
    void testNullString() {
      assertThat(Utils.fromCommaSeparatedString(null, Integer::parseInt)).isEqualTo(Collections.emptyList());
    }

    @Test
    void testSuccess() {
      assertThat(Utils.fromCommaSeparatedString("1,2,3,4", Integer::parseInt)).isEqualTo(List.of(1, 2, 3, 4));
      assertThat(Utils.fromCommaSeparatedString("4 , 3 , 2 , 1", Integer::parseInt)).isEqualTo(List.of(4, 3, 2, 1));
    }
  }

  @Test
  void testIsRunningWithinTest(){
      Logger logback = ((Logger) org.slf4j.LoggerFactory.getLogger(Utils.class));
      Level initLevel = logback.getLevel();
      try {
        logback.setLevel(Level.TRACE);

        assertThat(Utils.isRunningWithinTest()).isTrue();
      } finally {
        logback.setLevel(initLevel);
      }
    }
}
