package org.datavaultplatform.worker.tasks;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
@TestPropertySource(properties = "chunking.size=60MB")
@Slf4j
public class PerformDepositThenRetrieveSingleChunkIT extends BasePerformDepositThenRetrieveIT {

  @Override
  void checkChunkingProps(boolean chunkingEnabled, String chunkingByteSize) {
    assertTrue(chunkingEnabled);
    assertEquals("60MB", chunkingByteSize);
  }

  @Override
  Optional<Integer> getExpectedNumberChunksPerDeposit() {
    return Optional.of(1);
  }
}