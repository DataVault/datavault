package org.datavaultplatform.common.io;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicReference;

public class ProgressEventListenerTest {

  final AtomicReference<ProgressEvent> lastProgressEvent = new AtomicReference<>(null);

  final ProgressEventListener listener = lastProgressEvent::set;

  Progress progress;

  @BeforeEach
  void setup() {
    progress = new Progress(listener);
    progress.setByteCount(1000);
    progress.setFileCount(2000);
    progress.setDirCount(1000);
  }

  @Test
  void testProgressEvents() {
      check(()-> progress.setStartTime(1_000), ProgressEventType.START_TIME_SET,1_000);
      check(()-> progress.setTimestamp(2_000), ProgressEventType.TIMESTAMP_SET,2_000);
      check(()-> progress.setByteCount(3_000), ProgressEventType.BYTE_COUNT_SET,3_000);
      check(()-> progress.setFileCount(4_000), ProgressEventType.FILE_COUNT_SET,4_000);
      check(()-> progress.setDirCount(5_000), ProgressEventType.DIR_COUNT_SET,5_000);

      check(()-> progress.incByteCount(111), ProgressEventType.BYTE_COUNT_INC,3_111);
      check(()-> progress.incFileCount(222), ProgressEventType.FILE_COUNT_INC,4_222);
      check(()-> progress.incDirCount(333), ProgressEventType.DIR_COUNT_INC,5_333);
  }

  void check(Runnable runnable, ProgressEventType expectedType, long expectedValue) {
    lastProgressEvent.set(null);
    runnable.run();
    ProgressEvent lastEvent = this.lastProgressEvent.get();
    Assertions.assertEquals(expectedType, lastEvent.getType());
    Assertions.assertEquals(expectedValue, lastEvent.getValue());
  }
}
