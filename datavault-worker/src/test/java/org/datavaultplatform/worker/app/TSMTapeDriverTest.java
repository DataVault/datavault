package org.datavaultplatform.worker.app;

import lombok.extern.slf4j.Slf4j;
import org.datavaultplatform.common.storage.impl.TivoliStorageManager;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;

@Slf4j
public class TSMTapeDriverTest {

  @Test
  void testTSMTapeDriver() {
    assertFalse(TivoliStorageManager.checkTSMTapeDriver());
  }
}
