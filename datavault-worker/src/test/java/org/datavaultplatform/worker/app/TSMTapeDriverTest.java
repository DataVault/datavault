package org.datavaultplatform.worker.app;

import static org.junit.Assert.assertFalse;

import lombok.extern.slf4j.Slf4j;
import org.datavaultplatform.common.storage.impl.TivoliStorageManager;
import org.junit.jupiter.api.Test;

@Slf4j
public class TSMTapeDriverTest {

  @Test
  void testTSMTapeDriver() {
    assertFalse(TivoliStorageManager.checkTSMTapeDriver());
  }
}
