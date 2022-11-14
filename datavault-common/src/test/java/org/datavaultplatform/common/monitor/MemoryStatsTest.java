package org.datavaultplatform.common.monitor;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

@Slf4j
public class MemoryStatsTest {
  @Test
  void testMemoryStats() {
    MemoryStats stats1 = MemoryStats.getCurrent();
    log.info("{}", stats1.toPretty());
    log.info("{}", stats1);
  }
}
