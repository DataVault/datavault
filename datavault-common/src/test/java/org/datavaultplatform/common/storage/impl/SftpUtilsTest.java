package org.datavaultplatform.common.storage.impl;

import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

@Slf4j
public class SftpUtilsTest {

  @Test
  void testTimestampedDirectory(){

    Clock fixedClock = Clock.fixed(Instant.parse("2022-11-08T14:13:06.618Z"),
        ZoneId.of("Europe/London"));

    String tsDir = SftpUtils.getTimestampedDirectoryName(fixedClock);

    assertEquals("dv_20221108141306", tsDir);
  }

  @Test
  void testTimestampedDirectoryOnceEveryTwoSeconds() throws InterruptedException {

    final List<Instant> finishTimes = new ArrayList<>();

    Clock clock = Clock.systemDefaultZone();

    ExecutorService executor = Executors.newFixedThreadPool(5);
    for (int i = 0; i < 5; i++) {
      CompletableFuture<String> cf = CompletableFuture.supplyAsync(
          () -> SftpUtils.getTimestampedDirectoryName(clock), executor);
      cf.thenAccept(value -> {
        Instant now = clock.instant();
        log.info("instant ms is [{}]", now);
        finishTimes.add(now);
      });
    }
    executor.shutdown();
    boolean finishedOkay = executor.awaitTermination(12, TimeUnit.SECONDS);
    if (!finishedOkay) {
      throw new IllegalStateException("Failed to finished within 12 seconds");
    }
    for (int i = 1; i < 5; i++) {
      long diff = Duration.between(finishTimes.get(i - 1), finishTimes.get(i)).toMillis();
      log.info("diff is [{} to {}][{}]", i - 1, i, diff);
      Assertions.assertTrue(diff >= 2000);
    }
  }

}
