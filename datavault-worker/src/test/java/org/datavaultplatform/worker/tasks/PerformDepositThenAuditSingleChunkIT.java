package org.datavaultplatform.worker.tasks;

import lombok.extern.slf4j.Slf4j;
import org.datavaultplatform.worker.app.DataVaultWorkerInstanceApp;
import org.datavaultplatform.worker.test.AddTestProperties;
import org.slf4j.Logger;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.TestPropertySource;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(classes = {
        DataVaultWorkerInstanceApp.class,
        PerformDepositThenAuditSingleChunkIT.TestConfig.class
})
@Slf4j
@AddTestProperties
@TestPropertySource(properties = "chunking.size=60MB")
public class PerformDepositThenAuditSingleChunkIT extends BasePerformDepositThenAuditIT {

  @Override
  void checkChunkingProps(boolean chunkingEnabled, String chunkingByteSize) {
    assertTrue(chunkingEnabled);
    assertEquals("60MB", chunkingByteSize);
  }

  @Override
  Optional<Integer> getExpectedNumberChunksPerDeposit() {
    return Optional.of(1);
  }

  @TestConfiguration
  static class TestConfig {
    @Bean
    Logger monitorLogger() {
      return log;
    }
  }
}