package org.datavaultplatform.worker.tasks;

import lombok.extern.slf4j.Slf4j;
import org.datavaultplatform.worker.app.DataVaultWorkerInstanceApp;
import org.datavaultplatform.worker.test.AddTestProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(classes = DataVaultWorkerInstanceApp.class)
@AddTestProperties
@DirtiesContext
@TestPropertySource(properties = "chunking.size=20MB")
@Slf4j
public class PerformMultiDepositThenRetrieveMultiChunkIT extends BaseMultiPerformDepositThenRetrieveIT {

  @Override
  void checkChunkingProps(boolean chunkingEnabled, String chunkingByteSize) {
    assertTrue(chunkingEnabled);
    assertEquals("20MB", chunkingByteSize);
  }


  @Override
  Optional<Integer> getExpectedNumberChunks() {
    return Optional.of(3);
  }
}