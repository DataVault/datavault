package org.datavaultplatform.worker.tasks;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.datavaultplatform.worker.app.DataVaultWorkerInstanceApp;
import org.datavaultplatform.worker.test.AddTestProperties;
import org.datavaultplatform.worker.utils.DepositEvents;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.TestPropertySource;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(classes = {
        DataVaultWorkerInstanceApp.class,
        PerformDepositThenAuditMultiChunkMultiDepositIT.TestConfig.class
})
@Slf4j
@AddTestProperties
@TestPropertySource(properties = "chunking.size=20MB")
public class PerformDepositThenAuditMultiChunkMultiDepositIT extends BasePerformDepositThenAuditIT {

  @Override
  void checkChunkingProps(boolean chunkingEnabled, String chunkingByteSize) {
    assertTrue(chunkingEnabled);
    assertEquals("20MB", chunkingByteSize);
  }


  @Override
  Optional<Integer> getExpectedNumberChunksPerDeposit() {
    return Optional.of(3);
  }

  List<String> getSourcePaths() {
    return Arrays.asList(
            SRC_PATH_1,
            SRC_PATH_2
    );
  }

  @Test
  @SneakyThrows
  @Override
  void testDepositThenAudit() {
    assertEquals(0, destDir.listFiles().length);


    DepositEvents depositEvents1 = checkDeposit(SRC_PATH_1, BAG_ID_1);
    assertEquals(3, destDir.listFiles().length);

    DepositEvents depositEvents2 = checkDeposit(SRC_PATH_2, BAG_ID_2);
    assertEquals(6, destDir.listFiles().length);

    String auditMessage = buildAuditMessage(Arrays.asList(depositEvents1, depositEvents2));

    sendNormalMessage(auditMessage);
    checkAudit();
  }

  @SneakyThrows
  private DepositEvents checkDeposit(String srcPath, String bagId){
    events.clear();
    String depositMessage = getSampleDepositMessage(srcPath, bagId);
    Deposit deposit = new ObjectMapper().readValue(depositMessage, Deposit.class);
    log.info("depositMessage {}", depositMessage);
    sendNormalMessage(depositMessage);
    waitUntil(this::foundComplete);

    DepositEvents depositEvents = new DepositEvents(deposit, this.events);

    checkDepositWorkedOkay(srcPath, depositMessage, depositEvents);

    return depositEvents;
  }
  
  @TestConfiguration
  static class TestConfig {
    @Bean
    Logger monitorLogger() {
      return log;
    }
  }
}