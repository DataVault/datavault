package org.datavaultplatform.worker.tasks;

import lombok.extern.slf4j.Slf4j;
import org.datavaultplatform.worker.app.DataVaultWorkerInstanceApp;
import org.datavaultplatform.worker.test.AddTestProperties;
import org.junit.jupiter.api.Timeout;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

@SpringBootTest(classes = DataVaultWorkerInstanceApp.class)
@AddTestProperties
@DirtiesContext
@TestPropertySource(properties = {"chunking.enabled=false","chunking.size=0"})
@Slf4j
@Timeout(value = 60, unit = TimeUnit.SECONDS)
public class PerformMultiDepositThenRetrieveNoChunksIT extends BaseMultiPerformDepositThenRetrieveIT {

  @Override
  void checkChunkingProps(boolean chunkingEnabled, String chunkingByteSize) {
    assertFalse(chunkingEnabled);
    assertEquals("0", chunkingByteSize);
  }

  @Override
  Optional<Integer> getExpectedNumberChunks() {
    return Optional.empty();
  }
}