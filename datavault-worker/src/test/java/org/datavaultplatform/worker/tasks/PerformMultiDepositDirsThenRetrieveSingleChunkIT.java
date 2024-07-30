package org.datavaultplatform.worker.tasks;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.datavaultplatform.common.event.deposit.CompleteCopyUpload;
import org.datavaultplatform.worker.app.DataVaultWorkerInstanceApp;
import org.datavaultplatform.worker.test.AddTestProperties;
import org.slf4j.Logger;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest(classes = {
        DataVaultWorkerInstanceApp.class,
        PerformMultiDepositDirsThenRetrieveSingleChunkIT.TestConfig.class
})
@AddTestProperties
@DirtiesContext
@TestPropertySource(properties = "chunking.size=160MB")
@Slf4j
public class PerformMultiDepositDirsThenRetrieveSingleChunkIT extends BaseMultiPerformDepositDirsThenRetrieveIT {

  @Override
  void checkChunkingProps(boolean chunkingEnabled, String chunkingByteSize) {
    assertTrue(chunkingEnabled);
    assertEquals("160MB", chunkingByteSize);
  }

  @Override
  Optional<Integer> getExpectedNumberChunks() {
    return Optional.of(1);
  }
  
  @Override
  protected void checkDepositEvents() {
    List<CompleteCopyUpload> storedChunksEvents = getCopyUploadCompleteEvents();
    assertThat(storedChunksEvents.size()).isEqualTo(1);
  }
  @TestConfiguration
  static class TestConfig {
    @Bean
    Logger monitorLogger() {
      return log;
    }
  }
}