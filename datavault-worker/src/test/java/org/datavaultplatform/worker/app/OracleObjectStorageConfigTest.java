package org.datavaultplatform.worker.app;

import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import ch.qos.logback.classic.Logger;
import java.io.File;
import java.nio.file.Files;
import lombok.SneakyThrows;
import org.datavaultplatform.common.storage.impl.OracleObjectStorageClassic;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;


public class OracleObjectStorageConfigTest {

  static File tempHome;

  static ListAppender<ILoggingEvent> logBackListAppender = new ListAppender<>();

  private static ch.qos.logback.classic.Logger getOracleClassicLogger() {
    ch.qos.logback.classic.Logger result = (ch.qos.logback.classic.Logger) OracleObjectStorageClassic.LOGGER;
    return result;
  }

  @BeforeAll
  @SneakyThrows
  static void setupClass() {
    tempHome = Files.createTempDirectory("tempHome").toFile();
    assertTrue(tempHome.exists());
    assertTrue(tempHome.isDirectory());
    System.setProperty("user.home", tempHome.getAbsolutePath());
  }

  @BeforeEach
  void setup() {
    logBackListAppender.start();
    getOracleClassicLogger().addAppender(logBackListAppender);
  }

  @AfterEach
  void tearDown() {
    logBackListAppender.stop();
    getOracleClassicLogger().detachAppender(logBackListAppender);
  }

  @Test
  void testConfig() {
    assertFalse(OracleObjectStorageClassic.checkConfig());
    logBackListAppender.list.forEach(logEvent -> {
      System.out.println("EVENT " + logEvent.getFormattedMessage());
    });
    String actual = logBackListAppender.list.get(0).getFormattedMessage();
    String expected = String.format("Problem getting Oracle Config from[%s/.oci/config]",
        tempHome.getAbsolutePath());
    assertEquals(expected, actual);
  }


}
