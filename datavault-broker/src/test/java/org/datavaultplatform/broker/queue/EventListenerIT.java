package org.datavaultplatform.broker.queue;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDate;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.datavaultplatform.broker.app.DataVaultBrokerApp;
import org.datavaultplatform.broker.services.*;
import org.datavaultplatform.broker.test.AddTestProperties;
import org.datavaultplatform.broker.test.BaseDatabaseTest;
import org.datavaultplatform.common.event.Event;
import org.datavaultplatform.common.event.InitStates;
import org.datavaultplatform.common.event.UpdateProgress;
import org.datavaultplatform.common.event.audit.AuditComplete;
import org.datavaultplatform.common.event.audit.AuditError;
import org.datavaultplatform.common.event.audit.AuditStart;
import org.datavaultplatform.common.event.audit.ChunkAuditComplete;
import org.datavaultplatform.common.event.audit.ChunkAuditStarted;
import org.datavaultplatform.common.event.delete.DeleteComplete;
import org.datavaultplatform.common.event.delete.DeleteStart;
import org.datavaultplatform.common.event.deposit.Complete;
import org.datavaultplatform.common.event.deposit.CompleteCopyUpload;
import org.datavaultplatform.common.event.deposit.ComputedChunks;
import org.datavaultplatform.common.event.deposit.ComputedDigest;
import org.datavaultplatform.common.event.deposit.ComputedEncryption;
import org.datavaultplatform.common.event.deposit.ComputedSize;
import org.datavaultplatform.common.event.deposit.PackageComplete;
import org.datavaultplatform.common.event.deposit.Start;
import org.datavaultplatform.common.event.deposit.StartCopyUpload;
import org.datavaultplatform.common.event.deposit.TransferComplete;
import org.datavaultplatform.common.event.deposit.UploadComplete;
import org.datavaultplatform.common.event.deposit.ValidationComplete;
import org.datavaultplatform.common.event.retrieve.*;
import org.datavaultplatform.common.model.*;
import org.datavaultplatform.common.storage.Verify;
import org.junit.jupiter.api.*;
import org.springframework.amqp.rabbit.listener.RabbitListenerEndpointRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest(classes = DataVaultBrokerApp.class)
@AddTestProperties
@TestPropertySource(properties = {
    "broker.rabbit.enabled=false",
    "broker.scheduled.enabled=false",
    "broker.controllers.enabled=false",
    "broker.ldap.enabled=false",
    "broker.scheduled.enabled=false",
    "broker.email.enabled=false"
})
@Import({EventListener.class, TaskTimerSupport.class})
@Slf4j
@TestMethodOrder(MethodOrderer.MethodName.class)
public class EventListenerIT extends BaseDatabaseTest {

  @MockBean
  EmailService emailService;

  @MockBean
  Sender sender;

  @MockBean
  MessageIdProcessedListener messageIdProcessedListener;

  @Autowired
  UsersService usersService;

  @Autowired
  VaultsService vaultsService;

  @Autowired
  DepositsService depositsService;

  @Autowired
  JobsService jobsService;

  @Autowired
  GroupsService groupService;

  @Autowired
  EventListener eventListener;

  @Autowired
  RetrievesService retrievesService;

  @Autowired
  AuditsService auditsService;

  @MockBean
  RabbitListenerEndpointRegistry registry;
  private final String userId = "user123";
  private String bagId = "bag123";
  private String vaultId;
  private String jobDepositId;
  private String jobRetrieveId;
  private String jobGenericId;
  private String depositId;
  private String retrieveId;
  private String auditId;
  private String depositChunkId1;
  private String depositChunkId2;

  Job jobDeposit;
  Job jobRetrieve;
  Job jobGeneric;
  
  Vault vault;
  Deposit deposit;
  User user;
  Audit audit;

  Group group;
    @Autowired
    private EventService eventService;

  @BeforeEach
  void setup(){
    user = new User();
    user.setFirstname("first");
    user.setLastname("last");
    user.setID(userId);
    usersService.addUser(user);

    group = new Group();
    group.setID("group123");
    group.setName("test-group");
    group.setEnabled(true);
    groupService.addGroup(group);

    vault= new Vault();
    vault.setName("test-vault");
    vault.setContact("contact name");
    vault.setGroup(group);
    Date nowPlus1Year = java.sql.Date.valueOf(LocalDate.now().plusYears(1));
    vault.setReviewDate(nowPlus1Year);
    vaultsService.addVault(vault);
    this.vaultId = vault.getID();

    DepositChunk depositChunk1 = new DepositChunk();
    depositChunk1.setDeposit(deposit);
    depositChunk1.setChunkNum(1);
    
    DepositChunk depositChunk2 = new DepositChunk();
    depositChunk2.setDeposit(deposit);
    depositChunk2.setChunkNum(2);

    depositsService.updateDepositChunk(depositChunk1);
    depositsService.updateDepositChunk(depositChunk2);
    
    audit = new Audit();
    audit.setTimestamp(new Date());
    audit.setStatus(Audit.Status.IN_PROGRESS);
    auditsService.addAudit(audit, List.of(depositChunk1, depositChunk2));
    auditId = audit.getID();
    assertThat(auditId).isNotNull();
    
    AuditChunkStatus chunkStatus1 = auditsService.addAuditStatus(audit, depositChunk1,"archive-id-one","location-one");
    assertThat(chunkStatus1.getID()).isNotNull();

    AuditChunkStatus chunkStatus2 = auditsService.addAuditStatus(audit, depositChunk2,"archive-id-two","location-two");
    assertThat(chunkStatus2.getID()).isNotNull();
    
    depositChunkId1 = depositChunk1.getID();
    depositChunkId2 = depositChunk2.getID();
    assertThat(depositChunkId1).isNotNull();
    assertThat(depositChunkId2).isNotNull();
    
    deposit = new Deposit();
    deposit.setName("test-deposit");
    deposit.setHasPersonalData(false);
    deposit.setVault(vault);
    deposit.setUser(user);
    depositsService.addDeposit(vault, deposit, "shortPath", "origin");
    this.bagId = deposit.getBagId();
    this.depositId = deposit.getID();

    jobDeposit = new Job(Job.TASK_CLASS_DEPOSIT);
    jobRetrieve = new Job(Job.TASK_CLASS_RETRIEVE);
    jobGeneric = new Job();
    jobsService.addJob(deposit, jobGeneric);
    jobsService.addJob(deposit, jobDeposit);
    jobsService.addJob(deposit, jobRetrieve);
    this.jobGenericId = jobGeneric.getID();
    this.jobDepositId = jobDeposit.getID();
    this.jobRetrieveId = jobRetrieve.getID();

    Retrieve retrieve = new Retrieve();
    retrieve.setHasExternalRecipients(false);
    retrieve.setTimestamp(new Date());
    retrieve.setDeposit(deposit);

    retrievesService.addRetrieve(retrieve, deposit, "/path");
    this.retrieveId = retrieve.getID();
    assertThat(retrieve.getID()).isNotNull();
  }

  @Test
  void test00EventListener() {
    assertNotNull(eventListener);
    assertNotNull(usersService);
    assertNotNull(jobsService);
    assertNotNull(vaultsService);
    assertNotNull(depositsService);
    assertNotNull(groupService);
  }

  @Test
  @SneakyThrows
  void test01ProcessInitStates() {
    String message = "{"
        + "      \"message\": \"Job states: 6\","
        + "      \"eventClass\": \"org.datavaultplatform.common.event.InitStates\","
        + "      \"nextState\": null,"
        + "      \"timestamp\": \"2022-09-16T15:12:39.359Z\","
        + "      \"sequence\": 20,"
        + "      \"persistent\": false,"
        + "      \"depositId\": \"" + depositId + "\","
        + "      \"vaultId\"  : \"" + vaultId + "\","
        + "      \"jobId\"    : \"" + jobGenericId + "\","
        + "      \"userId\"   : \"" + userId + "\","
        + "      \"agent\": \"datavault-worker-1\","
        + "      \"agentType\": \"WORKER\","
        + "      \"states\": ["
        + "        \"Calculating size\","
        + "        \"Transferring\","
        + "        \"Packaging\","
        + "        \"Storing in archive\","
        + "        \"Verifying\","
        + "        \"Complete\""
        + "      ]"
        + "  }";

    Event event = eventListener.onMessageInternal(message);
    assertEquals(InitStates.class, event.getClass());

    Job job = jobsService.getJob(jobGenericId);
    assertEquals(List.of(
             "Calculating size",
             "Transferring",
             "Packaging",
             "Storing in archive",
             "Verifying",
             "Complete"),job.getStates());
  }

  @SneakyThrows
  @Test
  void test02v1updateProgress() {
    String message= "{"
        + "      \"message\": \"Job progress update\","
        + "      \"eventClass\": \"org.datavaultplatform.common.event.UpdateProgress\","
        + "      \"nextState\": 1,"
        + "      \"timestamp\": \"2022-09-16T15:12:39.387Z\","
        + "      \"sequence\": 23,"
        + "      \"persistent\": false,"
        + "      \"depositId\": \"" + depositId + "\","
        + "      \"vaultId\"  : \"" + vaultId + "\","
        + "      \"jobId\"    : \"" + jobGenericId + "\","
        + "      \"userId\"   : \"" + userId + "\","
        + "      \"agentType\": \"WORKER\""
        + "    }";
    Event event = eventListener.onMessageInternal(message);
  }

  @SneakyThrows
  @Test
  void test02v2UpdateProgress(){
    String message = "{"
        + "      \"message\": \"Job progress update\","
        + "      \"eventClass\": \"org.datavaultplatform.common.event.UpdateProgress\","
        + "      \"nextState\": null,"
        + "      \"timestamp\": \"2022-09-16T15:12:39.388Z\","
        + "      \"sequence\": 24,"
        + "      \"persistent\": false,"
        + "      \"depositId\": \"" + depositId + "\","
        + "      \"vaultId\"  : \"" + vaultId + "\","
        + "      \"jobId\"    : \"" + jobGenericId + "\","
        + "      \"userId\"   : \"" + userId + "\","
        + "      \"agent\": \"datavault-worker-1\","
        + "      \"agentType\": \"WORKER\","
        + "      \"progress\": 0,"
        + "      \"progressMax\": 6,"
        + "      \"progressMessage\": \"Starting transfer ...\""
        + "    }";
    Event event = eventListener.onMessageInternal(message);
    assertEquals(UpdateProgress.class, event.getClass());
  }

  @Test
  @SneakyThrows
  void test02v3UpdateProgress(){
    String message = "{"
        + "      \"id\": null,"
        + "      \"message\": \"Job progress update\","
        + "      \"retrieveId\": null,"
        + "      \"eventClass\": \"org.datavaultplatform.common.event.UpdateProgress\","
        + "      \"nextState\": null,"
        + "      \"timestamp\": \"2022-09-16T15:12:39.647Z\","
        + "      \"sequence\": 25,"
        + "      \"persistent\": false,"
        + "      \"depositId\": \"" + depositId + "\","
        + "      \"vaultId\"  : \"" + vaultId + "\","
        + "      \"jobId\"    : \"" + jobGenericId + "\","
        + "      \"userId\"   : \"" + userId + "\","
        + "      \"agent\": \"datavault-worker-1\","
        + "      \"agentType\": \"WORKER\","
        + "      \"progress\": 6,"
        + "      \"progressMax\": 6,"
        + "      \"progressMessage\": \"Transferred 6 bytes of 6 bytes (1 KB/sec)\""
        + "    }";
    Event event = eventListener.onMessageInternal(message);
    assertEquals(UpdateProgress.class, event.getClass());
  }

  @Test
  @SneakyThrows
  void test03ProcessStart() {
    String message = "{"
        + "      \"message\": \"Deposit started\","
        + "      \"eventClass\": \"org.datavaultplatform.common.event.deposit.Start\","
        + "      \"nextState\": 0,"
        + "      \"timestamp\": \"2022-09-16T15:12:39.375Z\","
        + "      \"sequence\": 21,"
        + "      \"persistent\": true,"
        + "      \"depositId\": \"" + depositId + "\","
        + "      \"vaultId\"  : \"" + vaultId + "\","
        + "      \"jobId\"    : \"" + jobDepositId + "\","
        + "      \"userId\"   : \"" + userId + "\","
        + "      \"agent\": \"datavault-worker-1\","
        + "      \"agentType\": \"WORKER\""
        + "    }";

    assertNull(jobDeposit.getState());
    Event event = eventListener.onMessageInternal(message);
    assertEquals(Start.class, event.getClass());

    Job job = jobsService.getJob(jobDepositId);
    assertEquals(0, job.getState());
  }

  @Test
  @SneakyThrows
  void test04ComputedSize() {

    String message = "{"
        + "      \"message\": \"Deposit size: 6 bytes\","
        + "      \"eventClass\": \"org.datavaultplatform.common.event.deposit.ComputedSize\","
        + "      \"nextState\": null,"
        + "      \"timestamp\": \"2022-09-16T15:12:39.385Z\","
        + "      \"sequence\": 22,"
        + "      \"persistent\": true,"
        + "      \"depositId\": \"" + depositId + "\","
        + "      \"vaultId\"  : \"" + vaultId   + "\","
        + "      \"jobId\"    : \"" + jobDepositId     + "\","
        + "      \"userId\"   : \"" + userId    + "\","
        + "      \"agent\": \"datavault-worker-1\","
        + "      \"agentType\": \"WORKER\","
        + "      \"bytes\": 6"
        + "    }";

    Event event = eventListener.onMessageInternal(message);
    assertEquals(ComputedSize.class, event.getClass());
  }

  @Test
  @SneakyThrows
  void test05ComputedChunks() {
    String message = "{"
        + "      \"eventClass\": \"org.datavaultplatform.common.event.deposit.ComputedChunks\","
        + "      \"persistent\": true,"
        + "      \"depositId\": \"" + depositId + "\","
        + "      \"vaultId\"  : \"" + vaultId   + "\","
        + "      \"jobId\"    : \"" + jobDepositId     + "\","
        + "      \"userId\"   : \"" + userId    + "\""
        + "    }";

    Event event = eventListener.onMessageInternal(message);
    assertEquals(ComputedChunks.class, event.getClass());
  }

  @Test
  @SneakyThrows
  @Transactional
  void test06ComputedEncryption(){
    String message = "{"
        + "      \"message\": \"Chunks encrypted with AES/GCM\","
        + "      \"eventClass\": \"org.datavaultplatform.common.event.deposit.ComputedEncryption\","
        + "      \"nextState\": null,"
        + "      \"timestamp\": \"2022-09-16T15:12:39.804Z\","
        + "      \"sequence\": 29,"
        + "      \"persistent\": true,"
        + "      \"depositId\": \"" + depositId + "\","
        + "      \"vaultId\"  : \"" + vaultId + "\","
        + "      \"jobId\"    : \"" + jobDepositId + "\","
        + "      \"userId\"   : \"" + userId + "\","
        + "      \"agent\": \"datavault-worker-1\","
        + "      \"agentType\": \"WORKER\","
        + "      \"chunkIVs\": {"
        + "        \"1\": \"AVBSQsBmzvRrBAJny6KVUmO9b3zXWDDJaaivbo/R2IlpYC1XKHIQhPwOINg42Vfth+vGICrerw47MIGIYdf5cLe43ZZqDIg6Vi6VFUe0OuaWIVIYaVZ1j7HYDXE6Arvp\""
        + "      },"
        + "      \"encChunkDigests\": {"
        + "        \"1\": \"29D1965B60BF026B8BE2403DC43DE77B85D4CFCB\""
        + "      },"
        + "      \"tarIV\": null,"
        + "      \"encTarDigest\": null,"
        + "      \"aesMode\": \"GCM\","
        + "      \"chunksDigest\": {"
        + "        \"1\": \"B22E00301E28767BD5E96019762B36CB2F395351\""
        + "      },"
        + "      \"digestAlgorithm\": \"SHA-1\""
        + "    }";
    Event event = eventListener.onMessageInternal(message);
    assertEquals(ComputedEncryption.class, event.getClass());

    Deposit deposit1  = depositsService.getDeposit(depositId);
    List<DepositChunk> chunks = deposit1.getDepositChunks();
    assertEquals(1, chunks.size());
    DepositChunk chunk1 = chunks.get(0);
    assertEquals(1, chunk1.getChunkNum());
    assertEquals(Verify.SHA_1_ALGORITHM, chunk1.getArchiveDigestAlgorithm());
    assertEquals("B22E00301E28767BD5E96019762B36CB2F395351", chunk1.getArchiveDigest());
    assertEquals("29D1965B60BF026B8BE2403DC43DE77B85D4CFCB", chunk1.getEcnArchiveDigest());
    assertEquals("AVBSQsBmzvRrBAJny6KVUmO9b3zXWDDJaaivbo/R2IlpYC1XKHIQhPwOINg42Vfth+vGICrerw47MIGIYdf5cLe43ZZqDIg6Vi6VFUe0OuaWIVIYaVZ1j7HYDXE6Arvp",
        Base64.getEncoder().encodeToString(chunk1.getEncIV()));
  }

  @Test
  @SneakyThrows
  void test07ComputedDigest() {
    String message = "{"
        + "      \"message\": \"SHA-1: B22E00301E28767BD5E96019762B36CB2F395351\","
        + "      \"eventClass\": \"org.datavaultplatform.common.event.deposit.ComputedDigest\","
        + "      \"nextState\": null,"
        + "      \"timestamp\": \"2022-09-16T15:12:39.693Z\","
        + "      \"sequence\": 28,"
        + "      \"persistent\": true,"
        + "      \"depositId\": \"" + depositId + "\","
        + "      \"vaultId\"  : \"" + vaultId + "\","
        + "      \"jobId\"    : \"" + jobDepositId + "\","
        + "      \"userId\"   : \"" + userId + "\","
        + "      \"agent\": \"datavault-worker-1\","
        + "      \"agentType\": \"WORKER\","
        + "      \"digest\": \"B22E00301E28767BD5E96019762B36CB2F395351\","
        + "      \"digestAlgorithm\": \"SHA-1\""
        + "    }"
        + "  }";
    Event event = eventListener.onMessageInternal(message);
    assertEquals(ComputedDigest.class, event.getClass());
  }

  @Test
  @SneakyThrows
  void test08UploadComplete() {
    String message = "{"
        + "      \"id\": null,"
        + "      \"message\": \"Upload completed\","
        + "      \"retrieveId\": null,"
        + "      \"eventClass\": \"org.datavaultplatform.common.event.deposit.UploadComplete\","
        + "      \"nextState\": null,"
        + "      \"timestamp\": \"2022-09-16T15:12:40.063Z\","
        + "      \"sequence\": 33,"
        + "      \"persistent\": true,"
        + "      \"depositId\": \"" + depositId + "\","
        + "      \"vaultId\"  : \"" + vaultId + "\","
        + "      \"jobId\"    : \"" + jobDepositId + "\","
        + "      \"userId\"   : \"" + userId + "\","
        + "      \"agent\": \"datavault-worker-1\","
        + "      \"agentType\": \"WORKER\","
        + "      \"archiveIds\": {"
        + "        \"ARCHIVE-STORE-DST-ID\": \"16092022-161238.tar\""
        + "      }}";
    Event event = eventListener.onMessageInternal(message);
    assertEquals(UploadComplete.class, event.getClass());
  }

  @Test
  @SneakyThrows
  void test09Complete() {
    String message = "{"
        + "      \"message\": \"Deposit completed\","
        + "      \"eventClass\": \"org.datavaultplatform.common.event.deposit.Complete\","
        + "      \"nextState\": 5,"
        + "      \"timestamp\": \"2022-09-16T15:12:40.152Z\","
        + "      \"sequence\": 36,"
        + "      \"persistent\": true,"
        + "      \"depositId\": \"" + depositId + "\","
        + "      \"vaultId\"  : \"" + vaultId + "\","
        + "      \"jobId\"    : \"" + jobDepositId + "\","
        + "      \"userId\"   : \"" + userId + "\","
        + "      \"agent\": \"datavault-worker-1\","
        + "      \"agentType\": \"WORKER\","
        + "      \"archiveIds\": {"
        + "        \"ARCHIVE-STORE-DST-ID\": \"16092022-161238.tar\""
        + "      },"
        + "      \"archiveSize\": 10240"
        + "    }";
    Event event = eventListener.onMessageInternal(message);
    assertEquals(Complete.class, event.getClass());
  }

  @Test
  @SneakyThrows
  void test10Error() {
    String message = "{"
        + "      \"eventClass\": \"org.datavaultplatform.common.event.Error\","
        + "      \"nextState\": 5,"
        + "      \"timestamp\": \"2022-09-16T15:12:40.152Z\","
        + "      \"sequence\": 36,"
        + "      \"persistent\": true,"
        + "      \"depositId\": \"" + depositId + "\","
        + "      \"vaultId\"  : \"" + vaultId + "\","
        + "      \"jobId\"    : \"" + jobGenericId + "\","
        + "      \"userId\"   : \"" + userId + "\","
        + "      \"agent\": \"datavault-worker-1\","
        + "      \"agentType\": \"WORKER\""
        + "    }";
    Event event = eventListener.onMessageInternal(message);
    assertEquals(org.datavaultplatform.common.event.Error.class, event.getClass());
  }

  @Nested
  class RetrieveEvents {
    
    @Test
    @SneakyThrows
    void test11RetrieveStart() {
      String message = "{"
              + "      \"message\": \"XXXXXXXXXX\","
              + "      \"eventClass\": \"org.datavaultplatform.common.event.retrieve.RetrieveStart\","
              + "      \"timestamp\": \"2022-09-16T15:12:40.063Z\","
              + "      \"sequence\": 33,"
              + "      \"persistent\": true,"
              + "      \"retrieveId\": \"" + retrieveId + "\","
              + "      \"depositId\": \"" + depositId + "\","
              + "      \"vaultId\"  : \"" + vaultId + "\","
              + "      \"jobId\"    : \"" + jobRetrieveId + "\","
              + "      \"userId\"   : \"" + userId + "\","
              + "      \"agent\": \"datavault-worker-1\","
              + "      \"agentType\": \"WORKER\""
              + "}";

      long before = eventService.count();
      Event event = eventListener.onMessageInternal(message);
      assertEquals(RetrieveStart.class, event.getClass());
      long after = eventService.count();
      assertThat(after).isEqualTo(before + 1);
      checkLastNotFailedRetrievedEvent(event, depositId, retrieveId);
    }

    private void checkLastNotFailedRetrievedEvent(Event event, String depositId, String retrieveId) {
      assertThat(event.getID()).isNotNull();

      Event storedEvent = eventService.findById(event.getID());
      storedEvent.refreshIdFields();
      assertThat(storedEvent.getDepositId()).isEqualTo(depositId);
      assertThat(storedEvent.getUserId()).isEqualTo(userId);
      assertThat(storedEvent.getRetrieveId()).isEqualTo(retrieveId);

      Event found = depositsService.getLastNotFailedRetrieveEvent(depositId, retrieveId);
      assertThat(found.getID()).isEqualTo(event.getID());
    }

    @Test
    @SneakyThrows
    void test12RetrieveComplete() {
      String message = "{"
              + "      \"message\": \"XXXXXXXXXX\","
              + "      \"eventClass\": \"org.datavaultplatform.common.event.retrieve.RetrieveComplete\","
              + "      \"timestamp\": \"2022-09-16T15:12:40.063Z\","
              + "      \"sequence\": 33,"
              + "      \"persistent\": true,"
              + "      \"retrieveId\": \"" + retrieveId + "\","
              + "      \"depositId\": \"" + depositId + "\","
              + "      \"vaultId\"  : \"" + vaultId + "\","
              + "      \"jobId\"    : \"" + jobRetrieveId + "\","
              + "      \"userId\"   : \"" + userId + "\","
              + "      \"agent\": \"datavault-worker-1\","
              + "      \"agentType\": \"WORKER\""
              + "}";

      long before = eventService.count();
      Event event = eventListener.onMessageInternal(message);
      assertEquals(RetrieveComplete.class, event.getClass());
      long after = eventService.count();
      assertThat(after).isEqualTo(before + 1);

      checkLastNotFailedRetrievedEvent(event, depositId, retrieveId);

    }

    @Test
    @SneakyThrows
    void test26archiveStoreRetrievedAll() {
      String message = "{"
              + "      \"message\": \"Archive Store Retrieved All\","
              + "      \"eventClass\": \"org.datavaultplatform.common.event.retrieve.ArchiveStoreRetrievedAll\","
              + "      \"nextState\": null,"
              + "      \"timestamp\": \"2022-09-16T15:12:40.150Z\","
              + "      \"sequence\": 35,"
              + "      \"persistent\": true,"
              + "      \"depositId\":  \"" + depositId + "\","
              + "      \"retrieveId\": \"" + retrieveId + "\","
              + "      \"vaultId\"  :  \"" + vaultId + "\","
              + "      \"jobId\"    :  \"" + jobRetrieveId + "\","
              + "      \"userId\"   :  \"" + userId + "\","
              + "      \"agent\": \"datavault-worker-1\","
              + "      \"agentType\": \"WORKER\""
              + "    }";


      long before = eventService.count();
      Event event = eventListener.onMessageInternal(message);
      assertEquals(ArchiveStoreRetrievedAll.class, event.getClass());
      assertEquals(ArchiveStoreRetrievedAll.MESSAGE, event.getMessage());

      long after = eventService.count();
      assertThat(after).isEqualTo(before + 1);

      checkLastNotFailedRetrievedEvent(event, depositId, retrieveId);
    }

    @Test
    @SneakyThrows
    void test27archiveStoreRetrievedChunk() {
      String message = "{"
              + "      \"message\": \"Archive Store Retrieved Chunk\","
              + "      \"eventClass\": \"org.datavaultplatform.common.event.retrieve.ArchiveStoreRetrievedChunk\","
              + "      \"nextState\": null,"
              + "      \"timestamp\": \"2022-09-16T15:12:40.150Z\","
              + "      \"sequence\": 35,"
              + "      \"persistent\": true,"
              + "      \"retrieveId\":  \"" + retrieveId + "\","
              + "      \"depositId\":  \"" + depositId + "\","
              + "      \"chunkNumber\": 2112,"
              + "      \"vaultId\"  : \"" + vaultId + "\","
              + "      \"jobId\"    : \"" + jobRetrieveId + "\","
              + "      \"userId\"   : \"" + userId + "\","
              + "      \"agent\": \"datavault-worker-1\","
              + "      \"agentType\": \"WORKER\""
              + "    }";
      long before = eventService.count();

      Event event = eventListener.onMessageInternal(message);
      assertEquals(ArchiveStoreRetrievedChunk.class, event.getClass());
      assertEquals(2112, event.getChunkNumber());
      assertEquals(ArchiveStoreRetrievedChunk.MESSAGE, event.getMessage());

      long after = eventService.count();
      assertThat(after).isEqualTo(before + 1);

      checkLastNotFailedRetrievedEvent(event, depositId, retrieveId);
    }


    @Test
    @SneakyThrows
    void test28userStoreSpaceAvailableChecked() {
      String message = "{"
              + "      \"message\": \"User Store Space Available Checked\","
              + "      \"eventClass\": \"org.datavaultplatform.common.event.retrieve.UserStoreSpaceAvailableChecked\","
              + "      \"nextState\": null,"
              + "      \"timestamp\": \"2022-09-16T15:12:40.150Z\","
              + "      \"sequence\": 35,"
              + "      \"persistent\": true,"
              + "      \"retrieveId\": \"" + retrieveId + "\","
              + "      \"depositId\":  \"" + depositId + "\","
              + "      \"vaultId\"  : \"" + vaultId + "\","
              + "      \"jobId\"    : \"" + jobRetrieveId + "\","
              + "      \"userId\"   : \"" + userId + "\","
              + "      \"agent\": \"datavault-worker-1\","
              + "      \"agentType\": \"WORKER\""
              + "    }";
      long before = eventService.count();
      Event event = eventListener.onMessageInternal(message);
      assertEquals(UserStoreSpaceAvailableChecked.class, event.getClass());
      assertEquals(UserStoreSpaceAvailableChecked.MESSAGE, event.getMessage());

      long after = eventService.count();
      assertThat(after).isEqualTo(before + 1);

      checkLastNotFailedRetrievedEvent(event, depositId, retrieveId);
    }

    @Test
    @SneakyThrows
    void test29UserStoreSpaceAvailableChecked() {
      String message = "{"
              + "      \"message\": \"User Store Space Available Checked\","
              + "      \"eventClass\": \"org.datavaultplatform.common.event.retrieve.UserStoreSpaceAvailableChecked\","
              + "      \"nextState\": null,"
              + "      \"timestamp\": \"2022-09-16T15:12:40.150Z\","
              + "      \"sequence\": 35,"
              + "      \"persistent\": true,"
              + "      \"retrieveId\": \"" + retrieveId + "\","
              + "      \"depositId\":  \"" + depositId + "\","
              + "      \"vaultId\"  :  \"" + vaultId + "\","
              + "      \"jobId\"    :  \"" + jobRetrieveId + "\","
              + "      \"userId\"   :  \"" + userId + "\","
              + "      \"agent\": \"datavault-worker-1\","
              + "      \"agentType\": \"WORKER\""
              + "    }";
      long before = eventService.count();
      Event event = eventListener.onMessageInternal(message);
      assertEquals(UserStoreSpaceAvailableChecked.class, event.getClass());
      assertEquals(UserStoreSpaceAvailableChecked.MESSAGE, event.getMessage());

      long after = eventService.count();
      assertThat(after).isEqualTo(before + 1);

      checkLastNotFailedRetrievedEvent(event, depositId, retrieveId);
    }

    @Test
    @SneakyThrows
    void testRetrieveError() {
      String message = "{"
              + "      \"message\": \"CUSTOM ERROR MESSAGE\","
              + "      \"eventClass\": \"org.datavaultplatform.common.event.retrieve.RetrieveError\","
              + "      \"nextState\": null,"
              + "      \"timestamp\": \"2022-09-16T15:12:40.150Z\","
              + "      \"sequence\": 35,"
              + "      \"persistent\": true,"
              + "      \"retrieveId\": \"" + retrieveId + "\","
              + "      \"depositId\":  \"" + depositId + "\","
              + "      \"vaultId\"  :  \"" + vaultId + "\","
              + "      \"jobId\"    :  \"" + jobRetrieveId + "\","
              + "      \"userId\"   :  \"" + userId + "\","
              + "      \"agent\": \"datavault-worker-1\","
              + "      \"agentType\": \"WORKER\""
              + "    }";
      long before = eventService.count();
      Event event = eventListener.onMessageInternal(message);
      assertEquals(RetrieveError.class, event.getClass());
      assertEquals("CUSTOM ERROR MESSAGE", event.getMessage());

      long after = eventService.count();
      assertThat(after).isEqualTo(before + 1);

      checkLastNotFailedRetrievedEvent(event, depositId, retrieveId);
    }

    @Test
    @SneakyThrows
    void test28UploadedToUserStore() {
      String message = "{"
              + "      \"message\": \"uploaded to user store\","
              + "      \"eventClass\": \"org.datavaultplatform.common.event.retrieve.UploadedToUserStore\","
              + "      \"nextState\": null,"
              + "      \"timestamp\": \"2022-09-16T15:12:40.150Z\","
              + "      \"sequence\": 35,"
              + "      \"persistent\": true,"
              + "      \"retrieveId\": \"" + retrieveId + "\","
              + "      \"depositId\":  \"" + depositId + "\","
              + "      \"vaultId\"  :  \"" + vaultId + "\","
              + "      \"jobId\"    :  \"" + jobRetrieveId + "\","
              + "      \"userId\"   :  \"" + userId + "\","
              + "      \"agent\": \"datavault-worker-1\","
              + "      \"agentType\": \"WORKER\""
              + "    }";
      long before = eventService.count();
      Event event = eventListener.onMessageInternal(message);
      assertEquals(UploadedToUserStore.class, event.getClass());
      assertEquals("uploaded to user store", event.getMessage());

      long after = eventService.count();
      assertThat(after).isEqualTo(before + 1);

      checkLastNotFailedRetrievedEvent(event, depositId, retrieveId);

    }
  }


  @Test
  @SneakyThrows
  void test13DeleteStart(){
    String message = "{"
        + "      \"message\": \"XXXXXXXXXX\","
        + "      \"retrieveId\": \"ABCDEF\","
        + "      \"eventClass\": \"org.datavaultplatform.common.event.delete.DeleteStart\","
        + "      \"timestamp\": \"2022-09-16T15:12:40.063Z\","
        + "      \"sequence\": 33,"
        + "      \"persistent\": true,"
        + "      \"depositId\": \"" + depositId + "\","
        + "      \"vaultId\"  : \"" + vaultId + "\","
        + "      \"jobId\"    : \"" + jobGenericId + "\","
        + "      \"userId\"   : \"" + userId + "\","
        + "      \"agent\": \"datavault-worker-1\","
        + "      \"agentType\": \"WORKER\""
        + "}";
    Event event = eventListener.onMessageInternal(message);
    assertEquals(DeleteStart.class, event.getClass());
  }

  @Test
  @SneakyThrows
  void test14DeleteComplete(){
    String message = "{"
        + "      \"message\": \"XXXXXXXXXX\","
        + "      \"retrieveId\": \"ABCDEF\","
        + "      \"eventClass\": \"org.datavaultplatform.common.event.delete.DeleteComplete\","
        + "      \"timestamp\": \"2022-09-16T15:12:40.063Z\","
        + "      \"sequence\": 33,"
        + "      \"persistent\": true,"
        + "      \"depositId\": \"" + depositId + "\","
        + "      \"vaultId\"  : \"" + vaultId + "\","
        + "      \"jobId\"    : \"" + jobGenericId + "\","
        + "      \"userId\"   : \"" + userId + "\","
        + "      \"agent\": \"datavault-worker-1\","
        + "      \"agentType\": \"WORKER\""
        + "}";
    Event event = eventListener.onMessageInternal(message);
    assertEquals(DeleteComplete.class, event.getClass());
  }

  @Test
  @SneakyThrows
  void test15AuditStart(){
    String message = "{"
        + "      \"message\": \"XXXXXXXXXX\","
        + "      \"eventClass\": \"org.datavaultplatform.common.event.audit.AuditStart\","
        + "      \"timestamp\": \"2022-09-16T15:12:40.063Z\","
        + "      \"sequence\": 33,"
        + "      \"persistent\": true,"
        + "      \"auditId\"  : \"" + auditId + "\","
        + "      \"depositId\": \"" + depositId + "\","
        + "      \"vaultId\"  : \"" + vaultId + "\","
        + "      \"jobId\"    : \"" + jobGenericId + "\","
        + "      \"userId\"   : \"" + userId + "\","
        + "      \"agent\": \"datavault-worker-1\","
        + "      \"agentType\": \"WORKER\""
        + "}";
    Event event = eventListener.onMessageInternal(message);
    assertEquals(AuditStart.class, event.getClass());
  }

  @Test
  @SneakyThrows
  void test16ChunkAuditStarted(){
    String message = "{"
        + "      \"message\": \"XXXXXXXXXX\","
        + "      \"eventClass\": \"org.datavaultplatform.common.event.audit.ChunkAuditStarted\","
        + "      \"timestamp\": \"2022-09-16T15:12:40.063Z\","
        + "      \"sequence\": 33,"
        + "      \"persistent\": true,"
        + "      \"chunkId\": \"" + depositChunkId1 + "\","
        + "      \"auditId\": \"" + auditId + "\","
        + "      \"depositId\": \"" + depositId + "\","
        + "      \"vaultId\"  : \"" + vaultId + "\","
        + "      \"jobId\"    : \"" + jobGenericId + "\","
        + "      \"userId\"   : \"" + userId + "\","
        + "      \"agent\": \"datavault-worker-1\","
        + "      \"agentType\": \"WORKER\""
        + "}";
    Event event = eventListener.onMessageInternal(message);
    assertEquals(ChunkAuditStarted.class, event.getClass());
  }

  @Test
  @SneakyThrows
  void test17ChunkAuditComplete() {
    String message = "{"
        + "      \"message\": \"XXXXXXXXXX\","
        + "      \"eventClass\": \"org.datavaultplatform.common.event.audit.ChunkAuditComplete\","
        + "      \"timestamp\": \"2022-09-16T15:12:40.063Z\","
        + "      \"sequence\": 33,"
        + "      \"persistent\": true,"
        + "      \"archiveId\": \"archive-id-one\", "
        + "      \"location\":  \"location-one\", "
        + "      \"chunkId\": \"" + depositChunkId1 + "\","
        + "      \"auditId\": \"" + auditId + "\","
        + "      \"depositId\": \"" + depositId + "\","
        + "      \"vaultId\"  : \"" + vaultId + "\","
        + "      \"jobId\"    : \"" + jobGenericId + "\","
        + "      \"userId\"   : \"" + userId + "\","
        + "      \"agent\": \"datavault-worker-1\","
        + "      \"agentType\": \"WORKER\""
        + "}";
    Event event = eventListener.onMessageInternal(message);
    assertEquals(ChunkAuditComplete.class, event.getClass());
  }

  @Test
  @SneakyThrows
  void test18AuditComplete() {
    String message = "{"
        + "      \"message\": \"XXXXXXXXXX\","
        + "      \"eventClass\": \"org.datavaultplatform.common.event.audit.AuditComplete\","
        + "      \"timestamp\": \"2022-09-16T15:12:40.063Z\","
        + "      \"sequence\": 33,"
        + "      \"persistent\": true,"
        + "      \"auditId\":   \"" + auditId   + "\","
        + "      \"depositId\": \"" + depositId + "\","
        + "      \"vaultId\"  : \"" + vaultId + "\","
        + "      \"jobId\"    : \"" + jobGenericId + "\","
        + "      \"userId\"   : \"" + userId + "\","
        + "      \"agent\": \"datavault-worker-1\","
        + "      \"agentType\": \"WORKER\""
        + "}";
    Event event = eventListener.onMessageInternal(message);
    assertEquals(AuditComplete.class, event.getClass());
  }

  @Test
  @SneakyThrows
  void test19AuditError() {
    String message = "{"
        + "      \"message\": \"XXXXXXXXXX\","
        + "      \"eventClass\": \"org.datavaultplatform.common.event.audit.AuditError\","
        + "      \"timestamp\": \"2022-09-16T15:12:40.063Z\","
        + "      \"sequence\": 33,"
        + "      \"persistent\": true,"
        + "      \"archiveId\": \"archive-id-one\", "
        + "      \"location\":  \"location-one\", "
        + "      \"chunkId\": \"" + depositChunkId1 + "\","
        + "      \"auditId\": \"" + auditId + "\","
        + "      \"depositId\": \"" + depositId + "\","
        + "      \"vaultId\"  : \"" + vaultId + "\","
        + "      \"jobId\"    : \"" + jobGenericId + "\","
        + "      \"userId\"   : \"" + userId + "\","
        + "      \"agent\": \"datavault-worker-1\","
        + "      \"agentType\": \"WORKER\""
        + "}";

    Event event = eventListener.onMessageInternal(message);
    assertEquals(AuditError.class, event.getClass());
  }

  @Test
  @SneakyThrows
  void test20TransferComplete() {
    String message = "{"
        + "      \"message\": \"File transfer completed\","
        + "      \"eventClass\": \"org.datavaultplatform.common.event.deposit.TransferComplete\","
        + "      \"nextState\": 2,"
        + "      \"timestamp\": \"2022-09-16T15:12:39.649Z\","
        + "      \"sequence\": 26,"
        + "      \"persistent\": true,"
        + "      \"depositId\": \"" + depositId + "\","
        + "      \"vaultId\"  : \"" + vaultId + "\","
        + "      \"jobId\"    : \"" + jobDepositId + "\","
        + "      \"userId\"   : \"" + userId + "\","
        + "      \"agent\": \"datavault-worker-1\","
        + "      \"agentType\": \"WORKER\""
        + "    }";
    Event event = eventListener.onMessageInternal(message);
    assertEquals(TransferComplete.class, event.getClass());
  }

  @Test
  @SneakyThrows
  void test21PackagingComplete() {
    String message = "{"
        + "      \"message\": \"Packaging completed\","
        + "      \"eventClass\": \"org.datavaultplatform.common.event.deposit.PackageComplete\","
        + "      \"nextState\": 3,"
        + "      \"timestamp\": \"2022-09-16T15:12:39.693Z\","
        + "      \"sequence\": 27,"
        + "      \"persistent\": true,"
        + "      \"depositId\": \"" + depositId + "\","
        + "      \"vaultId\"  : \"" + vaultId + "\","
        + "      \"jobId\"    : \"" + jobDepositId + "\","
        + "      \"userId\"   : \"" + userId + "\","
        + "      \"agent\": \"datavault-worker-1\","
        + "      \"agentType\": \"WORKER\""
        + "    }";
    Event event = eventListener.onMessageInternal(message);
    assertEquals(PackageComplete.class, event.getClass());
  }

  @Test
  @SneakyThrows
  void test22StartCopyUpload(){
    String message = "{"
        + "      \"id\": null,"
        + "      \"message\": \"Chunk 1 upload started - (org.datavaultplatform.common.storage.impl.LocalFileSystem)\","
        + "      \"eventClass\": \"org.datavaultplatform.common.event.deposit.StartCopyUpload\","
        + "      \"nextState\": null,"
        + "      \"timestamp\": \"2022-09-16T15:12:39.808Z\","
        + "      \"sequence\": 30,"
        + "      \"persistent\": true,"
        + "      \"depositId\": \"" + depositId + "\","
        + "      \"vaultId\"  : \"" + vaultId + "\","
        + "      \"jobId\"    : \"" + jobDepositId+ "\","
        + "      \"userId\"   : \"" + userId + "\","
        + "      \"agent\": \"datavault-worker-1\","
        + "      \"agentType\": \"WORKER\""
        + "    }";
    Event event = eventListener.onMessageInternal(message);
    assertEquals(StartCopyUpload.class, event.getClass());
  }

  @Test
  @SneakyThrows
  void test23CompleteCopyUpload() {
    String message = "{"
        + "      \"id\": null,"
        + "      \"message\": \"Chunk 1 upload finished - (org.datavaultplatform.common.storage.impl.LocalFileSystem)\","
        + "      \"retrieveId\": null,"
        + "      \"eventClass\": \"org.datavaultplatform.common.event.deposit.CompleteCopyUpload\","
        + "      \"nextState\": null,"
        + "      \"timestamp\": \"2022-09-16T15:12:39.813Z\","
        + "      \"sequence\": 31,"
        + "      \"persistent\": true,"
        + "      \"depositId\": \"" + depositId + "\","
        + "      \"vaultId\"  : \"" + vaultId + "\","
        + "      \"jobId\"    : \"" + jobDepositId + "\","
        + "      \"userId\"   : \"" + userId + "\","
        + "      \"agent\": \"datavault-worker-1\","
        + "      \"agentType\": \"WORKER\""
        + "    }";
    Event event = eventListener.onMessageInternal(message);
    assertEquals(CompleteCopyUpload.class, event.getClass());
  }

  @Test
  @SneakyThrows
  void test24ValidationComplete() {
    String message = "{"
        + "      \"message\": \"Validation completed\","
        + "      \"eventClass\": \"org.datavaultplatform.common.event.deposit.ValidationComplete\","
        + "      \"nextState\": null,"
        + "      \"timestamp\": \"2022-09-16T15:12:40.150Z\","
        + "      \"sequence\": 35,"
        + "      \"persistent\": true,"
        + "      \"depositId\": \"" + depositId + "\","
        + "      \"vaultId\"  : \"" + vaultId + "\","
        + "      \"jobId\"    : \"" + jobDepositId + "\","
        + "      \"userId\"   : \"" + userId + "\","
        + "      \"agent\": \"datavault-worker-1\","
        + "      \"agentType\": \"WORKER\""
        + "    }";
    Event event = eventListener.onMessageInternal(message);
    assertEquals(ValidationComplete.class, event.getClass());
  }
}
