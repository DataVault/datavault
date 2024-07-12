package org.datavaultplatform.worker.tasks;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.datavaultplatform.common.PropNames;
import org.datavaultplatform.common.event.Event;
import org.datavaultplatform.common.event.deposit.*;
import org.datavaultplatform.common.event.retrieve.*;
import org.datavaultplatform.common.storage.Verify;
import org.datavaultplatform.common.task.TaskInterrupter;
import org.datavaultplatform.common.task.TaskStageEvent;
import org.datavaultplatform.common.task.TaskStageEventListener;
import org.datavaultplatform.worker.rabbit.RabbitMessageSelectorScheduler;
import org.datavaultplatform.worker.utils.DepositEvents;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
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
import static org.mockito.ArgumentMatchers.any;

@Slf4j
public abstract class BasePerformDepositThenRetrieveThenRestartIT extends BaseDepositRestartIT {
  
  @MockBean
  TaskStageEventListener mockTaskStageEventListener;
  
  protected List<TaskStageEvent> taskStageEvents;
  
  @Autowired
  RabbitMessageSelectorScheduler scheduler;
  
  @BeforeEach
  void setupTaskStageEventListener() {
    taskStageEvents = new ArrayList<>();

    Mockito.doAnswer(invocation -> {
      TaskStageEvent event = invocation.getArgument(0, TaskStageEvent.class);
      taskStageEvents.add(event);
      return null;
    }).when(mockTaskStageEventListener).onTaskStageEvent(any(TaskStageEvent.class));

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
  void testDepositThenRetrieveThenRestart() {
    assertEquals(0, destDir.listFiles().length);
    String depositMessage = getSampleDepositMessage();
    Deposit deposit = new ObjectMapper().readValue(depositMessage, Deposit.class);
    log.info("depositMessage {}", depositMessage);
    sendNormalMessage(depositMessage, null);
    waitUntil(this::foundComplete);

    DepositEvents depositEvents = new DepositEvents(deposit, this.events);

    checkDepositEvents();

    checkDepositWorkedOkay(depositMessage, depositEvents);

    File hiddenFile = new File(this.retrieveDir, DATA_VAULT_HIDDEN_FILE_NAME);
    assertThat(hiddenFile).doesNotExist();

    buildAndSendRetrieveMessages(depositEvents);
    
    checkRetrieve();
    assertThat(hiddenFile).exists().isFile().isReadable();
  }

  protected void checkDepositEvents() {
  }

  @SneakyThrows
  private void checkRetrieve() {  
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
  
  boolean foundEvent(Class<? extends Event> eventClass) {
    return events.stream()
            .anyMatch(e -> e.getClass().equals(eventClass));
  }

  boolean foundComplete() {
    return foundEvent(Complete.class);
  }

  abstract Optional<Integer> getExpectedNumberChunksPerDeposit();


  @SneakyThrows
  private void buildAndSendRetrieveMessages(DepositEvents depositEvents) {
    String retrieveMessage2 = depositEvents.generateRetrieveMessage(this.retrieveBaseDir, this.retrieveDir.getName());

    Event nextLastEvent = null;
    int count = 0;
    Retrieve retrieve;
    String retrieveMessage;
    
    for (Class<? extends Event> interruptAtEventClass : List.of(
            UserStoreSpaceAvailableChecked.class,
            ArchiveStoreRetrievedAll.class,
            //is there not an event for rebuilding the user store contents ?
            UploadedToUserStore.class,
            RetrieveComplete.class)) {

      taskStageEvents.clear();
      retrieve = new ObjectMapper().readValue(retrieveMessage2, Retrieve.class);
      Path tarFilePath = Path.of(tempDir).resolve("datavault-worker").resolve(retrieve.getProperties().get(PropNames.BAG_ID)+".tar");

      log.info("-----------------------------------------------------");
      log.info("ITERATION[{}][{}]-------------------------------------------", ++count, interruptAtEventClass.getSimpleName());
      log.info("CALCULATED TAR FILE [{}]calculated-exists[{}]", tarFilePath, Files.exists(tarFilePath));
      log.info("-----------------------------------------------------");
      scheduler.setChecker(new TaskInterrupter.Checker(event -> interruptAtEventClass.getName().equals(event.getClass().getName()), interruptAtEventClass.getSimpleName()));
      Event lastEvent = nextLastEvent;
      retrieveMessage = mapper.writeValueAsString(retrieve);
      log.info("retrieveMessage {}", retrieveMessage);
      sendNormalMessage(retrieveMessage, lastEvent);
      waitUntil(() -> foundEvent(interruptAtEventClass));

      assertThat(this.taskStageEvents).isNotEmpty();

      if(interruptAtEventClass == RetrieveComplete.class){
        assertThat(this.taskStageEvents.stream().allMatch(TaskStageEvent::skipped));
      } else {
        int shouldHaveSkipped = taskStageEvents.size() - 1;
        assertThat(this.taskStageEvents.stream().limit(shouldHaveSkipped).allMatch(TaskStageEvent::skipped));
        TaskStageEvent lastTaskStageEvent = taskStageEvents.get(taskStageEvents.size() - 1);
        assertThat(lastTaskStageEvent.skipped()).isFalse();
      }

      List<Event> reversed = new ArrayList<>(events);
      Collections.reverse(reversed);
      nextLastEvent = reversed.stream().filter(e -> e.getClass().equals(interruptAtEventClass)).findFirst().get();
      Assert.isTrue(nextLastEvent != null, "Cannot find next last event when interruptAtEventClass[%s]".formatted(interruptAtEventClass));
      assertThat(nextLastEvent.getClass().getSimpleName()).isEqualTo(interruptAtEventClass.getSimpleName());
    }
  }
}
