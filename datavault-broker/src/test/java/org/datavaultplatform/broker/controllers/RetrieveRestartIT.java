package org.datavaultplatform.broker.controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.datavaultplatform.broker.app.DataVaultBrokerApp;
import org.datavaultplatform.broker.email.EmailBodyGenerator;
import org.datavaultplatform.broker.queue.MessageIdProcessedListener;
import org.datavaultplatform.broker.services.*;
import org.datavaultplatform.broker.test.AddTestProperties;
import org.datavaultplatform.broker.test.BaseDatabaseTest;
import org.datavaultplatform.common.PropNames;
import org.datavaultplatform.common.config.BaseQueueConfig;
import org.datavaultplatform.common.docker.DockerImage;
import org.datavaultplatform.common.event.Error;
import org.datavaultplatform.common.event.Event;
import org.datavaultplatform.common.event.InitStates;
import org.datavaultplatform.common.event.UpdateProgress;
import org.datavaultplatform.common.event.deposit.*;
import org.datavaultplatform.common.event.retrieve.RetrieveStart;
import org.datavaultplatform.common.model.*;
import org.datavaultplatform.common.model.dao.JobDAO;
import org.datavaultplatform.common.request.CreateDeposit;
import org.datavaultplatform.common.response.DepositInfo;
import org.datavaultplatform.common.storage.impl.LocalFileSystem;
import org.datavaultplatform.common.task.Task;
import org.datavaultplatform.common.util.TestUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.skyscreamer.jsonassert.JSONAssert;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.core.QueueInformation;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.junit.jupiter.Container;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;

@Sql(scripts = "classpath:db/retrieveRestart.sql")
@SpringBootTest(classes = DataVaultBrokerApp.class)
@AddTestProperties
@Slf4j
@TestPropertySource(properties = {
        "logging.level.org.springframework.security=TRACE",
        "broker.security.enabled=true",
        "broker.scheduled.enabled=false",
        "broker.controllers.enabled=true",
        "broker.services.enabled=true",
        "broker.rabbit.enabled=true",
        "broker.ldap.enabled=false",
        "broker.initialise.enabled=false",
        "broker.email.enabled=false",
        "broker.database.enabled=true",
        "spring.jpa.properties.hibernate.enable_lazy_load_no_trans=true"
})

class RetrieveRestartIT extends BaseDatabaseTest {

    private static final String ADMIN_USER_ID = "admin1";
    
    @Autowired
    ArchivesService archivesService;
    
    @Autowired
    RetentionPoliciesService rpService;
    
    @Autowired
    DepositsController depositsController;
    
    @MockBean
    JavaMailSender mailSender;

    @Autowired
    DepositsService depositsService;
    
    @Autowired
    ArchiveStoreService archiveStoreService;
    
    @Autowired
    UsersService usersService;

    @Autowired
    VaultsService vaultsService;

    @Autowired
    GroupsService groupsService;

    @MockBean
    EmailBodyGenerator emailBodyGenerator;
    
    @Autowired
    JobsService jobsService;
    
    @Autowired
    FileStoreService fileStoreService;
    
    @Autowired
    JobDAO jobDAO;
    
    @Autowired
    ObjectMapper mapper;
    
    @Container
    @ServiceConnection
    private static final RabbitMQContainer RABBIT = new RabbitMQContainer(DockerImage.RABBIT_IMAGE_NAME)
            .withExposedPorts(5672,15672);

    @PostConstruct
    void init() {
        log.info("RABBIT HTTP URL [ {} ]",RABBIT.getHttpUrl());
    }

    @Value(BaseQueueConfig.WORKER_QUEUE_NAME)
    String queueNameToWorker;

    @Value(BaseQueueConfig.BROKER_QUEUE_NAME)
    String queueNameToBroker;

    @Value(BaseQueueConfig.RESTART_WORKER_ONE_QUEUE_NAME)
    String queueNameWorker1Restart;
    @Value(BaseQueueConfig.RESTART_WORKER_TWO_QUEUE_NAME)
    String queueNameWorker2Restart;
    @Value(BaseQueueConfig.RESTART_WORKER_THREE_QUEUE_NAME)
    String queueNameWorker3Restart;

    @Autowired
    AmqpAdmin amqpAdmin;
    
    @Autowired
    RabbitTemplate rabbitTemplate;
    
    @MockBean
    MessageIdProcessedListener mMessageIdProcessedListener;
    
    List<String> processedMessageIds;
    
    protected org.datavaultplatform.common.model.ArchiveStore archiveStore;
    
    @BeforeEach
    void setup() {
        QueueInformation qInfoToWorker = amqpAdmin.getQueueInfo(queueNameToWorker);
        log.info("TO WORKER [{}]", qInfoToWorker);
        
        QueueInformation qInfoToBroker = amqpAdmin.getQueueInfo(queueNameToBroker);
        log.info("TO BROKER [{}]", qInfoToBroker);
        
        processedMessageIds = new ArrayList<>();
        Mockito.doAnswer(invocation -> {
            String messageId = invocation.getArgument(0, String.class);
            processedMessageIds.add(messageId);
            return null;
        }).when(mMessageIdProcessedListener).processedMessageId(any(String.class));

        archiveStore = archiveStoreService.getForRetrieval();
        assertThat(archiveStore.getID()).isEqualTo("test-lfs-id");
        assertThat(archiveStore.getLabel()).isEqualTo("test-lfs-label");
        assertThat(archiveStore.getStorageClass()).isEqualTo(LocalFileSystem.class.getName());
        assertThat(archiveStore.isRetrieveEnabled()).isEqualTo(true);
        assertThat(archiveStore.getProperties()).isEmpty();
    }
    
    @Test
    @SneakyThrows
    void testRetrieveRestart() {
        
        List<RetentionPolicy> rps = rpService.getRetentionPolicies();
        Collections.sort(rps, Comparator.comparing(RetentionPolicy::getID));
        long initialRetentionPolicies = rps.size();
        
        long initialDepositsCount = depositsService.count(ADMIN_USER_ID);
        long initialUserCount = usersService.getUsers().size();
        long initialVaultCount = vaultsService.getVaults().size();
        long initialGroupCount = groupsService.getGroups().size();
        long initialArchivesCount = archivesService.getArchives().size();
        
        Group group1 = groupsService.getGroups().get(0);
        
        System.out.printf("existing deposits [%s]%n", initialDepositsCount);
        System.out.printf("existing users [%s]%n", initialUserCount);
        System.out.printf("existing vaults [%s]%n", initialVaultCount);
        System.out.printf("existing retention policies [%s]%n", initialRetentionPolicies);
        System.out.printf("existing groups [%s]%n", initialGroupCount);
        System.out.printf("existing archives [%s]%n", initialArchivesCount);

        assertThat(initialDepositsCount).isZero();
        assertThat(initialUserCount).isEqualTo(2);
        assertThat(initialVaultCount).isEqualTo(0);
        assertThat(initialArchivesCount).isEqualTo(0);
        
        List<String> userids = usersService.getUsers().stream().map(User::getID).sorted().toList();
        assertThat(userids).isEqualTo(List.of(ADMIN_USER_ID,"user1"));

        User adminUser = usersService.getUser(ADMIN_USER_ID);

        HashMap<String, String> props = new HashMap<>();
        props.put(PropNames.ROOT_PATH, "/tmp");
        FileStore fileStore1 = new FileStore(LocalFileSystem.class.getName(), props,"UFS1");
        fileStore1.setUser(adminUser);
        fileStoreService.addFileStore(fileStore1);

        Vault vault = new Vault();
        vault.setReviewDate(new Date());
        vault.setRetentionPolicyStatus(123);
        vault.setName("test vault");
        vault.setDescription("test vault description");
        vault.setCreationTime(new Date());
        vault.setContact("Vault Owner");
        vault.setRetentionPolicy(rps.get(0));
        vault.setGroup(group1);

        vaultsService.addVault(vault);
        
        String vaultId = vault.getID();
        
        CreateDeposit createDeposit = new CreateDeposit();
        createDeposit.setVaultID(vaultId);
        createDeposit.setName("test deposit");
        createDeposit.setDescription("test deposit description");
        createDeposit.setHasPersonalData("nope");
        createDeposit.setFileUploadHandle("fileUploadHandle");
        createDeposit.setPersonalDataStatement("personal data statement");
        
        ResponseEntity<DepositInfo> response1 = depositsController.addDeposit(ADMIN_USER_ID, createDeposit);
        DepositInfo depositInfo = response1.getBody();
        
        String depositID = depositInfo.getID();
        
        Deposit deposit = depositsService.getDepositForRetrieves(depositID);
        String bagId = deposit.getBagId();
        assertThat(bagId).isNotBlank();

        archivesService.addArchive(deposit, archiveStore, "AS1");
        
        Deposit deposit2 = depositsService.getDepositForRetrieves(depositID);

        // SEND DEPOSITS EVENTS AND PROCESS THEM
        sendDepositEventsFromBroker(deposit.getID());
        
        
        // 1st RETRIEVE (a non-restart)

        Retrieve retrieve = new Retrieve();
        retrieve.setStatus(Retrieve.Status.NOT_STARTED);
        retrieve.setUser(adminUser);
        retrieve.setNote("test note");
        retrieve.setRetrievePath(fileStore1.getID()+"/A/B/C");
        retrieve.setHasExternalRecipients(false);
        retrieve.setTimestamp(new Date());
        retrieve.setDeposit(deposit2);

        boolean result = depositsController.retrieveDeposit(ADMIN_USER_ID, depositID, retrieve);
        assertThat(result).isTrue();
        
        Message depositMessage = rabbitTemplate.receive(this.queueNameToWorker);
        String depositMessageJson = toPrettyJson(toString(depositMessage));
        log.info("deposit message {}", depositMessageJson);

        
        Message retrieveMessage = rabbitTemplate.receive(this.queueNameToWorker);
        String retrieveMessageJson = toPrettyJson(toString(retrieveMessage));
        log.info("initial retrieve message {}", retrieveMessageJson);
        
        Task retrieveTask = mapper.readValue(retrieveMessageJson, Task.class);
        assertThat(retrieveTask.getLastEvent()).isNull();
        
        // simiulate Worker sending a couple of events back to Broker
        
        Job latestJobForDeposit = jobDAO.findByDepositIdOrderByTimestampDesc(depositID).get(0);
        
        InitStates retrieveInitStates = new InitStates();
        retrieveInitStates.setRetrieveId(retrieve.getID());
        retrieveInitStates.setDepositId(deposit.getID());
        retrieveInitStates.setUserId(deposit.getUser().getID());
        retrieveInitStates.setJobId(latestJobForDeposit.getID());
        
        ArrayList<String> retrieveStates = new ArrayList<>();
        retrieveStates.addAll(List.of("Computing free space",
                "Retrieving from archive",
                "Validating data",
                "Transferring files",
                "Data retrieve complete"));
        retrieveInitStates.setStates(retrieveStates);
        
        String retrieveInitStatesId = sendBrokerEvent(retrieveInitStates);
        
        TestUtils.waitUntil(() -> processedMessageIds.contains(retrieveInitStatesId));

        RetrieveStart retrieveStart = new RetrieveStart();
        retrieveStart.setRetrieveId(retrieve.getID());
        retrieveStart.setDepositId(deposit.getID());
        retrieveStart.setUserId(deposit.getUser().getID());
        retrieveStart.setJobId(latestJobForDeposit.getID());

        String retrieveStartId = sendBrokerEvent(retrieveStart);
        TestUtils.waitUntil(() -> processedMessageIds.contains(retrieveStartId));

        Error error = new Error();
        error.setDepositId(deposit.getID());
        error.setRetrieveId(retrieve.getID());
        error.setUserId(deposit.getUser().getID());
        error.setJobId(latestJobForDeposit.getID());
        
        String errorId = sendBrokerEvent(error);
        TestUtils.waitUntil(() -> processedMessageIds.contains(errorId));

        // 2nd RETRIEVE (a restart)

        String retrieveId = retrieve.getID();

        assertThat(rabbitTemplate.getUnconfirmedCount()).isEqualTo(0);

        depositsController.retrieveRestart(retrieveId);

        Callable<Boolean> threeRestarts = () -> Stream.of(queueNameWorker1Restart, queueNameWorker2Restart, queueNameWorker3Restart)
                .map(amqpAdmin::getQueueInfo)
                .map(info -> info.getMessageCount() == 1)
                .allMatch(Boolean::valueOf);
        
        TestUtils.waitUntil(threeRestarts);

        Message retrieveRestartMessage = rabbitTemplate.receive(this.queueNameWorker1Restart);
        String retrieveRestartMessageJson = toPrettyJson(toString(retrieveRestartMessage));
        log.info("restart retrieve message {}", retrieveRestartMessageJson);
        
        // the original retrieve and the restart retrive are different
        JSONAssert.assertNotEquals(retrieveMessageJson, retrieveRestartMessageJson, false);
        
        Task origRetrieveTask = mapper.readValue(retrieveMessageJson, Task.class);
        Task restartRetrieveTask = mapper.readValue(retrieveRestartMessageJson, Task.class);
        
        // the original retrieve has no lastEvent
        assertThat(origRetrieveTask.getLastEvent()).isNull();
        
        // the original retrieve the jobId is the same as the nonRestartJobId
        assertThat(origRetrieveTask.getJobID()).isEqualTo(getNonRestartJobId(origRetrieveTask));

        // the restart has a 'last event'
        assertThat(restartRetrieveTask.getLastEvent()).isNotNull();
        
        // the restart nonRestartJobId is the same as the jobId in the original retrieve message
        assertThat(origRetrieveTask.getJobID()).isEqualTo(getNonRestartJobId(restartRetrieveTask));

        // if we make 2 changes to the origRetrieveTask structure
        origRetrieveTask.setLastEvent(restartRetrieveTask.getLastEvent());
        origRetrieveTask.setJobID(restartRetrieveTask.getJobID());
        
        // and write back to json to create 'retrieveRestartMessageJson2'
        String retrieveRestartMessageJson2 = mapper.writeValueAsString(origRetrieveTask);
        
        // we should see the 'retrieveRestartMessageJson2' is the same as 'retrieveRestartMessageJson'
        JSONAssert.assertEquals(retrieveRestartMessageJson, retrieveRestartMessageJson2, true);
    }
    
    private String getNonRestartJobId(Task task){
        return task.getProperties().get(PropNames.NON_RESTART_JOB_ID);
    }

    private void sendDepositEventsFromBroker(String depositId) {
        Deposit deposit = depositsService.getDeposit(depositId);
        String bagId = deposit.getBagId();
        HashMap archiveIdsHashMap = new HashMap();
        archiveIdsHashMap.put("AS1", bagId+".tar");

        InitStates event1 = createEvent(InitStates.class, deposit);
        event1.setStates(new ArrayList<>(List.of("Calculating size",
                "Transferring",
                "Packaging",
                "Storing in archive",
                "Verifying",
                "Complete")));

        Start event2 = createEvent(Start.class, deposit);
        event2.withNextState(0);

        ComputedSize event3 = createEvent(ComputedSize.class, deposit);
        event3.setBytes(50_000);

        UpdateProgress event4 = createEvent(UpdateProgress.class, deposit);
        event4.withNextState(1);

        UpdateProgress event5 = createEvent(UpdateProgress.class, deposit);
        UpdateProgress event6 = createEvent(UpdateProgress.class, deposit);

        TransferComplete event7 = createEvent(TransferComplete.class, deposit);
        event7.withNextState(2);

        PackageComplete event8 = createEvent(PackageComplete.class, deposit);
        event8.withNextState(3);

        ComputedDigest event9 = createEvent(ComputedDigest.class, deposit);
        event9.setDigest("E11D93E31DDB4912C080C796B7FADF5C3574AFFC");
        event9.setDigestAlgorithm("SHA-1");

        HashMap<Integer,byte[]> chunksIVs = new HashMap<>();
        chunksIVs.put(1, "XXoD41zcqXc7PdpbaeG0/PDozrkSDIva2b5mpi2m6YGl7w17nWWrTy82cFGUp14rOAx99j/fRnpOigGh831CTkDnVx/s06TNajgPd7V7GEM7lhBteQhqzYBSs0i0xg53".getBytes(StandardCharsets.UTF_8));
        chunksIVs.put(2, "lRZZ2cGACmYdAUAdFiQlkAwgaEnn/y+p+i/Osk26S6i/FoyKO048d40QfEU+ln7cjY7Fj5x8zOqmebEHc5ucPi+SpJQ32+xeCMsvZbBBj1NZUEb9A1r9teO6hk39CM+u".getBytes(StandardCharsets.UTF_8));
        chunksIVs.put(3, "Wgz2rdaeDGi359Z0FZgXK1gTzomWLf3G91oavTo7ndKV1DlmzkDyLmXAVbjjRQ376p9J41oDqVVefTmP0Fe8c3dbKeHRy5SjopsCdiOwff5gqmjTwMLBy8bhdytGR6Q+".getBytes(StandardCharsets.UTF_8));

        HashMap<Integer, String> encChunksDigests = new HashMap<>();
        encChunksDigests.put(1, "7513E3A86A8E5E7859FB55A18C5FF70636C6D859");
        encChunksDigests.put(2, "2ADC070DC2CE9664DC2C0EED1D813343235B5102");
        encChunksDigests.put(3, "929B8AA1FE0C82400E7150F326D62FDB87839E12");

        HashMap<Integer, String> chunksDigest = new HashMap<>();
        chunksDigest.put(1, "E3670435119D9F4B56F014DB315EBB902FD5E309");
        chunksDigest.put(2, "A9C71EE106C392D1B20A00066912480993DB5457");
        chunksDigest.put(3, "84BB51D3FF74D103C0D74795C5C2E801F53AB0B2");

        ComputedEncryption event10 = createEvent(ComputedEncryption.class, deposit);
        event10.setChunkIVs(chunksIVs);
        event10.setEncChunkDigests(encChunksDigests);
        event10.setChunksDigest(chunksDigest);
        event10.setAesMode("GCM");

        StartCopyUpload event11 = createEvent(StartCopyUpload.class, deposit);
        StartCopyUpload event12 = createEvent(StartCopyUpload.class, deposit);
        StartCopyUpload event13 = createEvent(StartCopyUpload.class, deposit);

        CompleteCopyUpload event14 = createEvent(CompleteCopyUpload.class, deposit);
        event14.setArchiveId(bagId+".tar.1");
        event14.setChunkNumber(1);
        //event14.setArchiveStoreId(archiveStore.getID());

        CompleteCopyUpload event15 = createEvent(CompleteCopyUpload.class, deposit);
        event15.setArchiveId(bagId+".tar.2");
        event15.setChunkNumber(2);
        //event15.setArchiveStoreId(archiveStore.getID());

        CompleteCopyUpload event16 = createEvent(CompleteCopyUpload.class, deposit);
        event16.setArchiveId(bagId+".tar.3");
        event16.setChunkNumber(3);
        //event16.setArchiveStoreId(archiveStore.getID());

        UpdateProgress event17 = createEvent(UpdateProgress.class, deposit);
        UpdateProgress event18 = createEvent(UpdateProgress.class, deposit);
        UpdateProgress event19 = createEvent(UpdateProgress.class, deposit);

        UpdateProgress event20 = createEvent(UpdateProgress.class, deposit);

        UploadComplete event21 = createEvent(UploadComplete.class, deposit);
        event21.setArchiveIds(archiveIdsHashMap);
        event21.setArchiveStoreId(archiveStore.getID());
        event21.withNextState(4);

        ValidationComplete event22 = createEvent(ValidationComplete.class, deposit);

        Complete event23 = createEvent(Complete.class, deposit);
        event23.setArchiveIds(archiveIdsHashMap);
        event23.withNextState(5);
        event23.setArchiveSize(50022400);

        List<Event> events = List.of(
                event1, event2, event3, event4, event5,
                event6, event7, event8, event9, event10,
                event11, event12, event13, event14, event15,
                event16, event17, event18, event19, event20,
                event21, event22, event23 );
        
        int count = events.size();
        for(int i=0;i<events.size();i++) {
            Event event = events.get(i);
            String messageId = sendBrokerEvent(event);
            TestUtils.waitUntil(() -> processedMessageIds.contains(messageId));
            int idx = i  + 1;
            log.info("broker processed message id [{}/{}] [{}] - ",idx, count, messageId);
        }
        assertThat(processedMessageIds).hasSize(23);
        System.out.println("PROCESSED ALL BROKER EVENTS");
    }

    @SneakyThrows
    private String sendBrokerEvent(Event event) {
        String json = mapper.writeValueAsString(event);
        MessageProperties props = new MessageProperties();
        props.setMessageId(UUID.randomUUID().toString());
        Message message = new Message(json.getBytes(StandardCharsets.UTF_8), props);
        rabbitTemplate.send(this.queueNameToBroker, message);
        return props.getMessageId();
    }

    @SneakyThrows
    private <T extends Event>  T createEvent(Class<T> eventClass, Deposit deposit) {
        T event = eventClass.getConstructor().newInstance();
        event.setDepositId(deposit.getID());
        event.setJobId(jobDAO.findByDepositIdOrderByTimestampDesc(deposit.getID()).get(0).getID());
        event.setUserId(deposit.getUser().getID());
        event.setTimestamp(new Date());
        event.setAgent("43068@datavault-worker");
        event.setAgentType(Agent.AgentType.WORKER);
        event.setEventClass(eventClass.getName());
        event.setMessage("MESSAGE FOR - " + event.getEventClass());
        return event;
    }
    
    private String toString(Message message){
        return new String(message.getBody(), StandardCharsets.UTF_8);
    }
    @SneakyThrows
    private String toPrettyJson(String rawJson) {
        JsonNode node = mapper.readTree(rawJson);
        String result = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(node);
        return result;
    }
    
}
