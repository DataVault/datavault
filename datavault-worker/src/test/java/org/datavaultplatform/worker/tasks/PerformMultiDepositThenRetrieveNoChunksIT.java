package org.datavaultplatform.worker.tasks;

import lombok.extern.slf4j.Slf4j;
import org.datavaultplatform.common.event.deposit.CompleteCopyUpload;
import org.datavaultplatform.worker.app.DataVaultWorkerInstanceApp;
import org.datavaultplatform.worker.test.AddTestProperties;
import org.junit.jupiter.api.Timeout;
import org.slf4j.Logger;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.TestPropertySource;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

@SuppressWarnings("DefaultAnnotationParam")
@SpringBootTest(classes = {
        DataVaultWorkerInstanceApp.class,
        PerformMultiDepositThenRetrieveNoChunksIT.TestConfig.class
})
@Slf4j
@AddTestProperties
@Timeout(value = 60, unit = TimeUnit.SECONDS)
@TestPropertySource(properties = {"chunking.enabled=false","chunking.size=0"})
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

  @Override
  protected void checkDepositEvents() {
    List<CompleteCopyUpload> storedChunksEvents = getCopyUploadCompleteEvents();
    assertThat(storedChunksEvents.size()).isEqualTo(1);
    assertThat(storedChunksEvents.get(0).getChunkNumber()).isNull();
  }
  
  @TestConfiguration
  static class TestConfig {
    @Bean
    Logger monitorLogger() {
      return log;
    }
  }
}