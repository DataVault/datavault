package org.datavaultplatform.worker.tasks;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;

import org.datavaultplatform.common.event.deposit.Complete;
import org.datavaultplatform.common.event.deposit.CompleteCopyUpload;
import org.datavaultplatform.common.event.retrieve.RetrieveComplete;
import org.datavaultplatform.common.storage.Verify;
import org.datavaultplatform.worker.utils.DepositEvents;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.datavaultplatform.worker.tasks.retrieve.RetrieveUtils.DATA_VAULT_HIDDEN_FILE_NAME;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Slf4j
public abstract class BasePerformDepositThenRetrieveIT extends BaseDepositIntegrationTest {
  
  @Override
  void taskSpecificSetup() throws IOException {
    Path baseTemp = Paths.get(this.tempDir);
    retrieveBaseDir = baseTemp.resolve("retrieve").toFile();
    retrieveDir = retrieveBaseDir.toPath().resolve("ret-folder").toFile();
    Files.createDirectories(retrieveBaseDir.toPath());
    Files.createDirectories(retrieveDir.toPath());
    assertThat(retrieveBaseDir).exists();
    assertThat(retrieveBaseDir).isDirectory();

    assertThat(retrieveDir).exists();
    assertThat(retrieveDir).isDirectory();
    log.info("retrieve base dir [{}]", retrieveBaseDir);
    log.info("retrieve dir [{}]", retrieveDir);

  }

  @SneakyThrows
  static Set<Path> getPathsWithinTarFile(File tarFile) {
    Set<Path> paths = new HashSet<>();
    try (TarArchiveInputStream tarIn = new TarArchiveInputStream(new FileInputStream(tarFile))) {
      TarArchiveEntry entry;
      while ((entry = tarIn.getNextEntry()) != null) {
        if (entry.isDirectory()) {
          continue;
        }
        paths.add(Paths.get(entry.getName()));
      }
    }
    return paths;
  }

  @DynamicPropertySource
  @SneakyThrows
  static void setupProperties(DynamicPropertyRegistry registry) {
    File baseTemp = Files.createTempDirectory("test").toFile();
    File tempDir = new File(baseTemp, "temp");
    assertTrue(tempDir.mkdir());

    File metaDir = new File(baseTemp, "meta");
    assertTrue(metaDir.mkdir());

    String tempDirValue = tempDir.getCanonicalPath();
    String metaDirValue = metaDir.getCanonicalPath();

    registry.add("tempDir", () -> tempDirValue);
    registry.add("metaDir", () -> metaDirValue);
  }



  abstract void checkChunkingProps(boolean chunkingEnabled, String chunkingByteSize);

  @Test
  @SneakyThrows
  void testDepositThenRetrieve() {
    assertEquals(0, destDir.listFiles().length);
    String depositMessage = getSampleDepositMessage();
    Deposit deposit = new ObjectMapper().readValue(depositMessage, Deposit.class);
    log.info("depositMessage {}", depositMessage);
    sendNormalMessage(depositMessage);
    waitUntil(this::foundComplete);

    DepositEvents depositEvents = new DepositEvents(deposit, this.events);

    checkDepositEvents();
    
    checkDepositWorkedOkay(depositMessage, depositEvents);

    File hiddenFile = new File(this.retrieveDir, DATA_VAULT_HIDDEN_FILE_NAME);
    assertThat(hiddenFile).doesNotExist();
    buildAndSendRetrieveMessage(depositEvents);
    checkRetrieve();
    assertThat(hiddenFile).exists().isFile().isReadable();
    
    
  }

  protected void checkDepositEvents() {
  }

  @SneakyThrows
  private void checkRetrieve() {
    log.info("FIN {}", retrieveDir.getCanonicalPath());
    waitUntil(this::foundRetrieveComplete);
    log.info("FIN {}", retrieveDir.getCanonicalPath());
    File retrieved = new File(this.retrieveDir + "/"  + SRC_PATH_DEFAULT + "/src-file-1");

    String digestOriginal = Verify.getDigest(this.largeFile.getFile());
    String digestRetrieved = Verify.getDigest(retrieved);

    assertEquals(digestOriginal, digestRetrieved);
  }

  public List<CompleteCopyUpload> getCopyUploadCompleteEvents(){
    return events.stream()
            .filter(e -> e.getClass().equals(CompleteCopyUpload.class))
            .map(CompleteCopyUpload.class::cast)
            .toList();
  }
  
  boolean foundRetrieveComplete() {
    return events.stream()
            .anyMatch(e -> e.getClass().equals(RetrieveComplete.class));
  }

  boolean foundComplete() {
    return events.stream()
            .anyMatch(e -> e.getClass().equals(Complete.class));
  }

  abstract Optional<Integer> getExpectedNumberChunksPerDeposit();


  @SneakyThrows
  private void buildAndSendRetrieveMessage(DepositEvents depositEvents) {
    String retrieveMessage2 = depositEvents.generateRetrieveMessage(this.retrieveBaseDir, this.retrieveDir.getName());
    sendNormalMessage(retrieveMessage2);
  }
}
