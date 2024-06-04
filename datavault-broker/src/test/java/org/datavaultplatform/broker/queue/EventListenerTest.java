package org.datavaultplatform.broker.queue;

import static org.assertj.core.api.Assertions.assertThat;
import static org.datavaultplatform.broker.queue.EventListener.EMAIL_KEY_DEPOSIT_ID;
import static org.datavaultplatform.broker.queue.EventListener.EMAIL_KEY_DEPOSIT_NAME;
import static org.datavaultplatform.broker.queue.EventListener.EMAIL_KEY_ERROR_MESSAGE;
import static org.datavaultplatform.broker.queue.EventListener.EMAIL_KEY_GROUP_NAME;
import static org.datavaultplatform.broker.queue.EventListener.EMAIL_KEY_HAS_PERSONAL_DATA;
import static org.datavaultplatform.broker.queue.EventListener.EMAIL_KEY_HELP_MAIL;
import static org.datavaultplatform.broker.queue.EventListener.EMAIL_KEY_HELP_PAGE;
import static org.datavaultplatform.broker.queue.EventListener.EMAIL_KEY_HOME_PAGE;
import static org.datavaultplatform.broker.queue.EventListener.EMAIL_KEY_RETRIEVER_FIRSTNAME;
import static org.datavaultplatform.broker.queue.EventListener.EMAIL_KEY_RETRIEVER_ID;
import static org.datavaultplatform.broker.queue.EventListener.EMAIL_KEY_RETRIEVER_LASTNAME;
import static org.datavaultplatform.broker.queue.EventListener.EMAIL_KEY_SIZE_BYTES;
import static org.datavaultplatform.broker.queue.EventListener.EMAIL_KEY_TIMESTAMP;
import static org.datavaultplatform.broker.queue.EventListener.EMAIL_KEY_USER_FIRSTNAME;
import static org.datavaultplatform.broker.queue.EventListener.EMAIL_KEY_USER_ID;
import static org.datavaultplatform.broker.queue.EventListener.EMAIL_KEY_USER_LASTNAME;
import static org.datavaultplatform.broker.queue.EventListener.EMAIL_KEY_VAULT_ID;
import static org.datavaultplatform.broker.queue.EventListener.EMAIL_KEY_VAULT_NAME;
import static org.datavaultplatform.broker.queue.EventListener.EMAIL_KEY_VAULT_REVIEW_DATE;
import static org.datavaultplatform.broker.queue.EventListener.TYPE_COMPLETE;
import static org.datavaultplatform.broker.queue.EventListener.TYPE_ERROR;
import static org.datavaultplatform.broker.queue.EventListener.TYPE_RETRIEVE_COMPLETE;
import static org.datavaultplatform.broker.queue.EventListener.TYPE_RETRIEVE_START;
import static org.datavaultplatform.broker.queue.EventListener.TYPE_START;
import static org.datavaultplatform.common.email.EmailTemplate.GROUP_ADMIN_RETRIEVE_COMPLETE;
import static org.datavaultplatform.common.email.EmailTemplate.GROUP_ADMIN_RETRIEVE_START;
import static org.datavaultplatform.common.email.EmailTemplate.USER_RETRIEVE_COMPLETE;
import static org.datavaultplatform.common.email.EmailTemplate.USER_RETRIEVE_START;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Stream;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.datavaultplatform.broker.services.ArchiveStoreService;
import org.datavaultplatform.broker.services.ArchivesService;
import org.datavaultplatform.broker.services.AuditsService;
import org.datavaultplatform.broker.services.DepositsService;
import org.datavaultplatform.broker.services.EmailService;
import org.datavaultplatform.broker.services.EventService;
import org.datavaultplatform.broker.services.JobsService;
import org.datavaultplatform.broker.services.RetrievesService;
import org.datavaultplatform.broker.services.UsersService;
import org.datavaultplatform.broker.services.VaultsService;
import org.datavaultplatform.common.email.EmailTemplate;
import org.datavaultplatform.common.event.Error;
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
import org.datavaultplatform.common.event.deposit.ChunksDigestEvent;
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
import org.datavaultplatform.common.model.ArchiveStore;
import org.datavaultplatform.common.model.Audit;
import org.datavaultplatform.common.model.AuditChunkStatus;
import org.datavaultplatform.common.model.Deposit;
import org.datavaultplatform.common.model.Deposit.Status;
import org.datavaultplatform.common.model.DepositChunk;
import org.datavaultplatform.common.model.Group;
import org.datavaultplatform.common.model.Job;
import org.datavaultplatform.common.model.Retrieve;
import org.datavaultplatform.common.model.User;
import org.datavaultplatform.common.model.Vault;
import org.hibernate.StaleObjectStateException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer.MethodName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;

@Slf4j
@ExtendWith(MockitoExtension.class)
@TestMethodOrder(MethodName.class)
public class EventListenerTest {

  @Mock
  JobsService jobsService;
  @Mock
  EventService eventService;
  @Mock
  VaultsService vaultsService;
  @Mock
  DepositsService depositsService;
  @Mock
  ArchiveStoreService archiveStoreService;
  @Mock
  ArchivesService archivesService;
  @Mock
  RetrievesService retrievesService;
  @Mock
  UsersService usersService;
  @Mock
  EmailService emailService;
  @Mock
  AuditsService auditsService;

  final String homeUrl = "MOCK_HOME_URL";

  final String helpUrl = "MOCK_HELP_URL";

  final String helpMail = "MOCK_HELP_MAIL";

  final String auditAdminEmail = "MOCK_ADMIN_EMAIL";
  final Clock clock = Clock.fixed(Instant.now(), ZoneId.of("Europe/London"));
  final Date timestamp = new Date(clock.millis());
  private EventListener sut;


  @BeforeEach
  void setup() {
    sut = new EventListener(
        clock,
        jobsService,
        vaultsService,
        depositsService,
        archiveStoreService,
        archivesService,
        retrievesService,
        eventService,
        usersService,
        emailService,
        auditsService,
        homeUrl,
        helpUrl,
        helpMail,
        auditAdminEmail);
  }

  @Nested
  class ProcessWithRefreshTests {

    @Test
    void testNullInitialValue() {
      Supplier<Integer> supplier = () -> 1;
      Consumer<Integer> processor = val -> {
      };
      IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
          () -> sut.processWithRefresh(null, supplier, processor));
      assertEquals("The initialValue cannot be null", ex.getMessage());
    }

    @Test
    void testAlwaysFails() {
      Supplier<Integer> supplier = () -> 1;
      Consumer<Integer> processor = val -> {
        throw new StaleObjectStateException("fail", "fail");
      };

      boolean processed = sut.processWithRefresh(1, supplier, processor);
      assertFalse(processed);
    }

    @ParameterizedTest
    @CsvSource(value = {"1", "2", "3", "4", "5", "6"})
    void testWorksAfterNAttempts(int attemptToWork) {
      Supplier<Integer> supplier = () -> 1;
      Consumer<Integer> consumer = new Consumer<>() {
        int attempt = 0;

        @Override
        public void accept(Integer integer) {
          attempt++;
          if (attempt < attemptToWork) {
            throw new StaleObjectStateException("fail", "fail");
          }
        }
      };
      boolean success = sut.processWithRefresh(1, supplier, consumer);
      assertEquals(success, attemptToWork <= EventListener.MAX_ATTEMPTS);
    }
  }

  @Nested
  class OnMessageTests {



    @Test
    @SneakyThrows
    void testUnknownEvent() {
      TestEvent event = new TestEvent();
      event.setJobId("job123");
      event.setDepositId("deposit123");
      event.setUserId("user123");

      Deposit mDeposit = mock(Deposit.class);
      Job mJob = mock(Job.class);
      User mUser = mock(User.class);

      when(depositsService.getDeposit("deposit123")).thenReturn(mDeposit);
      when(jobsService.getJob("job123")).thenReturn(mJob);
      when(usersService.getUser("user123")).thenReturn(mUser);

      event.setEventClass(TestEvent.class.getName());
      String messageBody = new ObjectMapper().writeValueAsString(event);

      Exception ex = assertThrows(Exception.class, () -> sut.onMessageInternal(messageBody));
      assertTrue(ex.getMessage().startsWith(
          "Failed to process unknown Event class[class org.datavaultplatform.broker.queue.EventListenerTest$TestEvent]message[{"));
    }

    @Test
    void testBadMessage() {
      JsonParseException ex = assertThrows(JsonParseException.class, () ->
          sut.onMessageInternal("bad-message"));
      assertTrue(ex.getMessage().startsWith("Unrecognized token 'bad':"));
    }

    @Test
    @SneakyThrows
    void testMessageInternalSuccess() {
      ArgumentCaptor<String> argMessage = ArgumentCaptor.forClass(String.class);
      EventListener spy = spy(sut);
      Complete event = new Complete();
      Mockito.doReturn(event).when(spy).onMessageInternal(argMessage.capture());

      MessageProperties props = new MessageProperties();
      spy.onMessage(new Message("hello".getBytes(StandardCharsets.UTF_8), props));

      assertEquals("hello", argMessage.getValue());

      Mockito.verify(spy, times(1)).onMessageInternal(argMessage.getValue());
    }

    @Test
    @SneakyThrows
    void testMessageInternalFailure() {
      ArgumentCaptor<String> argMessage = ArgumentCaptor.forClass(String.class);
      EventListener spy = spy(sut);
      RuntimeException ex = new RuntimeException();
      Mockito.doThrow(ex).when(spy).onMessageInternal(argMessage.capture());

      MessageProperties props = new MessageProperties();
      spy.onMessage(new Message("hello".getBytes(StandardCharsets.UTF_8), props));

      assertEquals("hello", argMessage.getValue());

      Mockito.verify(spy, times(1)).onMessageInternal(argMessage.getValue());
    }

    @ParameterizedTest
    @SneakyThrows
    @ValueSource(booleans = {true,false})
    void testOnMessageInternal(boolean isPersistent) {

      EventListener spy = spy(sut);

      Event event = new Event();
      event.setPersistent(isPersistent);
      event.setDepositId("deposit-id");
      event.setJobId("job-id");
      event.setUserId("user-id");

      Deposit deposit = new Deposit();
      User user = new User();
      Job job = new Job();

      doReturn(event).when(spy).getConcreteEvent("message-body");
      doReturn(deposit).when(spy).getDeposit("deposit-id");
      doReturn(job).when(spy).getJob("job-id");
      doReturn(user).when(spy).getUser("user-id");
      doNothing().when(spy).updateJobToNextState(event, job);
      doNothing().when(spy).processEvent("message-body", event, deposit, job);
      lenient().doNothing().when(eventService).addEvent(event);

      assertNull(event.getUser());

      Event result = spy.onMessageInternal("message-body");

      assertEquals(user, event.getUser());

      assertEquals(event, result);

      verify(spy).getConcreteEvent("message-body");
      verify(spy).getDeposit("deposit-id");
      verify(spy).getJob("job-id");
      verify(spy).getUser("user-id");
      verify(spy).updateJobToNextState(event, job);
      verify(spy).processEvent("message-body", event, deposit, job);
      int expected = isPersistent ? 1 : 0;
      verify(eventService, times(expected)).addEvent(event);
    }
  }

  @Nested
  class GetTests {

    @Nested
    class Found {

      @Test
      void testGetDepositFound() {
        Deposit deposit = new Deposit();
        when(depositsService.getDeposit("depositId")).thenReturn(deposit);
        assertEquals(deposit, sut.getDeposit("depositId"));
        verify(depositsService).getDeposit("depositId");
      }

      @Test
      void testGetUserFound() {
        User user = new User();
        when(usersService.getUser("userId")).thenReturn(user);
        assertEquals(user, sut.getUser("userId"));
        verify(usersService).getUser("userId");
      }

      @Test
      void testGetVaultFound() {
        Vault vault = new Vault();
        when(vaultsService.getVault("vaultId")).thenReturn(vault);
        assertEquals(vault, sut.getVault("vaultId"));
        verify(vaultsService).getVault("vaultId");
      }

      @Test
      void testGetRetrieveFound() {
        Retrieve retrieve = new Retrieve();
        when(retrievesService.getRetrieve("retrieveId")).thenReturn(retrieve);
        assertEquals(retrieve, sut.getRetrieve("retrieveId"));
        verify(retrievesService).getRetrieve("retrieveId");
      }

      @Test
      void testGetAuditFound() {
        Audit audit = new Audit();
        when(auditsService.getAudit("auditId")).thenReturn(audit);
        assertEquals(audit, sut.getAudit("auditId"));
        verify(auditsService).getAudit("auditId");
      }

      @Test
      void testGetJobFound() {
        Job job = new Job();
        when(jobsService.getJob("jobId")).thenReturn(job);
        assertEquals(job, sut.getJob("jobId"));
        verify(jobsService).getJob("jobId");
      }

      @Test
      void testDepositChunkFound() {
        DepositChunk chunk = new DepositChunk();
        when(depositsService.getDepositChunkById("chunkId")).thenReturn(chunk);
        assertEquals(chunk, sut.getDepositChunk("chunkId"));
        verify(depositsService).getDepositChunkById("chunkId");
      }
    }

    @Nested
    class NotFound {

      @Test
      void testGetDepositNotFound() {
        when(depositsService.getDeposit("depositId")).thenReturn(null);
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
            () -> sut.getDeposit("depositId"));
        assertEquals("Deposit not found for id depositId", ex.getMessage());
        verify(depositsService).getDeposit("depositId");
      }

      @Test
      void testGetUserNotFound() {
        when(usersService.getUser("userId")).thenReturn(null);
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
            () -> sut.getUser("userId"));
        assertEquals("User not found for id userId", ex.getMessage());
        verify(usersService).getUser("userId");
      }

      @Test
      void testGetVaultNotFound() {
        when(vaultsService.getVault("vaultId")).thenReturn(null);
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
            () -> sut.getVault("vaultId"));
        assertEquals("Vault not found for id vaultId", ex.getMessage());
        verify(vaultsService).getVault("vaultId");
      }

      @Test
      void testGetRetrieveNotFound() {
        when(retrievesService.getRetrieve("retrieveId")).thenReturn(null);
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
            () -> sut.getRetrieve("retrieveId"));
        assertEquals("Retrieve not found for id retrieveId", ex.getMessage());
        verify(retrievesService).getRetrieve("retrieveId");
      }

      @Test
      void testGetAuditNotFound() {
        when(auditsService.getAudit("auditId")).thenReturn(null);
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
            () -> sut.getAudit("auditId"));
        assertEquals("Audit not found for id auditId", ex.getMessage());
        verify(auditsService).getAudit("auditId");
      }

      @Test
      void testGetJobNotFound() {
        when(jobsService.getJob("jobId")).thenReturn(null);
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
            () -> sut.getJob("jobId"));
        assertEquals("Job not found for id jobId", ex.getMessage());
        verify(jobsService).getJob("jobId");
      }

      @Test
      void testDepositChunkNotFound() {
        when(depositsService.getDepositChunkById("chunkId")).thenReturn(null);
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
            () -> sut.getDepositChunk("chunkId"));
        assertEquals("Chunk not found for id chunkId", ex.getMessage());
        verify(depositsService).getDepositChunkById("chunkId");
      }
    }
  }


  @Nested
  class EventSpecificTests {

    Job job;
    Deposit deposit;

    @BeforeEach
    void init() {
      job = mock(Job.class);
      deposit = mock(Deposit.class);
    }

    @Test
    void test01InitStates() {

      InitStates event = new InitStates();
      @SuppressWarnings("unchecked")
      ArrayList<String> states = mock(ArrayList.class);
      event.setStates(states);

      @SuppressWarnings("unchecked") 
      ArgumentCaptor<ArrayList<String>> argStates = ArgumentCaptor.forClass(ArrayList.class);
      ArgumentCaptor<Job> argJob = ArgumentCaptor.forClass(Job.class);

      doNothing().when(job).setStates(argStates.capture());
      doNothing().when(jobsService).updateJob(argJob.capture());

      sut.process01InitStates(event, job);

      verify(job).setStates(argStates.getValue());
      verify(jobsService).updateJob(argJob.getValue());

      assertEquals(job, argJob.getValue());
      assertEquals(states, argStates.getValue());
    }

    @Test
    void test02UpdateProgress() {

      ArgumentCaptor<Job> argJob = ArgumentCaptor.forClass(Job.class);
      ArgumentCaptor<Long> argProgress = ArgumentCaptor.forClass(Long.class);
      ArgumentCaptor<Long> argProgressMax = ArgumentCaptor.forClass(Long.class);
      ArgumentCaptor<String> argProgressMessage = ArgumentCaptor.forClass(String.class);

      UpdateProgress event = new UpdateProgress();
      event.setProgress(1234L);
      event.setProgressMax(2345L);
      event.setProgressMessage("the-progress-message");

      doNothing().when(job).setProgress(argProgress.capture());
      doNothing().when(job).setProgressMax(argProgressMax.capture());
      doNothing().when(job).setProgressMessage(argProgressMessage.capture());
      doNothing().when(jobsService).updateJob(argJob.capture());

      sut.process02UpdateProcess(event, job);

      assertEquals(1234L, argProgress.getValue());
      assertEquals(2345L, argProgressMax.getValue());
      assertEquals("the-progress-message", argProgressMessage.getValue());
      assertEquals(job, argJob.getValue());

      verify(job).setProgress(argProgress.getValue());
      verify(job).setProgressMax(argProgressMax.getValue());
      verify(job).setProgressMessage(argProgressMessage.getValue());
      verify(jobsService).updateJob(argJob.getValue());
    }

    @Nested
    class Test03StartEvent {

      final Start event = new Start();
      final ArgumentCaptor<Deposit.Status> argStatus = ArgumentCaptor.forClass(Deposit.Status.class);
      final ArgumentCaptor<String> argUserTemplate = ArgumentCaptor.forClass(String.class);
      final ArgumentCaptor<String> argAdminTemplate = ArgumentCaptor.forClass(String.class);

      final ArgumentCaptor<Deposit> argDeposit = ArgumentCaptor.forClass(Deposit.class);
      final ArgumentCaptor<Event> argEvent = ArgumentCaptor.forClass(Event.class);
      final ArgumentCaptor<String> argType = ArgumentCaptor.forClass(String.class);


      @ParameterizedTest
      @CsvSource({"NOT_STARTED"})
      @NullSource
      void test03StartStatusNotComplete(Deposit.Status nonCompleteStatus) {

        EventListener spy = spy(sut);

        doReturn(nonCompleteStatus).when(deposit).getStatus();
        doNothing().when(deposit).setStatus(argStatus.capture());
        doNothing().when(spy)
            .sendEmails(argDeposit.capture(), argEvent.capture(), argType.capture(),
                argUserTemplate.capture(), argAdminTemplate.capture());

        spy.process03Start(event, deposit);

        assertEquals(Status.IN_PROGRESS, argStatus.getValue());

        assertEquals(deposit, argDeposit.getValue());
        assertEquals(event, argEvent.getValue());
        assertEquals(TYPE_START, argType.getValue());
        assertEquals(EmailTemplate.USER_DEPOSIT_START, argUserTemplate.getValue());
        assertEquals(EmailTemplate.GROUP_ADMIN_DEPOSIT_START, argAdminTemplate.getValue());

        verify(deposit, atLeast(1)).getStatus();
        verify(deposit).setStatus(argStatus.getValue());

        verify(spy).sendEmails(argDeposit.getValue(), argEvent.getValue(), argType.getValue(),
            argUserTemplate.getValue(), argAdminTemplate.getValue());
      }

      @Test
      void test03StartStatusComplete() {

        EventListener spy = spy(sut);

        doReturn(Status.COMPLETE).when(deposit).getStatus();
        doNothing().when(spy)
            .sendEmails(argDeposit.capture(), argEvent.capture(), argType.capture(),
                argUserTemplate.capture(), argAdminTemplate.capture());

        spy.process03Start(event, deposit);

        assertEquals(deposit, argDeposit.getValue());
        assertEquals(event, argEvent.getValue());
        assertEquals(TYPE_START, argType.getValue());
        assertEquals(EmailTemplate.USER_DEPOSIT_START, argUserTemplate.getValue());
        assertEquals(EmailTemplate.GROUP_ADMIN_DEPOSIT_START, argAdminTemplate.getValue());

        verify(deposit, times(2)).getStatus();

        verify(spy).sendEmails(argDeposit.getValue(), argEvent.getValue(), argType.getValue(),
            argUserTemplate.getValue(), argAdminTemplate.getValue());
      }
    }

    @Test
    void test04ComputedSize() {

      ComputedSize event = new ComputedSize();
      event.setBytes(1234L);
      Deposit deposit = mock(Deposit.class);

      ArgumentCaptor<Long> argSize = ArgumentCaptor.forClass(Long.class);
      ArgumentCaptor<Deposit> argDeposit = ArgumentCaptor.forClass(Deposit.class);

      doNothing().when(deposit).setSize(argSize.capture());
      doNothing().when(depositsService).updateDeposit(argDeposit.capture());

      sut.process04ComputedSize(event, deposit);

      verify(deposit).setSize(1234L);
      verify(depositsService).updateDeposit(deposit);
    }

    @Test
    void test05ComputedChunks() {
      ArgumentCaptor<ComputedChunks> argEvent = ArgumentCaptor.forClass(ComputedChunks.class);
      ArgumentCaptor<Deposit> argDeposit = ArgumentCaptor.forClass(Deposit.class);

      EventListener spy = spy(sut);
      Deposit deposit = mock(Deposit.class);

      ComputedChunks event = new ComputedChunks();

      doNothing().when(spy).updateDepositWithChunks(argDeposit.capture(), argEvent.capture());
      doNothing().when(depositsService).updateDeposit(argDeposit.capture());

      spy.process05ComputedChunks(event, deposit);

      assertEquals(2, argDeposit.getAllValues().size());
      assertEquals(deposit, argDeposit.getAllValues().get(0));
      assertEquals(deposit, argDeposit.getAllValues().get(1));
      assertEquals(event, argEvent.getValue());

      verify(spy).updateDepositWithChunks(deposit, event);
      verify(depositsService).updateDeposit(deposit);
    }

    @Test
    void test06ComputedEncryption() {
      ArgumentCaptor<ComputedChunks> argEvent = ArgumentCaptor.forClass(ComputedChunks.class);
      ArgumentCaptor<Deposit> argDeposit = ArgumentCaptor.forClass(Deposit.class);

      EventListener spy = spy(sut);

      Deposit deposit = new Deposit();

      deposit.setDepositChunks(Stream.of(1, 2, 3).map(chunkNum -> {
        DepositChunk chunk = new DepositChunk();
        chunk.setChunkNum(chunkNum);
        return chunk;
      }).toList());

      ComputedEncryption event = new ComputedEncryption();

      byte[] tarIV = "tar-iv".getBytes(StandardCharsets.UTF_8);
      HashMap<Integer, byte[]> chunksIVs = new HashMap<>();
      HashMap<Integer, String> encChunksDigest = new HashMap<>();

      event.setTarIV(tarIV);
      event.setEncTarDigest("enc-tar-digest");

      event.setChunkIVs(chunksIVs);
      event.setEncChunkDigests(encChunksDigest);

      for (int i = 0; i < 3; i++) {
        int chunkNumber = i + 1;
        byte[] bytes = String.valueOf(chunkNumber).getBytes(StandardCharsets.UTF_8);
        String encDigest = "enc-digest-" + chunkNumber;

        chunksIVs.put(chunkNumber, bytes);
        encChunksDigest.put(chunkNumber, encDigest);
      }

      doNothing().when(spy).updateDepositWithChunks(any(), any());
      doNothing().when(depositsService).updateDeposit(argDeposit.capture());
      spy.process06ComputedEncryption(event, deposit);

      assertEquals(tarIV, deposit.getEncIV());
      assertEquals("enc-tar-digest", deposit.getEncArchiveDigest());

      assertEquals(3, deposit.getDepositChunks().size());

      DepositChunk chunk1 = deposit.getDepositChunks().get(0);
      assertEquals(1, chunk1.getChunkNum());
      assertEquals("1", new String(chunk1.getEncIV(), StandardCharsets.UTF_8));
      assertEquals("enc-digest-1", chunk1.getEcnArchiveDigest());

      DepositChunk chunk2 = deposit.getDepositChunks().get(1);
      assertEquals(2, chunk2.getChunkNum());
      assertEquals("2", new String(chunk2.getEncIV(), StandardCharsets.UTF_8));
      assertEquals("enc-digest-2", chunk2.getEcnArchiveDigest());

      DepositChunk chunk3 = deposit.getDepositChunks().get(2);
      assertEquals(3, chunk3.getChunkNum());
      assertEquals("3", new String(chunk3.getEncIV(), StandardCharsets.UTF_8));
      assertEquals("enc-digest-3", chunk3.getEcnArchiveDigest());

      assertThat(argDeposit.getValue()).isEqualTo(deposit);

      verify(spy).updateDepositWithChunks(deposit, event);
      verify(depositsService).updateDeposit(deposit);

    }

    @Test
    void test07ComputedDigest() {
      ArgumentCaptor<String> argDigest = ArgumentCaptor.forClass(String.class);
      ArgumentCaptor<String> argArchiveAlgorithm = ArgumentCaptor.forClass(String.class);

      ComputedDigest event = new ComputedDigest();
      event.setDigest("digest-123");
      event.setDigestAlgorithm("digest-algorithm-456");

      Deposit deposit = mock(Deposit.class);
      doNothing().when(deposit).setArchiveDigest(argDigest.capture());
      doNothing().when(deposit).setArchiveDigestAlgorithm(argArchiveAlgorithm.capture());

      sut.process07ComputedDigest(event, deposit);

      verify(deposit).setArchiveDigest(argDigest.getValue());
      verify(deposit).setArchiveDigestAlgorithm(argArchiveAlgorithm.getValue());
    }

    @Test
    void test08UploadComplete() {
      UploadComplete event = new UploadComplete();
      HashMap<String,String> items = new HashMap<>();
      items.put("store-id-1", "archive-id-1");
      items.put("store-id-2", "archive-id-2");
      items.put("store-id-3", "archive-id-3");
      event.setArchiveIds(items);

      ArchiveStore store1 = new ArchiveStore(){
        public String getID(){
          return "store-id-1";
        }
      };
      ArchiveStore store2 = new ArchiveStore(){
        public String getID(){
          return "store-id-2";
        }
      };
      ArchiveStore store3 = new ArchiveStore(){
        public String getID(){
          return "store-id-3";
        }
      };

      doReturn(store1).when(archiveStoreService).getArchiveStore("store-id-1");
      doReturn(store2).when(archiveStoreService).getArchiveStore("store-id-2");
      doReturn(store3).when(archiveStoreService).getArchiveStore("store-id-3");

      doNothing().when(archivesService).saveOrUpdateArchive(any(), any(), any());

      Deposit deposit = new Deposit();

      sut.process08UploadComplete(event, deposit);

      verify(archiveStoreService).getArchiveStore("store-id-1");
      verify(archiveStoreService).getArchiveStore("store-id-2");
      verify(archiveStoreService).getArchiveStore("store-id-3");

      verify(archivesService).saveOrUpdateArchive(deposit, store1, "archive-id-1");
      verify(archivesService).saveOrUpdateArchive(deposit, store2, "archive-id-2");
      verify(archivesService).saveOrUpdateArchive(deposit, store3, "archive-id-3");
    }

    @Test
    void test09Complete() {
      EventListener spy = spy(sut);
      Deposit deposit = new Deposit();
      deposit.setSize(1234L);
      deposit.setArchiveSize(2222L);

      Vault vault = new Vault();
      vault.setSize(1000L);

      deposit.setVault(vault);

      Complete event = new Complete();
      event.setArchiveSize(2345L);

      doNothing().when(depositsService).updateDeposit(deposit);
      doNothing().when(vaultsService).updateVault(vault);
      doNothing().when(spy).sendEmails(deposit, event, TYPE_COMPLETE,
          EmailTemplate.USER_DEPOSIT_COMPLETE,
          EmailTemplate.GROUP_ADMIN_DEPOSIT_COMPLETE);

      spy.process09Complete(event, deposit);

      verify(depositsService).updateDeposit(deposit);
      verify(vaultsService).updateVault(vault);
      verify(spy).sendEmails(deposit, event, TYPE_COMPLETE,
          EmailTemplate.USER_DEPOSIT_COMPLETE,
          EmailTemplate.GROUP_ADMIN_DEPOSIT_COMPLETE);

      assertEquals(Status.COMPLETE, deposit.getStatus());
      assertEquals(2345L, deposit.getArchiveSize());
      assertEquals(2234L, vault.getSize());
    }
    @Nested
    class ErrorTests {
      EventListener spy;
      ArgumentCaptor<Event> argEvent;
      ArgumentCaptor<Deposit> argDeposit;
      ArgumentCaptor<String> argType;
      ArgumentCaptor<String> argUserTemplate;
      ArgumentCaptor<String> argAdminTemplate;
      Error event;
      Job job;
      Deposit deposit;

      @BeforeEach
      void setup() {
        event = new Error();
        event.setMessage("test-error-message");

        job = new Job();

        spy = spy(sut);

        deposit = new Deposit();

        argEvent = ArgumentCaptor.forClass(Event.class);
        argDeposit = ArgumentCaptor.forClass(Deposit.class);
        argType = ArgumentCaptor.forClass(String.class);
        argUserTemplate = ArgumentCaptor.forClass(String.class);
        argAdminTemplate = ArgumentCaptor.forClass(String.class);

        lenient().doNothing().when(depositsService).updateDeposit(deposit);

        doNothing().when(jobsService).updateJob(job);

        lenient().doNothing().when(spy).sendEmails(argDeposit.capture(),
            argEvent.capture(),
            argType.capture(), argUserTemplate.capture(), argAdminTemplate.capture());

      }

      @Test
      void testErrorWithNullDeposit() {

        spy.process10Error(event, null, job);

        verify(jobsService).updateJob(job);

        assertTrue(job.isError());
        assertEquals("test-error-message", job.getErrorMessage());

        verify(spy, times(0)).sendEmails(any(), any(), any(), any(), any());

        verifyNoMoreInteractions(depositsService, jobsService);
      }

      @ParameterizedTest
      @EnumSource(Deposit.Status.class)
      void testErrorWithNonNullDeposit(Deposit.Status depositStatus) {

        deposit.setStatus(depositStatus);

        spy.process10Error(event, deposit, job);

        assertTrue(job.isError());
        assertEquals("test-error-message", job.getErrorMessage());

        final Deposit.Status expectedStatus;
        if (depositStatus == Status.COMPLETE) {
          expectedStatus = depositStatus;
        } else if (depositStatus == Status.DELETE_IN_PROGRESS) {
          expectedStatus = Status.DELETE_FAILED;
        } else {
          expectedStatus = Status.FAILED;
        }
        assertEquals(expectedStatus, deposit.getStatus());

        int expectedInvocations = depositStatus == Status.COMPLETE ? 0 : 1;
        verify(depositsService, times(expectedInvocations)).updateDeposit(deposit);

        verify(jobsService).updateJob(job);

        verify(spy, times(1)).sendEmails(
            argDeposit.getValue(), argEvent.getValue(),
            argType.getValue(), argUserTemplate.getValue(), argAdminTemplate.getValue());

        verifyNoMoreInteractions(depositsService, jobsService);
      }
    }
    @Test
    void test11RetrieveStart() {
      RetrieveStart event = new RetrieveStart();
      event.setRetrieveId("ret-id-123");
      Deposit mDeposit = mock(Deposit.class);
      Retrieve mRetrieve = mock(Retrieve.class);

      ArgumentCaptor<Retrieve> argRetrieve = ArgumentCaptor.forClass(Retrieve.class);
      ArgumentCaptor<Deposit> argDeposit = ArgumentCaptor.forClass(Deposit.class);
      ArgumentCaptor<Event> argEvent = ArgumentCaptor.forClass(Event.class);
      ArgumentCaptor<String> argType = ArgumentCaptor.forClass(String.class);
      ArgumentCaptor<String> argUserTemplate = ArgumentCaptor.forClass(String.class);
      ArgumentCaptor<String> argAdminTemplate = ArgumentCaptor.forClass(String.class);
      ArgumentCaptor<Retrieve.Status> argStatus = ArgumentCaptor.forClass(Retrieve.Status.class);

      EventListener spy = spy(sut);

      doReturn(mRetrieve).when(spy).getRetrieve("ret-id-123");
      doNothing().when(mRetrieve).setStatus(argStatus.capture());
      doNothing().when(retrievesService).updateRetrieve(argRetrieve.capture());

      doNothing().when(spy).sendEmails(argDeposit.capture(),
          argEvent.capture(), argType.capture(),
          argUserTemplate.capture(), argAdminTemplate.capture());

      spy.process11RetrieveStart(event, mDeposit);

      verify(spy).getRetrieve("ret-id-123");
      verify(mRetrieve).setStatus(Retrieve.Status.IN_PROGRESS);

      assertEquals(mRetrieve, argRetrieve.getValue());
      verify(retrievesService).updateRetrieve(argRetrieve.getValue());

      assertEquals(mDeposit, argDeposit.getValue());
      assertEquals(event, argEvent.getValue());
      assertEquals(TYPE_RETRIEVE_START, argType.getValue());
      assertEquals(USER_RETRIEVE_START, argUserTemplate.getValue());
      assertEquals(GROUP_ADMIN_RETRIEVE_START, argAdminTemplate.getValue());

      verify(spy).sendEmails(argDeposit.getValue(), argEvent.getValue(), argType.getValue(),
          argUserTemplate.getValue(), argAdminTemplate.getValue());
      verifyNoInteractions(mDeposit);
    }

    @Test
    void test12RetrieveStart() {
      RetrieveComplete event = new RetrieveComplete();
      event.setRetrieveId("ret-id-123");
      Deposit mDeposit = mock(Deposit.class);
      Retrieve mRetrieve = mock(Retrieve.class);

      ArgumentCaptor<Retrieve> argRetrieve = ArgumentCaptor.forClass(Retrieve.class);
      ArgumentCaptor<Deposit> argDeposit = ArgumentCaptor.forClass(Deposit.class);
      ArgumentCaptor<Event> argEvent = ArgumentCaptor.forClass(Event.class);
      ArgumentCaptor<String> argType = ArgumentCaptor.forClass(String.class);
      ArgumentCaptor<String> argUserTemplate = ArgumentCaptor.forClass(String.class);
      ArgumentCaptor<String> argAdminTemplate = ArgumentCaptor.forClass(String.class);
      ArgumentCaptor<Retrieve.Status> argStatus = ArgumentCaptor.forClass(Retrieve.Status.class);

      EventListener spy = spy(sut);

      doReturn(mRetrieve).when(spy).getRetrieve("ret-id-123");
      doNothing().when(mRetrieve).setStatus(argStatus.capture());
      doNothing().when(retrievesService).updateRetrieve(argRetrieve.capture());

      doNothing().when(spy).sendEmails(argDeposit.capture(),
          argEvent.capture(), argType.capture(),
          argUserTemplate.capture(), argAdminTemplate.capture());

      spy.process12RetrieveComplete(event, mDeposit);

      verify(spy).getRetrieve("ret-id-123");
      verify(mRetrieve).setStatus(Retrieve.Status.COMPLETE);

      assertEquals(mRetrieve, argRetrieve.getValue());
      verify(retrievesService).updateRetrieve(argRetrieve.getValue());

      assertEquals(mDeposit, argDeposit.getValue());
      assertEquals(event, argEvent.getValue());
      assertEquals(TYPE_RETRIEVE_COMPLETE, argType.getValue());
      assertEquals(USER_RETRIEVE_COMPLETE, argUserTemplate.getValue());
      assertEquals(GROUP_ADMIN_RETRIEVE_COMPLETE, argAdminTemplate.getValue());

      verify(spy).sendEmails(argDeposit.getValue(), argEvent.getValue(), argType.getValue(),
          argUserTemplate.getValue(), argAdminTemplate.getValue());
      verifyNoInteractions(mDeposit);
    }

    @Test
    void test13DeleteStart() {
      ArgumentCaptor<Deposit.Status> argStatus = ArgumentCaptor.forClass(Deposit.Status.class);
      ArgumentCaptor<Deposit> argDeposit = ArgumentCaptor.forClass(Deposit.class);

      Deposit mDeposit = mock(Deposit.class);

      Deposit.Status initialStatus = Status.NOT_STARTED;

      doReturn(initialStatus).when(mDeposit).getStatus();
      doNothing().when(mDeposit).setStatus(argStatus.capture());
      doNothing().when(depositsService).updateDeposit(argDeposit.capture());

      assertNull(sut.preDeletionStatus);
      sut.process13DeleteStart(null, mDeposit);

      assertEquals(initialStatus, sut.preDeletionStatus);
      assertEquals(mDeposit, argDeposit.getValue());
      assertEquals(Status.DELETE_IN_PROGRESS, argStatus.getValue());

      verify(mDeposit).getStatus();
      verify(mDeposit).setStatus(argStatus.getValue());
      verify(depositsService).updateDeposit(argDeposit.getValue());
    }

    @ParameterizedTest
    @EnumSource(Deposit.Status.class)
    void test14DeleteComplete(Deposit.Status preDeletionStatus) {

      long vaultSize = 1234L;
      long depositSize = 2345L;

      sut.preDeletionStatus = preDeletionStatus;

      Deposit deposit = new Deposit();
      deposit.setSize(depositSize);

      Vault vault = new Vault();
      vault.setSize(vaultSize);

      deposit.setVault(vault);

      DeleteComplete event = new DeleteComplete();

      doNothing().when(depositsService).updateDeposit(deposit);

      lenient().doNothing().when(vaultsService).updateVault(vault);

      sut.process14DeleteComplete(event, deposit);

      if (preDeletionStatus == Status.FAILED) {

        verify(vaultsService, times(0)).updateVault(vault);
        assertEquals(vaultSize, vault.getSize());

      } else {

        verify(vaultsService, times(1)).updateVault(vault);
        assertEquals(vaultSize - depositSize, vault.getSize());
      }
    }

    @Test
    void test15AuditStart() {
      AuditStart event = new AuditStart();
      event.setAuditId("audit-id-1234");

      EventListener spy = spy(sut);

      ArgumentCaptor<Audit> argAudit = ArgumentCaptor.forClass(Audit.class);
      ArgumentCaptor<Audit.Status> argAuditStatus = ArgumentCaptor.forClass(Audit.Status.class);

      Audit mAudit = mock(Audit.class);

      doReturn(mAudit).when(spy).getAudit("audit-id-1234");
      doNothing().when(mAudit).setStatus(argAuditStatus.capture());
      doNothing().when(auditsService).updateAudit(argAudit.capture());

      spy.process15AuditStart(event);

      assertEquals(mAudit, argAudit.getValue());
      assertEquals(Audit.Status.IN_PROGRESS, argAuditStatus.getValue());

      verify(spy).getAudit("audit-id-1234");
      verify(mAudit).setStatus(argAuditStatus.getValue());
      verify(auditsService).updateAudit(argAudit.getValue());
    }

    @Test
    void test16ChunkAuditStarted() {
      EventListener spy = spy(sut);

      ArgumentCaptor<Audit> argAudit = ArgumentCaptor.forClass(Audit.class);
      ArgumentCaptor<DepositChunk> argDepositChunk = ArgumentCaptor.forClass(DepositChunk.class);
      ArgumentCaptor<String> argArchiveId = ArgumentCaptor.forClass(String.class);
      ArgumentCaptor<String> argLocation = ArgumentCaptor.forClass(String.class);
      ArgumentCaptor<String> argChunkId = ArgumentCaptor.forClass(String.class);

      ChunkAuditStarted event = new ChunkAuditStarted();
      event.setAuditId("audit-id-ABC");
      event.setArchiveId("arch-id-DEF");
      event.setLocation("location-123");

      Audit mAudit = mock(Audit.class);
      DepositChunk mDepositChunk = mock(DepositChunk.class);

      doReturn(mAudit).when(spy).getAudit("audit-id-ABC");

      doReturn(mDepositChunk).when(spy).getDepositChunk(argChunkId.capture());

      AuditChunkStatus auditChunkStatus = new AuditChunkStatus();
      doReturn(auditChunkStatus).when(auditsService).addAuditStatus(
          argAudit.capture(),
          argDepositChunk.capture(),
          argArchiveId.capture(),
          argLocation.capture());

      spy.process16ChunkAuditStarted(event);

      verify(spy).getAudit("audit-id-ABC");
      verify(spy).getDepositChunk(argChunkId.getValue());

      assertEquals(mAudit, argAudit.getValue());
      assertEquals(mDepositChunk, argDepositChunk.getValue());
      assertEquals("arch-id-DEF", argArchiveId.getValue());
      assertEquals("location-123", argLocation.getValue());

      verify(auditsService).addAuditStatus(
          argAudit.getValue(),
          argDepositChunk.getValue(),
          argArchiveId.getValue(),
          argLocation.getValue());

    }

    @Test
    void test17ChunkAuditComplete() {
      ChunkAuditComplete event = new ChunkAuditComplete();
      EventListener spy = spy(sut);
      AuditChunkStatus mChunkStatus = mock(AuditChunkStatus.class);

      doReturn(mChunkStatus).when(spy).processAuditChunkStatus(event);
      doNothing().when(mChunkStatus).complete();
      doNothing().when(auditsService).updateAuditChunkStatus(mChunkStatus);

      spy.process17ChunkAuditComplete(event);

      verify(spy).processAuditChunkStatus(event);
      verify(mChunkStatus).complete();
      verify(auditsService).updateAuditChunkStatus(mChunkStatus);
    }

    @Nested
    class AuditCompleteTests {

      @ParameterizedTest
      @NullSource
      @EnumSource(value = AuditChunkStatus.Status.class, names = {"QUEUING", "IN_PROGRESS",
          "COMPLETE", "FIXED"})
      void testAuditCompleteSuccess(AuditChunkStatus.Status status) {
        AuditChunkStatus auditChunkStatus = new AuditChunkStatus();
        auditChunkStatus.setStatus(status);
        checkAuditComplete(false, List.of(auditChunkStatus));
      }

      @Test
      void testAuditCompleteError() {
        AuditChunkStatus auditChunkStatus = new AuditChunkStatus();
        auditChunkStatus.setStatus(AuditChunkStatus.Status.ERROR);
        checkAuditComplete(true, List.of(auditChunkStatus));
      }

      void checkAuditComplete(boolean errorExpected, List<AuditChunkStatus> items) {
        AuditComplete event = new AuditComplete();
        event.setAuditId("test-audit-id");

        Audit audit = new Audit();

        EventListener spy = spy(sut);

        doReturn(audit).when(spy).getAudit("test-audit-id");
        doReturn(items).when(auditsService).getAuditChunkStatusFromAudit(audit);
        doNothing().when(auditsService).updateAudit(audit);

        spy.process18AuditComplete(event);

        Audit.Status expectedStatus = errorExpected ? Audit.Status.FAILED : Audit.Status.COMPLETE;

        assertEquals(expectedStatus, audit.getStatus());

      }

    }

    @Test
    void test19AuditError() {
      ArgumentCaptor<String> argNote = ArgumentCaptor.forClass(String.class);
      ArgumentCaptor<AuditChunkStatus> argChunkStatus = ArgumentCaptor.forClass(
          AuditChunkStatus.class);

      AuditError event = new AuditError();
      event.setMessage("event-message-123");

      AuditChunkStatus mChunkStatus = mock(AuditChunkStatus.class);
      EventListener spy = spy(sut);

      doReturn(mChunkStatus).when(spy).processAuditChunkStatus(event);
      doNothing().when(mChunkStatus).failed(argNote.capture());
      doNothing().when(auditsService).updateAuditChunkStatus(argChunkStatus.capture());
      doNothing().when(spy).sendAuditEmails(event, EmailTemplate.AUDIT_CHUNK_ERROR);

      spy.process19AuditError(event);

      verify(spy).processAuditChunkStatus(event);

      assertEquals("event-message-123", argNote.getValue());
      verify(mChunkStatus).failed(argNote.getValue());

      assertEquals(mChunkStatus, argChunkStatus.getValue());
      verify(auditsService).updateAuditChunkStatus(argChunkStatus.getValue());

      verify(spy).sendAuditEmails(event, EmailTemplate.AUDIT_CHUNK_ERROR);
    }

    @Test
    void testCopyCompleteUploadWIthChunk() {
      CompleteCopyUpload event = new CompleteCopyUpload("depositId","jobId","test-type",123,"archiveStoreId","archiveId");
      
      Deposit mDeposit = mock(Deposit.class);

      ArchiveStore mArchiveStore = mock(ArchiveStore.class);
      
      when(archiveStoreService.getArchiveStore("archiveStoreId")).thenReturn(mArchiveStore);

      ArgumentCaptor<Event> argEvent = ArgumentCaptor.forClass(Event.class);
      doNothing().when(eventService).addEvent(argEvent.capture());
      sut.process23CompleteCopyUpload(event, mDeposit);
      
      verify(eventService).addEvent(argEvent.getValue());
      verify(archivesService).saveOrUpdateArchive(mDeposit, mArchiveStore, "archiveId");
      verifyNoMoreInteractions(eventService, archivesService, mDeposit, mArchiveStore);
    }
    
    @Test
    void testCopyCompleteUploadWithoutChunk() {
      CompleteCopyUpload event = new CompleteCopyUpload("depositId","jobId","test-type",null,"archiveStoreId","archiveId");

      Deposit mDeposit = mock(Deposit.class);

      ArchiveStore mArchiveStore = mock(ArchiveStore.class);
      
      when(archiveStoreService.getArchiveStore("archiveStoreId")).thenReturn(mArchiveStore);

      ArgumentCaptor<Event> argEvent = ArgumentCaptor.forClass(Event.class);
      doNothing().when(eventService).addEvent(argEvent.capture());
      sut.process23CompleteCopyUpload(event, mDeposit);
      
      verify(eventService).addEvent(argEvent.getValue());
      verify(archivesService).saveOrUpdateArchive(mDeposit, mArchiveStore, "archiveId");
      verifyNoMoreInteractions(eventService, archivesService, mDeposit, mArchiveStore);
    }

    @Nested
    class IgnoreEvents {

      @Test
      @SneakyThrows
      void test20TransferComplete() {
        sut.process20TransferComplete(new TransferComplete());
      }

      @Test
      @SneakyThrows
      void test21PackageComplete() {
        sut.process21PackageComplete(new PackageComplete());
      }

      @Test
      @SneakyThrows
      void test22StartCopyUpload() {
        sut.process22StartCopyUpload(new StartCopyUpload());
      }

      @Test
      @SneakyThrows
      void test24ValidationComplete() {
        sut.process24ValidationComplete(new ValidationComplete());
      }
    }

  }

  @Nested
  class ProcessEventTests {

    EventListener spy;
    final Deposit deposit = new Deposit();
    final Job job = new Job();

    @BeforeEach
    void setup() {
      spy = spy(sut);
    }

    @Test
    @SneakyThrows
    void test01() {
      InitStates event = new InitStates();
      doNothing().when(spy).process01InitStates(event, job);
      spy.processEvent("message", event, deposit, job);
      verify(spy).process01InitStates(event, job);
    }

    @Test
    @SneakyThrows
    void test02() {
      UpdateProgress event = new UpdateProgress();
      doNothing().when(spy).process02UpdateProcess(event, job);
      spy.processEvent("message", event, deposit, job);
      verify(spy).process02UpdateProcess(event, job);
    }

    @Test
    @SneakyThrows
    void test03() {
      Start event = new Start();
      doNothing().when(spy).process03Start(event, deposit);
      spy.processEvent("message", event, deposit, job);
      verify(spy).process03Start(event, deposit);
    }

    @Test
    @SneakyThrows
    void test04() {
      ComputedSize event = new ComputedSize();
      doNothing().when(spy).process04ComputedSize(event, deposit);
      spy.processEvent("message", event, deposit, job);
      verify(spy).process04ComputedSize(event, deposit);
    }

    @Test
    @SneakyThrows
    void test05() {
      ComputedChunks event = new ComputedChunks();
      doNothing().when(spy).process05ComputedChunks(event, deposit);
      spy.processEvent("message", event, deposit, job);
      verify(spy).process05ComputedChunks(event, deposit);
    }

    @Test
    @SneakyThrows
    void test06() {
      ComputedEncryption event = new ComputedEncryption();
      doNothing().when(spy).process06ComputedEncryption(event, deposit);
      spy.processEvent("message", event, deposit, job);
      verify(spy).process06ComputedEncryption(event, deposit);
    }

    @Test
    @SneakyThrows
    void test07() {
      ComputedDigest event = new ComputedDigest();
      doNothing().when(spy).process07ComputedDigest(event, deposit);
      spy.processEvent("message", event, deposit, job);
      verify(spy).process07ComputedDigest(event, deposit);
    }

    @Test
    @SneakyThrows
    void test08() {
      UploadComplete event = new UploadComplete();
      doNothing().when(spy).process08UploadComplete(event, deposit);
      spy.processEvent("message", event, deposit, job);
      verify(spy).process08UploadComplete(event, deposit);
    }

    @Test
    @SneakyThrows
    void test09() {
      Complete event = new Complete();
      doNothing().when(spy).process09Complete(event, deposit);
      spy.processEvent("message", event, deposit, job);
      verify(spy).process09Complete(event, deposit);
    }

    @Test
    @SneakyThrows
    void test10() {
      Error event = new Error();
      doNothing().when(spy).process10Error(event, deposit, job);
      spy.processEvent("message", event, deposit, job);
      verify(spy).process10Error(event, deposit, job);
    }

    @Test
    @SneakyThrows
    void test11() {
      RetrieveStart event = new RetrieveStart();
      doNothing().when(spy).process11RetrieveStart(event, deposit);
      spy.processEvent("message", event, deposit, job);
      verify(spy).process11RetrieveStart(event, deposit);
    }

    @Test
    @SneakyThrows
    void test12() {
      RetrieveComplete event = new RetrieveComplete();
      doNothing().when(spy).process12RetrieveComplete(event, deposit);
      spy.processEvent("message", event, deposit, job);
      verify(spy).process12RetrieveComplete(event, deposit);
    }

    @Test
    @SneakyThrows
    void test13() {
      DeleteStart event = new DeleteStart();
      doNothing().when(spy).process13DeleteStart(event, deposit);
      spy.processEvent("message", event, deposit, job);
      verify(spy).process13DeleteStart(event, deposit);
    }

    @Test
    @SneakyThrows
    void test14() {
      DeleteComplete event = new DeleteComplete();
      doNothing().when(spy).process14DeleteComplete(event, deposit);
      spy.processEvent("message", event, deposit, job);
      verify(spy).process14DeleteComplete(event, deposit);
    }

    @Test
    @SneakyThrows
    void test15() {
      AuditStart event = new AuditStart();
      doNothing().when(spy).process15AuditStart(event);
      spy.processEvent("message", event, deposit, job);
      verify(spy).process15AuditStart(event);
    }

    @Test
    @SneakyThrows
    void test16() {
      ChunkAuditStarted event = new ChunkAuditStarted();
      doNothing().when(spy).process16ChunkAuditStarted(event);
      spy.processEvent("message", event, deposit, job);
      verify(spy).process16ChunkAuditStarted(event);
    }

    @Test
    @SneakyThrows
    void test17() {
      ChunkAuditComplete event = new ChunkAuditComplete();
      doNothing().when(spy).process17ChunkAuditComplete(event);
      spy.processEvent("message", event, deposit, job);
      verify(spy).process17ChunkAuditComplete(event);
    }

    @Test
    @SneakyThrows
    void test18() {
      AuditComplete event = new AuditComplete();
      doNothing().when(spy).process18AuditComplete(event);
      spy.processEvent("message", event, deposit, job);
      verify(spy).process18AuditComplete(event);
    }

    @Test
    @SneakyThrows
    void test19() {
      AuditError event = new AuditError();
      doNothing().when(spy).process19AuditError(event);
      spy.processEvent("message", event, deposit, job);
      verify(spy).process19AuditError(event);
    }

    @Test
    @SneakyThrows
    void test20() {
      TransferComplete event = new TransferComplete();
      doNothing().when(spy).process20TransferComplete(event);
      spy.processEvent("message", event, deposit, job);
      verify(spy).process20TransferComplete(event);
    }

    @Test
    @SneakyThrows
    void test21() {
      PackageComplete event = new PackageComplete();
      doNothing().when(spy).process21PackageComplete(event);
      spy.processEvent("message", event, deposit, job);
      verify(spy).process21PackageComplete(event);
    }

    @Test
    @SneakyThrows
    void test22() {
      StartCopyUpload event = new StartCopyUpload();
      doNothing().when(spy).process22StartCopyUpload(event);
      spy.processEvent("message", event, deposit, job);
      verify(spy).process22StartCopyUpload(event);
    }

    @Test
    @SneakyThrows
    void test23() {
      CompleteCopyUpload event = new CompleteCopyUpload();
      doNothing().when(spy).process23CompleteCopyUpload(event, deposit);
      spy.processEvent("message", event, deposit, job);
      verify(spy).process23CompleteCopyUpload(event, deposit);
    }

    @Test
    @SneakyThrows
    void test24() {
      ValidationComplete event = new ValidationComplete();
      doNothing().when(spy).process24ValidationComplete(event);
      spy.processEvent("message", event, deposit, job);
      verify(spy).process24ValidationComplete(event);
    }
  }

  @Nested
  class HelperMethodTests {

    @ParameterizedTest
    @ValueSource(ints = {0,1,2})
    @NullSource
    void testUpdateJobToNextStateJobHasState(Integer nextState) {
      log.info("nextState [{}]", nextState);
      Job job = new Job();
      job.setState(1);

      Event event = new Event();
      event.nextState = nextState;

      sut.updateJobToNextState(event, job);

      if(nextState != null && nextState > 1){
        verify(jobsService, times(1)).updateJob(job);
        assertEquals(nextState, job.getState());
      } else {
        verify(jobsService, times(0)).updateJob(any());
        assertEquals(1, job.getState());
      }
    }

    @Test
    void testUpdateJobToNextState() {

      checkUpdateJobToNextStateJobHasNoState(null, null);
      checkUpdateJobToNextStateJobHasNoState(null, 1);

      checkUpdateJobToNextStateJobHasNoState(1, null);
      checkUpdateJobToNextStateJobHasNoState(1, 0);
      checkUpdateJobToNextStateJobHasNoState(1, 1);
      checkUpdateJobToNextStateJobHasNoState(1, 2);
    }

    void checkUpdateJobToNextStateJobHasNoState(Integer currentState, Integer nextState) {
      Mockito.reset(jobsService);
      log.info("nextState [{}]", nextState);
      Job job = new Job();
      job.setState(currentState);

      Event event = new Event();
      event.nextState = nextState;

      sut.updateJobToNextState(event, job);

      if(nextState != null &&  (currentState == null || currentState < nextState)) {
        verify(jobsService, times(1)).updateJob(job);
        assertEquals(nextState, job.getState());
      } else {
        verify(jobsService, times(0)).updateJob(any());
        assertEquals(currentState, job.getState());
      }
    }

    @ParameterizedTest
    @MethodSource(value = "org.datavaultplatform.broker.queue.EventListenerTest#eventForTestModel")
    @SneakyThrows
    void testGetModel(Event event) {
      Field fTimestamp = Event.class.getDeclaredField("timestamp");
      fTimestamp.setAccessible(true);
      fTimestamp.set(event, timestamp);

      Deposit deposit = new Deposit() {
        @Override
        public String getID() {
          return "deposit-id-AAA";
        }
      };
      deposit.setName("d-name");
      Vault vault = new Vault() {
        @Override
        public String getID() {
          return "vault-id-BBB";
        }
      };
      vault.setName("vault-name-123");
      User depositUser = new User();
      depositUser.setFirstname("d-first");
      depositUser.setLastname("d-last");
      depositUser.setID("deposit-user-id-CCC");

      User retrieveUser = new User();
      retrieveUser.setFirstname("r-first");
      retrieveUser.setLastname("r-last");
      retrieveUser.setID("retrieve-user-id-DDD");

      Group group = new Group();
      group.setID("group-id-EEE");
      group.setName("group-name-123");

      Map<String, Object> model = sut.getModel(deposit, depositUser, event, retrieveUser, vault,
          group);
      assertTrue(model.size() >= 15);
      assertThat(model.get(EMAIL_KEY_GROUP_NAME)).isEqualTo("group-name-123");
      assertThat(model.get(EMAIL_KEY_DEPOSIT_NAME)).isEqualTo("d-name");
      assertThat(model.get(EMAIL_KEY_DEPOSIT_ID)).isEqualTo("deposit-id-AAA");
      assertThat(model.get(EMAIL_KEY_VAULT_NAME)).isEqualTo("vault-name-123");
      assertThat(model.get(EMAIL_KEY_VAULT_ID)).isEqualTo("vault-id-BBB");

      assertThat(model.get(EMAIL_KEY_VAULT_REVIEW_DATE)).isEqualTo(vault.getReviewDate());
      //really???
      assertThat(model.get(EMAIL_KEY_USER_ID)).isEqualTo("deposit-user-id-CCC");
      assertThat(model.get(EMAIL_KEY_USER_FIRSTNAME)).isEqualTo("d-first");
      assertThat(model.get(EMAIL_KEY_USER_LASTNAME)).isEqualTo("d-last");
      assertThat(model.get(EMAIL_KEY_SIZE_BYTES)).isEqualTo(deposit.getArchiveSize());

      assertThat(model.get(EMAIL_KEY_TIMESTAMP)).isEqualTo(event.getTimestamp());
      assertThat(model.get(EMAIL_KEY_HAS_PERSONAL_DATA)).isEqualTo(deposit.getHasPersonalData());
      assertThat(model.get(EMAIL_KEY_HOME_PAGE)).isEqualTo("MOCK_HOME_URL");
      assertThat(model.get(EMAIL_KEY_HELP_PAGE)).isEqualTo("MOCK_HELP_URL");
      assertThat(model.get(EMAIL_KEY_HELP_MAIL)).isEqualTo("MOCK_HELP_MAIL");
      String expectedErrorMessage = event instanceof Error ? event.getMessage() : null;
      assertThat(model.get(EMAIL_KEY_ERROR_MESSAGE)).isEqualTo(expectedErrorMessage);
      String expectedRetFirstName = null;
      String expectedRetLastName = null;
      String expectedRetId = null;
      if (event instanceof Error ||
          event instanceof RetrieveStart ||
          event instanceof RetrieveComplete) {
        expectedRetFirstName = retrieveUser.getFirstname();
        expectedRetLastName = retrieveUser.getLastname();
        expectedRetId = retrieveUser.getID();
      }
      assertThat(model.get(EMAIL_KEY_RETRIEVER_FIRSTNAME)).isEqualTo(expectedRetFirstName);
      assertThat(model.get(EMAIL_KEY_RETRIEVER_LASTNAME)).isEqualTo(expectedRetLastName);
      assertThat(model.get(EMAIL_KEY_RETRIEVER_ID)).isEqualTo(expectedRetId);
    }

    @Nested
    class SubjectsForEmailTemplatesTests {

      @Test
      void testUserSubjectsForEmailTemplates() {

        // USER
        assertEquals(EventListener.SUBJECT_START, sut.getUserSubject(TYPE_START));
        assertEquals(EventListener.SUBJECT_COMPLETE, sut.getUserSubject(TYPE_COMPLETE));
        assertEquals(EventListener.SUBJECT_RETRIEVE_START,
            sut.getUserSubject(TYPE_RETRIEVE_START));
        assertEquals(EventListener.SUBJECT_RETRIEVE_COMPLETE,
            sut.getUserSubject(TYPE_RETRIEVE_COMPLETE));
        assertEquals(EventListener.SUBJECT_ERROR, sut.getUserSubject(EventListener.TYPE_ERROR));
        IllegalArgumentException ex1 = assertThrows(IllegalArgumentException.class,
            () -> sut.getUserSubject("UNKNOWN"));
        assertEquals("User Subject key not found: user-deposit-UNKNOWN", ex1.getMessage());

      }

      @Test
      void testAdminSubjectsForEmailTemplates() {

        // ADMIN
        assertEquals(EventListener.SUBJECT_START, sut.getAdminSubject(TYPE_START));
        assertEquals(EventListener.SUBJECT_COMPLETE, sut.getAdminSubject(TYPE_COMPLETE));
        assertEquals(EventListener.SUBJECT_RETRIEVE_START,
            sut.getAdminSubject(TYPE_RETRIEVE_START));
        assertEquals(EventListener.SUBJECT_RETRIEVE_COMPLETE,
            sut.getAdminSubject(TYPE_RETRIEVE_COMPLETE));
        assertEquals(EventListener.SUBJECT_ERROR, sut.getAdminSubject(EventListener.TYPE_ERROR));
        IllegalArgumentException ex2 = assertThrows(IllegalArgumentException.class,
            () -> sut.getAdminSubject("UNKNOWN"));
        assertEquals("Admin Subject key not found: admin-deposit-UNKNOWN", ex2.getMessage());


      }

      @Test
      void testAuditSubjectsForEmailTemplates() {

        // AUDIT
        assertEquals(EventListener.SUBJECT_AUDIT_ERROR,
            sut.getAuditErrorSubject(EventListener.TYPE_ERROR));

        Stream.of(
            TYPE_START, TYPE_COMPLETE,
            TYPE_RETRIEVE_START, TYPE_RETRIEVE_COMPLETE).forEach(type -> {

          IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
              () -> sut.getAuditErrorSubject(type));
          assertEquals("Audit Error Subject key not found: " + EventListener.AUDIT_CHUNK_PREFIX + type,
              ex.getMessage());
        });
      }
    }

    @SuppressWarnings("unchecked")
    @Test
    void testSendAuditEmails() {
      AuditError auditError = new AuditError();
      ArgumentCaptor<String> argTo = ArgumentCaptor.forClass(String.class);
      ArgumentCaptor<String> argSubject = ArgumentCaptor.forClass(String.class);
      ArgumentCaptor<String> argTemplate = ArgumentCaptor.forClass(String.class);
      ArgumentCaptor<Map<String,Object>> argModel = ArgumentCaptor.forClass(Map.class);

      EventListener spy = spy(sut);

      Map<String, Object> model = new HashMap<>();
      doReturn(model).when(spy).getErrorModel(auditError);

      doReturn("<SUBJECT>").when(spy).getAuditErrorSubject(TYPE_ERROR);

      doNothing().when(emailService).sendTemplateMail(
          argTo.capture(),
          argSubject.capture(),
          argTemplate.capture(),
          argModel.capture());

      //the method we want to test
      spy.sendAuditEmails(auditError, "<template>");

      assertEquals("MOCK_ADMIN_EMAIL", argTo.getValue());
      assertEquals("<SUBJECT>", argSubject.getValue());
      assertEquals("<template>", argTemplate.getValue());
      assertEquals(model, argModel.getValue());

      verify(spy, times(1)).getAuditErrorSubject(TYPE_ERROR);

      verify(emailService, times(1))
          .sendTemplateMail(argTo.getValue(), argSubject.getValue(),
              argTemplate.getValue(), argModel.getValue());
    }

    @Test
    @SneakyThrows
    void testSendEmails() {

      ArgumentCaptor<String> argTo = ArgumentCaptor.forClass(String.class);
      ArgumentCaptor<String> argSubject = ArgumentCaptor.forClass(String.class);
      ArgumentCaptor<String> argTemplate = ArgumentCaptor.forClass(String.class);
      ArgumentCaptor<Map<String, Object>> argModel = ArgumentCaptor.forClass(Map.class);

      EventListener spy = spy(sut);

      String userId = "some-user-id";
      String type = "some-type";
      String userTemplate = "some-user-template";
      String adminTemplate = "some-admin-template";
      String userSubject = "user-subject";
      String adminSubject = "admin-subject";
      String userEmail = "user1@test.com";

      Map<String, Object> model = mock(Map.class);

      Vault vault = mock(Vault.class);
      Group group = mock(Group.class);
      Deposit deposit = mock(Deposit.class);
      User depositUser = mock(User.class);
      User retrieveUser = mock(User.class);
      Event event = mock(Event.class);

      User admin1 = new User();
      admin1.setEmail("admin1@test.com");
      User admin2 = new User();
      admin2.setEmail("admin2@test.com");
      User admin3 = new User();
      admin3.setEmail("admin3@test.com");

      doReturn(depositUser).when(deposit).getUser();
      doReturn(group).when(vault).getGroup();
      doReturn(userEmail).when(depositUser).getEmail();
      doReturn(userId).when(event).getUserId();
      doReturn(vault).when(deposit).getVault();

      doNothing().when(spy).validateType("some-type");

      doReturn(retrieveUser).when(usersService).getUser(userId);

      doReturn(model).when(spy).getModel(deposit, depositUser, event, retrieveUser, vault, group);

      doReturn(userSubject).when(spy).getUserSubject(type);
      doReturn(adminSubject).when(spy).getAdminSubject(type);

      doReturn(List.of(admin1, admin2, admin3)).when(group).getOwners();

      doNothing().when(spy)
          .sendTemplateEmail(argTo.capture(), argSubject.capture(), argTemplate.capture(),
              argModel.capture());

      // CALL THE METHOD WE WANT TO TEST
      spy.sendEmails(deposit, event, type, userTemplate, adminTemplate);

      // NOW VERIFY
      verify(spy, times(4)).sendTemplateEmail(any(), any(), any(), any());

      assertEquals(List.of(
              "admin1@test.com", "admin2@test.com", "admin3@test.com", "user1@test.com"),
          argTo.getAllValues());

      assertEquals(List.of(
              adminSubject, adminSubject, adminSubject, userSubject),
          argSubject.getAllValues());

      assertEquals(List.of(
              adminTemplate, adminTemplate, adminTemplate, userTemplate),
          argTemplate.getAllValues());

      assertEquals(List.of(model, model, model, model),
          argModel.getAllValues());

      verify(deposit).getUser();
      verify(vault).getGroup();
      verify(depositUser).getEmail();
      verify(event).getUserId();
      verify(deposit).getVault();

      verify(spy).validateType("some-type");

      verify(usersService).getUser(userId);

      verify(spy).getModel(deposit, depositUser, event, retrieveUser, vault, group);

      verify(spy).getUserSubject(type);
      verify(spy).getAdminSubject(type);

      verify(group).getOwners();

      verifyNoMoreInteractions(vault, group, deposit, depositUser, retrieveUser, event);
    }

    @Test
    @SneakyThrows
    void testErrorModelForAuditError() {
      AuditError event = new AuditError("job-A", "aud-B", "chu-C", "arc-D", "loc-E");
      Field fTimestamp = Event.class.getDeclaredField("timestamp");
      fTimestamp.setAccessible(true);
      fTimestamp.set(event, timestamp);

      Map<String, Object> model = sut.getErrorModel(event);
      assertEquals(5, model.size());
      assertThat(model.get(EventListener.EMAIL_KEY_AUDIT_ID)).isEqualTo("aud-B");
      assertThat(model.get(EventListener.EMAIL_KEY_CHUNK_ID)).isEqualTo("chu-C");
      assertThat(model.get(EventListener.EMAIL_KEY_ARCHIVE_ID)).isEqualTo("arc-D");
      assertThat(model.get(EventListener.EMAIL_KEY_LOCATION)).isEqualTo("loc-E");
      assertThat(model.get(EventListener.EMAIL_KEY_TIMESTAMP)).isEqualTo(timestamp);
    }

    @Test
    void testValidateTypes() {

      List<String> validTypes = List.of(
          TYPE_START,
          TYPE_COMPLETE,
          TYPE_RETRIEVE_START,
          TYPE_RETRIEVE_COMPLETE,
          TYPE_ERROR);

      validTypes.forEach(sut::validateType);

      IllegalArgumentException ex1 = assertThrows(IllegalArgumentException.class,
          () -> sut.validateType(null));
      assertEquals("Invalid Type null", ex1.getMessage());

      IllegalArgumentException ex2 = assertThrows(IllegalArgumentException.class,
          () -> sut.validateType("<invalid>"));
      assertEquals("Invalid Type <invalid>", ex2.getMessage());
    }

    @ParameterizedTest
    @ValueSource(strings = {"", " ", "invalid-email", "user@test.com"})
    @NullSource
    @SneakyThrows
    void testSend(String to) {
      Map<String, Object> model = new HashMap<>();
      lenient().doNothing().when(emailService).sendTemplateMail(to, "subject", "template", model);
      sut.sendTemplateEmail(to, "subject", "template", model);
      int expected = to == null ? 0 : 1;
      verify(emailService, times(expected)).sendTemplateMail(to, "subject", "template", model);
      verifyNoMoreInteractions(emailService);
    }

    @ParameterizedTest
    @ValueSource(ints = {1,2})
    void testProcessAuditChunkStatus(int numChunkStatus) {
      Audit mAudit = mock(Audit.class);
      DepositChunk mDepositChunk = mock(DepositChunk.class);
      EventListener spy = spy(sut);
      Event event = new Event();
      event.setAuditId("audit-id-123");
      event.setChunkId("chunk-id-123");
      event.setArchiveId("archive-id-123");
      event.setLocation("location-abc");

      AuditChunkStatus auditChunkStatus = new AuditChunkStatus();
      assertNull(auditChunkStatus.getCompleteTime());

      List<AuditChunkStatus> values = new ArrayList<>();
      for (int i = 0; i < numChunkStatus; i++) {
        values.add(auditChunkStatus);
      }

      doReturn(mAudit).when(spy).getAudit("audit-id-123");
      doReturn(mDepositChunk).when(spy).getDepositChunk("chunk-id-123");
      doReturn(values).when(auditsService)
          .getRunningAuditChunkStatus(mAudit, mDepositChunk, "archive-id-123", "location-abc");

      AuditChunkStatus result = spy.processAuditChunkStatus(event);

      assertEquals(auditChunkStatus, result);
      assertNotNull(auditChunkStatus.getCompleteTime());

      verify(spy).getAudit("audit-id-123");
      verify(spy).getDepositChunk("chunk-id-123");
      verify(auditsService).getRunningAuditChunkStatus(mAudit, mDepositChunk, "archive-id-123",
          "location-abc");
    }

    @Test
    @SneakyThrows
    void testUpdateDepositWithChunks() {
      Map<Integer, String> digests = new HashMap<>();
      digests.put(3, "hash-three");
      digests.put(1, "hash-one");
      digests.put(2, "hash-two");
      Deposit deposit = new Deposit();
      ChunksDigestEvent event = new ChunksDigestEvent() {
        @Override
        public Map<Integer, String> getChunksDigest() {
          return digests;
        }

        @Override
        public String getDigestAlgorithm() {
          return "algo-123";
        }
      };
      assertEquals(0, deposit.getNumOfChunks());
      assertNull(deposit.getDepositChunks());

      doNothing().when(depositsService).updateDeposit(deposit);
      sut.updateDepositWithChunks(deposit, event);

      verify(depositsService).updateDeposit(deposit);

      List<DepositChunk> chunks = deposit.getDepositChunks();
      assertEquals(3, chunks.size());

      DepositChunk chunk1 = chunks.get(0);
      assertEquals(1, chunk1.getChunkNum());
      assertEquals("hash-one", chunk1.getArchiveDigest());
      assertEquals("algo-123", chunk1.getArchiveDigestAlgorithm());
      assertEquals(deposit, chunk1.getDeposit());

      DepositChunk chunk2 = chunks.get(1);
      assertEquals(2, chunk2.getChunkNum());
      assertEquals("hash-two", chunk2.getArchiveDigest());
      assertEquals("algo-123", chunk2.getArchiveDigestAlgorithm());
      assertEquals(deposit, chunk2.getDeposit());

      DepositChunk chunk3 = chunks.get(2);
      assertEquals(3, chunk3.getChunkNum());
      assertEquals("hash-three", chunk3.getArchiveDigest());
      assertEquals("algo-123", chunk3.getArchiveDigestAlgorithm());
      assertEquals(deposit, chunk3.getDeposit());
    }
  }

  @Nested
  class ProcessDepositJobAndVaultTests {

    private <T> Consumer<T> getFailsFirstTimeConsumer() {
      return new Consumer<>() {
        int count = 0;

        @Override
        public void accept(T vault) {
          count++;
          if (count == 1) {
            throw new StaleObjectStateException("blah", "blah");
          }
        }
      };
    }

    EventListener spy;

    @BeforeEach
    void init() {
      spy = spy(sut);
    }

    @Test
    void testProcessDeposit() {
      Deposit deposit1 = new Deposit() {
        @Override
        public String getID() {
          return "deposit-id-123";
        }
      };
      Deposit deposit2 = new Deposit() {
        @Override
        public String getID() {
          return "deposit-id-123";
        }
      };
      doReturn(deposit2).when(spy).getDeposit("deposit-id-123");
      spy.processDeposit(deposit1, getFailsFirstTimeConsumer());
      verify(spy, times(1)).getDeposit("deposit-id-123");
    }

    @Test
    void testProcessJob() {
      Job job1 = new Job() {
        @Override
        public String getID() {
          return "job-id-123";
        }
      };
      Job job2 = new Job() {
        @Override
        public String getID() {
          return "job-id-123";
        }
      };
      doReturn(job2).when(spy).getJob("job-id-123");
      spy.processJob(job1, getFailsFirstTimeConsumer());
      verify(spy, times(1)).getJob("job-id-123");
    }

    @Test
    void testProcessVault() {
      Vault vault1 = new Vault() {
        @Override
        public String getID() {
          return "vault-id-123";
        }
      };
      Vault vault2 = new Vault() {
        @Override
        public String getID() {
          return "vault-id-123";
        }
      };
      doReturn(vault2).when(spy).getVault("vault-id-123");
      spy.processVault(vault1, getFailsFirstTimeConsumer());
      verify(spy, times(1)).getVault("vault-id-123");
    }
  }
  static class TestEvent extends Event {
  }
  public static Stream<Arguments> eventForTestModel() {
    Complete event = new Complete();
    Error err = new Error();
    err.setMessage("error-message");
    RetrieveStart start = new RetrieveStart();
    RetrieveComplete complete = new RetrieveComplete();
    return Stream.of(event, err, start, complete).map(Arguments::of);
  }
}