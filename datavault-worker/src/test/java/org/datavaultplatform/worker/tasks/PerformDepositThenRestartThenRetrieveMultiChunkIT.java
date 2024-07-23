package org.datavaultplatform.worker.tasks;

import lombok.extern.slf4j.Slf4j;
import org.datavaultplatform.common.event.deposit.CompleteCopyUpload;
import org.datavaultplatform.worker.app.DataVaultWorkerInstanceApp;
import org.datavaultplatform.worker.test.AddTestProperties;
import org.slf4j.Logger;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.TestPropertySource;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(classes = {
        DataVaultWorkerInstanceApp.class,
        PerformDepositThenRestartThenRetrieveMultiChunkIT.TestConfig.class
})
@Slf4j
@AddTestProperties
@TestPropertySource(properties = "chunking.size=20MB")
public class PerformDepositThenRestartThenRetrieveMultiChunkIT extends BasePerformDepositThenRestartThenRetrieveIT {

  @Override
  void checkChunkingProps(boolean chunkingEnabled, String chunkingByteSize) {
    assertTrue(chunkingEnabled);
    assertEquals("20MB", chunkingByteSize);
  }


  @Override
  Optional<Integer> getExpectedNumberChunksPerDeposit() {
    return Optional.of(3);
  }

  @Override
  protected void checkDepositEvents() {
    List<CompleteCopyUpload> storedChunksEvents = getCopyUploadCompleteEvents();
    assertThat(storedChunksEvents.size()).isEqualTo(3); //because we only archive each chunk once :-)
    Set<Integer> storedChunkNumbers = storedChunksEvents.stream().map(CompleteCopyUpload::getChunkNumber).collect(Collectors.toSet());
    assertThat(storedChunkNumbers).isEqualTo(Set.of(1, 2, 3));
  }

  @TestConfiguration
  static class TestConfig {
    @Bean
    Logger monitorLogger() {
      return log;
    }
  }
}