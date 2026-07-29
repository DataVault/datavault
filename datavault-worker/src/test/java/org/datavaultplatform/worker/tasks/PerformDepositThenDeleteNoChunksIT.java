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
import static org.junit.jupiter.api.Assertions.assertFalse;

@SpringBootTest(classes = {
        DataVaultWorkerInstanceApp.class,
        PerformDepositThenDeleteNoChunksIT.TestConfig.class
})
@Slf4j
@AddTestProperties
@TestPropertySource(properties = {"chunking.enabled=false","chunking.size=0"})
class PerformDepositThenDeleteNoChunksIT extends BasePerformDepositThenDeleteIT {

  @Override
  
  void checkChunkingProps(boolean chunkingEnabled, String chunkingByteSize) {
    assertFalse(chunkingEnabled);
    assertEquals("0", chunkingByteSize);
  }

  @Override
  public Optional<Integer> getExpectedNumberChunksPerDeposit() {
    return Optional.empty();
  }
  
  @TestConfiguration
  static class TestConfig {
    @Bean
    Logger monitorLogger() {
      return log;
    }
  }
}