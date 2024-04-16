package org.datavaultplatform.worker.tasks;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.datavaultplatform.worker.app.DataVaultWorkerInstanceApp;
import org.datavaultplatform.worker.test.AddTestProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest(classes = DataVaultWorkerInstanceApp.class)
@AddTestProperties
@DirtiesContext
@TestPropertySource(properties = {"chunking.enabled=false","chunking.size=0"})
@Slf4j
public class PerformDepositThenRetrieveNoChunksIT extends BasePerformDepositThenRetrieveIT {

  @Override
  void checkChunkingProps(boolean chunkingEnabled, String chunkingByteSize) {
    assertFalse(chunkingEnabled);
    assertEquals("0", chunkingByteSize);
  }

  @Override
  Optional<Integer> getExpectedNumberChunksPerDeposit() {
    return Optional.empty();
  }
}