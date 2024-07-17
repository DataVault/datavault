package org.datavaultplatform.worker.tasks;

import lombok.extern.slf4j.Slf4j;
import org.datavaultplatform.common.event.deposit.CompleteCopyUpload;
import org.datavaultplatform.worker.app.DataVaultWorkerInstanceApp;
import org.datavaultplatform.worker.test.AddTestProperties;
import org.junit.jupiter.api.Timeout;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

@SpringBootTest(classes = DataVaultWorkerInstanceApp.class)
@AddTestProperties
@DirtiesContext
@TestPropertySource(properties = {"chunking.enabled=false","chunking.size=0"})
@Slf4j
@Timeout(value = 60, unit = TimeUnit.SECONDS)
public class PerformMultiDepositDirsThenRetrieveNoChunksIT extends BaseMultiPerformDepositDirsThenRetrieveIT {

  @Override
  void checkChunkingProps(boolean chunkingEnabled, String chunkingByteSize) {
    assertFalse(chunkingEnabled);
    assertEquals("0", chunkingByteSize);
  }

  @Override
  Optional<Integer> getExpectedNumberChunks() {
    return Optional.empty();
  }

  @Override
  protected void checkDepositEvents() {
    List<CompleteCopyUpload> storedChunksEvents = getCopyUploadCompleteEvents();
    assertThat(storedChunksEvents.size()).isEqualTo(1);
  }
}