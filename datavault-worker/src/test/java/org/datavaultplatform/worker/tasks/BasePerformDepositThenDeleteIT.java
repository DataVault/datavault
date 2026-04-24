package org.datavaultplatform.worker.tasks;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.awaitility.core.ConditionTimeoutException;
import org.datavaultplatform.common.event.delete.DeleteComplete;
import org.datavaultplatform.common.event.deposit.CompleteCopyUpload;
import org.datavaultplatform.worker.utils.DepositEvents;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

@Slf4j 
abstract class BasePerformDepositThenDeleteIT extends BaseDepositIT implements DepositThenDelete {
  
  @Override
  void taskSpecificSetup() {
  }

  abstract void checkChunkingProps(boolean chunkingEnabled, String chunkingByteSize);

  @Test
  @SneakyThrows
  void testDepositThenDelete() {
    assertEquals(0, destDir.listFiles().length);
    String depositMessage = getSampleDepositMessage();
    Deposit deposit = new ObjectMapper().readValue(depositMessage, Deposit.class);
    log.info("depositMessage {}", depositMessage);
    sendNormalMessage(depositMessage);
    waitUntil(this::foundComplete);

    DepositEvents depositEvents = new DepositEvents(deposit, this.events);
    checkDepositEvents();
    this.events.clear();
    checkDepositWorkedOkay(depositMessage, depositEvents);

    List<File> expectedFiles = getExpectedDepositFiles(depositMessage);
    for (File file : expectedFiles) {
      log.info("Checking file [{}] exists and non-empty", file.getCanonicalPath());
      assertThat(file).exists();
      assertThat(file).isNotEmpty();
    }

    buildAndSendDeleteMessage(depositEvents);
 
    try {
      waitUntil(this::foundDeleteComplete);
    } catch (ConditionTimeoutException ex) {
      log.error("Timed Out waiting for DeleteComplete", ex);
    }
    checkDeleteEvents(this.events);

    for (File file : expectedFiles) {
      log.info("Checking file [{}] has been deleted", file.getCanonicalPath());
      assertThat(file).doesNotExist();
    }

  }

  @SneakyThrows
  private List<File> getExpectedDepositFiles(String depositMessage) {
    Deposit deposit = mapper.readValue(depositMessage, Deposit.class);
    String bagId = deposit.getProperties().get("bagId");

    // create path from this.destDir
    Path baseDest = this.destDir.toPath();

    List<File> files = new ArrayList<>();
    if (chunkingEnabled) {
      int numberOfChunks = getExpectedNumberChunksPerDeposit().orElseThrow();
      for (int chunkNum = 1; chunkNum <= numberOfChunks; chunkNum++) {
        File chunkTarFile = baseDest.resolve(bagId + ".tar." + chunkNum).toFile();
        files.add(chunkTarFile);
      }
    } else {
      File singleTarFile = baseDest.resolve(bagId + ".tar").toFile();
      files.add(singleTarFile);
    }
    return files;
  }
  
  public List<CompleteCopyUpload> getCopyUploadCompleteEvents(){
    return events.stream()
            .filter(e -> e.getClass().equals(CompleteCopyUpload.class))
            .map(CompleteCopyUpload.class::cast)
            .toList();
  }
  
  boolean foundDeleteComplete() {
    return events.stream()
            .anyMatch(e -> e.getClass().equals(DeleteComplete.class));
  }

  @SneakyThrows
  private void buildAndSendDeleteMessage(DepositEvents depositEvents) {
    String deleteMessage = depositEvents.generateDeleteMessage();
    sendNormalMessage(deleteMessage);
  }

  public final long getArchiveCount() {
    return 1;
  }
}
