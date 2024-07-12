package org.datavaultplatform.worker.tasks;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.datavaultplatform.common.PropNames;
import org.datavaultplatform.common.event.Event;
import org.datavaultplatform.common.event.deposit.*;
import org.datavaultplatform.common.event.retrieve.RetrieveComplete;
import org.datavaultplatform.common.storage.Verify;
import org.datavaultplatform.common.task.TaskInterrupter;
import org.datavaultplatform.worker.rabbit.RabbitMessageSelectorScheduler;
import org.datavaultplatform.worker.utils.DepositEvents;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.util.Assert;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.datavaultplatform.worker.tasks.retrieve.RetrieveUtils.DATA_VAULT_HIDDEN_FILE_NAME;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Slf4j
public abstract class BasePerformDepositThenRestartThenRetrieveIT extends BaseDepositRestartIT {
  
  @Autowired
  RabbitMessageSelectorScheduler scheduler;
  
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

  abstract void checkChunkingProps(boolean chunkingEnabled, String chunkingByteSize);

  @Test
  @SneakyThrows
  void testDepositThenRestartThenRetrieve() {
    assertEquals(0, destDir.listFiles().length);
    String sampleDepositMessage = getSampleDepositMessage();
    Event nextLastEvent = null;
    int count = 0;
    Deposit deposit=null;
    String depositMessage=null;
    for(Class<? extends Event> interruptAtEventClass : List.of(
            ComputedSize.class,
            TransferComplete.class,
            ComputedEncryption.class,
            UploadComplete.class,
            Complete.class)) {

      deposit = new ObjectMapper().readValue(sampleDepositMessage, Deposit.class);
      modifyDepositWithEvents(deposit, events);
      log.info("-----------------------------------------------------");
      log.info("ITERATION[{}][{}]-------------------------------------------", ++count, interruptAtEventClass.getSimpleName());
      log.info("-----------------------------------------------------");
      scheduler.setChecker(new TaskInterrupter.Checker(event -> interruptAtEventClass.getName().equals(event.getClass().getName()), interruptAtEventClass.getSimpleName()));
      Event lastEvent = nextLastEvent;
      depositMessage = mapper.writeValueAsString(deposit);
      log.info("depositMessage {}", depositMessage);
      sendNormalMessage(depositMessage, lastEvent);
      waitUntil(() -> foundEvent(interruptAtEventClass));
      List<Event> reversed = new ArrayList<>(events);
      Collections.reverse(reversed);
      nextLastEvent = reversed.stream().filter(e -> e.getClass().equals(interruptAtEventClass)).findFirst().get();
      Assert.isTrue(nextLastEvent != null, "Cannot find next last event when interruptAtEventClass[%s]".formatted(interruptAtEventClass));
      assertThat(nextLastEvent.getClass().getSimpleName()).isEqualTo(interruptAtEventClass.getSimpleName());
    }
    DepositEvents depositEvents = new DepositEvents(deposit, this.events);
    checkDepositEvents();
    
    checkDepositWorkedOkay(depositMessage, depositEvents);

    File hiddenFile = new File(this.retrieveDir, DATA_VAULT_HIDDEN_FILE_NAME);
    assertThat(hiddenFile).doesNotExist();
    buildAndSendRetrieveMessage(depositEvents);
    checkRetrieve();
    assertThat(hiddenFile).exists().isFile().isReadable();
  }

  private void modifyDepositWithEvents(Deposit deposit, List<Event> events) {
    Map<String, String> props = deposit.getProperties();
    
    // for non-chunk
    props.put(PropNames.NUM_OF_CHUNKS, "0");
    fromEventsArchiveDigest(events).ifPresent( archiveDigest -> props.put(PropNames.ARCHIVE_DIGEST, archiveDigest));
    fromEventsEncArchiveDigest(events).ifPresent(deposit::setEncTarDigest);
    fromEventsEncArchiveTarIV(events).ifPresent(deposit::setTarIV);
    
    // for chunks
    fromEventsNumberOfChunks(events).ifPresent( numberOfChunks -> props.put(PropNames.NUM_OF_CHUNKS, String.valueOf(numberOfChunks)));
    fromEventsChunksDigests(events).ifPresent(deposit::setChunkFilesDigest);
    fromEventsChunksEncDigests(events).ifPresent(deposit::setEncChunksDigest);
    fromEventsEncChunksIVs(events).ifPresent(deposit::setChunksIVs);
  }
  
  Optional<String> fromEventsArchiveDigest(List<Event> events) {
    return events.stream().filter(e -> e.getEventClass().equals(ComputedDigest.class.getName()))
            .map(ComputedDigest.class::cast).map(ComputedDigest::getDigest).filter(Objects::nonNull).findFirst();
  }
  Optional<String> fromEventsEncArchiveDigest(List<Event> events) {
    return events.stream().filter(e -> e.getEventClass().equals(ComputedEncryption.class.getName()))
            .map(ComputedEncryption.class::cast).map(ComputedEncryption::getEncTarDigest).filter(Objects::nonNull).findFirst();
  }
  Optional<byte[]> fromEventsEncArchiveTarIV(List<Event> events) {
    return events.stream().filter(e -> e.getEventClass().equals(ComputedEncryption.class.getName()))
            .map(ComputedEncryption.class::cast).map(ComputedEncryption::getTarIV).filter(Objects::nonNull).findFirst();
  }
  Optional<HashMap<Integer,String>> fromEventsChunksDigests(List<Event> events) {
    return events.stream().filter(e -> e.getEventClass().equals(ComputedEncryption.class.getName()))
            .map(ComputedEncryption.class::cast).map(ComputedEncryption::getChunksDigest).filter(Objects::nonNull).findFirst();
  }
  Optional<HashMap<Integer,String>> fromEventsChunksEncDigests(List<Event> events) {
    return events.stream().filter(e -> e.getEventClass().equals(ComputedEncryption.class.getName()))
            .map(ComputedEncryption.class::cast).map(ComputedEncryption::getEncChunkDigests).filter(Objects::nonNull).findFirst();
  }
  Optional<HashMap<Integer,byte[]>> fromEventsEncChunksIVs(List<Event> events) {
    return events.stream().filter(e -> e.getEventClass().equals(ComputedEncryption.class.getName()))
            .map(ComputedEncryption.class::cast).map(ComputedEncryption::getChunkIVs).filter(Objects::nonNull).findFirst();
  }
  
  Optional<Integer> fromEventsNumberOfChunks(List<Event> events) {
    return events.stream().filter(e -> e.getEventClass().equals(ComputedEncryption.class.getName()))
            .map(ComputedEncryption.class::cast)
            .map(ComputedEncryption::getChunkIVs)
            .map(HashMap::size)
            .findFirst();
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

  boolean foundEvent(Class<? extends Event> eventClass) {
    return events.stream()
            .anyMatch(e -> e.getClass().equals(eventClass));
  }

  boolean foundComplete() {
    return foundEvent(Complete.class);
  }

  abstract Optional<Integer> getExpectedNumberChunksPerDeposit();


  @SneakyThrows
  private void buildAndSendRetrieveMessage(DepositEvents depositEvents) {
    String retrieveMessage2 = depositEvents.generateRetrieveMessage(this.retrieveBaseDir, this.retrieveDir.getName());
    sendNormalMessage(retrieveMessage2, null);
  }
}
