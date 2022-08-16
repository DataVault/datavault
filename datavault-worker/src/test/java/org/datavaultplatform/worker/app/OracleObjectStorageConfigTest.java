package org.datavaultplatform.worker.app;

import static org.junit.Assert.assertFalse;

import org.datavaultplatform.common.storage.impl.OracleObjectStorageClassic;
import org.junit.jupiter.api.Test;

public class OracleObjectStorageConfigTest {

  @Test
  void testConfig() {
    assertFalse(OracleObjectStorageClassic.checkConfig());
  }
}
