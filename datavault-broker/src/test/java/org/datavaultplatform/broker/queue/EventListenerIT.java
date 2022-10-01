package org.datavaultplatform.broker.queue;

import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.datavaultplatform.broker.app.DataVaultBrokerApp;
import org.datavaultplatform.broker.services.DepositsService;
import org.datavaultplatform.broker.services.EmailService;
import org.datavaultplatform.broker.services.GroupsService;
import org.datavaultplatform.broker.services.JobsService;
import org.datavaultplatform.broker.services.UsersService;
import org.datavaultplatform.broker.services.VaultsService;
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
import org.datavaultplatform.common.event.retrieve.RetrieveComplete;
import org.datavaultplatform.common.event.retrieve.RetrieveStart;
import org.datavaultplatform.common.model.Deposit;
import org.datavaultplatform.common.model.DepositChunk;
import org.datavaultplatform.common.model.Group;
import org.datavaultplatform.common.model.Job;
import org.datavaultplatform.common.model.User;
import org.datavaultplatform.common.model.Vault;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
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
@Import({EventListener.class})
@Slf4j
@TestMethodOrder(MethodOrderer.MethodName.class)
public class EventListenerIT extends BaseDatabaseTest {

  @MockBean
  EmailService emailService;

  @MockBean
  Sender sender;

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

  @MockBean
  RabbitListenerEndpointRegistry registry;
  private String userId = "user123";
  private String bagId = "bag123";
  private String vaultId;
  private String jobId;
  private String depositId;

  Job job;
  Vault vault;
  Deposit deposit;
  User user;

  Group group;

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

    deposit = new Deposit();
    deposit.setName("test-deposit");
    deposit.setHasPersonalData(false);
    deposit.setVault(vault);
    deposit.setUser(user);
    depositsService.addDeposit(vault, deposit, "shortPath", "origin");
    this.bagId = deposit.getBagId();
    this.depositId = deposit.getID();

    job = new Job();
    jobsService.addJob(deposit, job);
    this.jobId = job.getID();

  }

  @Test
  void test00EventListener() {
    assertTrue(eventListener != null);
    assertTrue(usersService != null);
    assertTrue(jobsService != null);
    assertTrue(vaultsService != null);
    assertTrue(depositsService != null);
    assertTrue(groupService != null);
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
        + "      \"jobId\"    : \"" + jobId + "\","
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

    Job job = jobsService.getJob(jobId);
    assertEquals(Arrays.asList(
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
        + "      \"jobId\"    : \"" + jobId + "\","
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
        + "      \"jobId\"    : \"" + jobId + "\","
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
        + "      \"jobId\"    : \"" + jobId + "\","
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
        + "      \"jobId\"    : \"" + jobId + "\","
        + "      \"userId\"   : \"" + userId + "\","
        + "      \"agent\": \"datavault-worker-1\","
        + "      \"agentType\": \"WORKER\""
        + "    }";

    assertEquals(null, job.getState());
    Event event = eventListener.onMessageInternal(message);
    assertEquals(Start.class, event.getClass());

    job = jobsService.getJob(jobId);
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
        + "      \"jobId\"    : \"" + jobId     + "\","
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
        + "      \"jobId\"    : \"" + jobId     + "\","
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
        + "      \"jobId\"    : \"" + jobId + "\","
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
    assertEquals("SHA-1", chunk1.getArchiveDigestAlgorithm());
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
        + "      \"jobId\"    : \"" + jobId + "\","
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
        + "      \"jobId\"    : \"" + jobId + "\","
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
        + "      \"jobId\"    : \"" + jobId + "\","
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
        + "      \"jobId\"    : \"" + jobId + "\","
        + "      \"userId\"   : \"" + userId + "\","
        + "      \"agent\": \"datavault-worker-1\","
        + "      \"agentType\": \"WORKER\""
        + "    }";
    Event event = eventListener.onMessageInternal(message);
    assertEquals(org.datavaultplatform.common.event.Error.class, event.getClass());
  }

  @Disabled
  @Test
  @SneakyThrows
  void test11RetrieveStart(){
    String message = "{"
        + "      \"message\": \"XXXXXXXXXX\","
        + "      \"retrieveId\": \"ABCDEF\","
        + "      \"eventClass\": \"org.datavaultplatform.common.event.retrieve.RetrieveStart\","
        + "      \"timestamp\": \"2022-09-16T15:12:40.063Z\","
        + "      \"sequence\": 33,"
        + "      \"persistent\": true,"
        + "      \"depositId\": \"" + depositId + "\","
        + "      \"vaultId\"  : \"" + vaultId + "\","
        + "      \"jobId\"    : \"" + jobId + "\","
        + "      \"userId\"   : \"" + userId + "\","
        + "      \"agent\": \"datavault-worker-1\","
        + "      \"agentType\": \"WORKER\""
        + "}";
    Event event = eventListener.onMessageInternal(message);
    assertEquals(RetrieveStart.class, event.getClass());
  }

  @Disabled
  @Test
  @SneakyThrows
  void test12RetrieveComplete(){
    String message = "{"
        + "      \"message\": \"XXXXXXXXXX\","
        + "      \"retrieveId\": \"ABCDEF\","
        + "      \"eventClass\": \"org.datavaultplatform.common.event.retrieve.RetrieveComplete\","
        + "      \"timestamp\": \"2022-09-16T15:12:40.063Z\","
        + "      \"sequence\": 33,"
        + "      \"persistent\": true,"
        + "      \"depositId\": \"" + depositId + "\","
        + "      \"vaultId\"  : \"" + vaultId + "\","
        + "      \"jobId\"    : \"" + jobId + "\","
        + "      \"userId\"   : \"" + userId + "\","
        + "      \"agent\": \"datavault-worker-1\","
        + "      \"agentType\": \"WORKER\""
        + "}";
    Event event = eventListener.onMessageInternal(message);
    assertEquals(RetrieveComplete.class, event.getClass());
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
        + "      \"jobId\"    : \"" + jobId + "\","
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
        + "      \"jobId\"    : \"" + jobId + "\","
        + "      \"userId\"   : \"" + userId + "\","
        + "      \"agent\": \"datavault-worker-1\","
        + "      \"agentType\": \"WORKER\""
        + "}";
    Event event = eventListener.onMessageInternal(message);
    assertEquals(DeleteComplete.class, event.getClass());
  }

  @Disabled
  @Test
  @SneakyThrows
  void test15AuditStart(){
    String message = "{"
        + "      \"message\": \"XXXXXXXXXX\","
        + "      \"auditId\": \"ABCDEF\","
        + "      \"eventClass\": \"org.datavaultplatform.common.event.audit.AuditStart\","
        + "      \"timestamp\": \"2022-09-16T15:12:40.063Z\","
        + "      \"sequence\": 33,"
        + "      \"persistent\": true,"
        + "      \"depositId\": \"" + depositId + "\","
        + "      \"vaultId\"  : \"" + vaultId + "\","
        + "      \"jobId\"    : \"" + jobId + "\","
        + "      \"userId\"   : \"" + userId + "\","
        + "      \"agent\": \"datavault-worker-1\","
        + "      \"agentType\": \"WORKER\""
        + "}";
    Event event = eventListener.onMessageInternal(message);
    assertEquals(AuditStart.class, event.getClass());
  }

  @Disabled
  @Test
  @SneakyThrows
  void test16ChunkAuditStarted(){
    String message = "{"
        + "      \"message\": \"XXXXXXXXXX\","
        + "      \"auditId\": \"AUDIT-ID\","
        + "      \"chunkId\": \"CHUNK-ID\","
        + "      \"eventClass\": \"org.datavaultplatform.common.event.audit.ChunkAuditStarted\","
        + "      \"timestamp\": \"2022-09-16T15:12:40.063Z\","
        + "      \"sequence\": 33,"
        + "      \"persistent\": true,"
        + "      \"depositId\": \"" + depositId + "\","
        + "      \"vaultId\"  : \"" + vaultId + "\","
        + "      \"jobId\"    : \"" + jobId + "\","
        + "      \"userId\"   : \"" + userId + "\","
        + "      \"agent\": \"datavault-worker-1\","
        + "      \"agentType\": \"WORKER\""
        + "}";
    Event event = eventListener.onMessageInternal(message);
    assertEquals(ChunkAuditStarted.class, event.getClass());
  }

  @Test
  @SneakyThrows
  @Disabled
  void test17ChunkAuditComplete() {
    String message = "{"
        + "      \"message\": \"XXXXXXXXXX\","
        + "      \"auditId\": \"AUDIT-ID\","
        + "      \"chunkId\": \"CHUNK-ID\","
        + "      \"eventClass\": \"org.datavaultplatform.common.event.audit.ChunkAuditComplete\","
        + "      \"timestamp\": \"2022-09-16T15:12:40.063Z\","
        + "      \"sequence\": 33,"
        + "      \"persistent\": true,"
        + "      \"depositId\": \"" + depositId + "\","
        + "      \"vaultId\"  : \"" + vaultId + "\","
        + "      \"jobId\"    : \"" + jobId + "\","
        + "      \"userId\"   : \"" + userId + "\","
        + "      \"agent\": \"datavault-worker-1\","
        + "      \"agentType\": \"WORKER\""
        + "}";
    Event event = eventListener.onMessageInternal(message);
    assertEquals(ChunkAuditComplete.class, event.getClass());
  }

  @Test
  @SneakyThrows
  @Disabled
  void test18AuditComplete() {
    String message = "{"
        + "      \"message\": \"XXXXXXXXXX\","
        + "      \"auditId\": \"ABCDEF\","
        + "      \"eventClass\": \"org.datavaultplatform.common.event.audit.AuditComplete\","
        + "      \"timestamp\": \"2022-09-16T15:12:40.063Z\","
        + "      \"sequence\": 33,"
        + "      \"persistent\": true,"
        + "      \"depositId\": \"" + depositId + "\","
        + "      \"vaultId\"  : \"" + vaultId + "\","
        + "      \"jobId\"    : \"" + jobId + "\","
        + "      \"userId\"   : \"" + userId + "\","
        + "      \"agent\": \"datavault-worker-1\","
        + "      \"agentType\": \"WORKER\""
        + "}";
    Event event = eventListener.onMessageInternal(message);
    assertEquals(AuditComplete.class, event.getClass());
  }

  @Test
  @SneakyThrows
  @Disabled
  void test19AuditError() {
    String message = "{"
        + "      \"message\": \"XXXXXXXXXX\","
        + "      \"auditId\": \"ABCDEF\","
        + "      \"eventClass\": \"org.datavaultplatform.common.event.audit.AuditError\","
        + "      \"timestamp\": \"2022-09-16T15:12:40.063Z\","
        + "      \"sequence\": 33,"
        + "      \"persistent\": true,"
        + "      \"depositId\": \"" + depositId + "\","
        + "      \"vaultId\"  : \"" + vaultId + "\","
        + "      \"jobId\"    : \"" + jobId + "\","
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
        + "      \"jobId\"    : \"" + jobId + "\","
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
        + "      \"jobId\"    : \"" + jobId + "\","
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
        + "      \"jobId\"    : \"" + jobId + "\","
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
        + "      \"jobId\"    : \"" + jobId + "\","
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
        + "      \"jobId\"    : \"" + jobId + "\","
        + "      \"userId\"   : \"" + userId + "\","
        + "      \"agent\": \"datavault-worker-1\","
        + "      \"agentType\": \"WORKER\""
        + "    }";
    Event event = eventListener.onMessageInternal(message);
    assertEquals(ValidationComplete.class, event.getClass());
  }
}
