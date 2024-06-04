package org.datavaultplatform.common.model.dao;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.IntStream;


import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.datavaultplatform.broker.app.DataVaultBrokerApp;
import org.datavaultplatform.broker.test.AddTestProperties;
import org.datavaultplatform.broker.test.BaseDatabaseTest;
import org.datavaultplatform.broker.test.TestUtils;
import org.datavaultplatform.common.event.Event;
import org.datavaultplatform.common.event.deposit.*;
import org.datavaultplatform.common.event.Error;
import org.datavaultplatform.common.event.retrieve.RetrieveStart;
import org.datavaultplatform.common.model.Deposit;
import org.datavaultplatform.common.model.Job;
import org.datavaultplatform.common.model.Vault;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest(classes = DataVaultBrokerApp.class)
@AddTestProperties
@Slf4j
@TestPropertySource(properties = {
    "broker.email.enabled=true",
    "broker.controllers.enabled=false",
    "broker.rabbit.enabled=false",
    "broker.scheduled.enabled=false"
})
public class EventDAOIT extends BaseDatabaseTest {

  @Autowired
  EventDAO dao;

  @Autowired
  VaultDAO vaultDAO;

  @Autowired
  DepositDAO depositDAO;
  
  @Autowired
  JobDAO jobDAO;
  
  @Autowired
  JdbcTemplate template;

  @Nested
  class BlobTests {


    @SuppressWarnings("OptionalGetWithoutIsPresent")
    @Test
    public void testComputedChunksChunksDigestBLOB() {
      ComputedChunks event = getEvent1_ComputedChunkEvent();
      dao.save(event);

      Event foundEvent = dao.findById(event.getID()).get();
      assertInstanceOf(ComputedChunks.class, foundEvent);
      ComputedChunks foundCCEvent = (ComputedChunks) foundEvent;

      assertEquals(event.getChunksDigest(), foundCCEvent.getChunksDigest());
    }

    @SuppressWarnings("OptionalGetWithoutIsPresent")
    @Test
    public void testCompleteArchiveIdsBLOB() {
      Complete event = getEvent2_CompleteEvent();
      dao.save(event);

      Event foundEvent = dao.findById(event.getID()).get();
      assertInstanceOf(Complete.class, foundEvent);
      Complete foundCompleteEvent = (Complete) foundEvent;

      assertEquals(event.getArchiveIds(), foundCompleteEvent.getArchiveIds());
    }

    @SuppressWarnings("OptionalGetWithoutIsPresent")
    @Test
    public void testComputedEncryptionEventBLOBS() {
      ComputedEncryption event = getEvent3_ComputedEncryption();
      dao.save(event);

      Event foundEvent = dao.findById(event.getID()).get();
      assertInstanceOf(ComputedEncryption.class, foundEvent);
      ComputedEncryption foundComputedEncryptionEvent = (ComputedEncryption) foundEvent;

      assertEquals(event.getMessage(), foundComputedEncryptionEvent.getMessage());
      assertArrayEquals(event.getTarIV(), foundComputedEncryptionEvent.getTarIV());

      assertEquals(event.getChunksDigest(), foundComputedEncryptionEvent.getChunksDigest());
      assertEquals(event.getEncChunkDigests(), foundComputedEncryptionEvent.getEncChunkDigests());
      assertEquals(event.getChunkIVs().keySet(), foundComputedEncryptionEvent.getChunkIVs().keySet());

      for (Integer key : event.getChunkIVs().keySet()) {
        assertArrayEquals(event.getChunkIVs().get(key), foundComputedEncryptionEvent.getChunkIVs().get(key));
      }
    }

    @SuppressWarnings("OptionalGetWithoutIsPresent")
    @Test
    public void testUploadCompleteArchiveIdsBLOB() {
      UploadComplete event = getEvent4_UploadComplete();
      dao.save(event);

      Event foundEvent = dao.findById(event.getID()).get();
      assertInstanceOf(UploadComplete.class, foundEvent);
      UploadComplete foundCompleteEvent = (UploadComplete) foundEvent;

      assertEquals(event.getArchiveIds(), foundCompleteEvent.getArchiveIds());
    }
  }

  @Test
  void testListWithSort() {

    Event event1 = getEvent1_ComputedChunkEvent();
    Event event2 = getEvent2_CompleteEvent();
    Event event3 = getEvent3_ComputedEncryption();
    Event event4 = getEvent4_UploadComplete();

    dao.save(event1);
    dao.save(event2);
    dao.save(event3);
    dao.save(event4);

    assertEquals(4, dao.count());

    List<Event> events = dao.list();
    checkSameEventMessages(events, event1, event2, event3, event4);

    List<Event> eventsByNull = dao.list(null);
    checkOrderOfEventMessages(eventsByNull, event2, event3, event1, event4);

    List<Event> eventsByTimestamp = dao.list("timestamp");
    checkOrderOfEventMessages(eventsByTimestamp, event2, event3, event1, event4);

    List<Event> eventsById = dao.list("id");
    checkOrderOfEventMessages(eventsById, getEventsSortedById(event1, event2, event3, event4));
  }


  @Test
  void testVaultEvents() {

    Vault vault = VaultDAOIT.getVault1();
    vaultDAO.save(vault);

    Event event1 = getEvent1_ComputedChunkEvent();
    Event event2 = getEvent2_CompleteEvent();
    Event event3 = getEvent3_ComputedEncryption();
    Event event4 = getEvent4_UploadComplete();

    event2.setVault(vault);
    event3.setVault(vault);

    dao.save(event1);
    dao.save(event2);
    dao.save(event3);
    dao.save(event4);

    assertEquals(4, dao.count());

    List<Event> events = dao.findVaultEvents(vault);
    checkOrderOfEventMessages(events, event2, event3);
  }


  Event[] getEventsSortedById(Event... events) {
    return Arrays.stream(events).sorted(Comparator.comparing(Event::getID)).toArray(Event[]::new);
  }

  void checkSameEventMessages(Collection<Event> actual, Event... expected){
    assertEquals(
        Arrays.stream(expected).map(Event::getMessage).sorted().toList(),
        actual.stream().map(Event::getMessage).sorted().toList());
  }
  void checkOrderOfEventMessages(Collection<Event> actual, Event... expected){
    assertEquals(
        Arrays.stream(expected).map(Event::getMessage).toList(),
        actual.stream().map(Event::getMessage).toList());
  }



  private ComputedChunks getEvent1_ComputedChunkEvent() {
    ComputedChunks event = new ComputedChunks();
    event.setChunkId("chunk-id-123");
    event.setChunksDigest(TestUtils.getRandomMapIntegerKey());
    event.setMessage("event1");
    event.setTimestamp(TestUtils.ONE_YEAR_AGO);
    return event;
  }

  private Complete getEvent2_CompleteEvent() {
    Complete event = new Complete();
    event.setArchiveIds(TestUtils.getRandomMap(5));
    event.setMessage("event2");
    event.setTimestamp(TestUtils.THREE_YEARS_AGO);
    return event;
  }

  private ComputedEncryption getEvent3_ComputedEncryption() {
    ComputedEncryption event = new ComputedEncryption();
    event.setMessage("event3");
    event.setTarIV(String.join(",", TestUtils.getRandomList(5)).getBytes(
        StandardCharsets.UTF_8));
    event.setChunkIVs(TestUtils.getRandomMapIntegerKeyByteArrayValue());
    event.setEncChunkDigests(TestUtils.getRandomMapIntegerKey());
    event.setChunksDigest(TestUtils.getRandomMapIntegerKey());
    event.setTimestamp(TestUtils.TWO_YEARS_AGO);
    return event;
  }

  private UploadComplete getEvent4_UploadComplete() {
    UploadComplete event = new UploadComplete();
    event.setArchiveIds(TestUtils.getRandomMap(5));
    event.setMessage("event4");
    event.setTimestamp(TestUtils.NOW);
    return event;
  }

  @AfterEach
  void tidyUp() {
    template.execute("delete from `Events`");
    assertEquals(0, dao.count());
  }
  
  @Nested
  class LatestEventTests {
  
    Deposit deposit1;
    
    Deposit deposit2;

    Deposit deposit3;
    Deposit deposit4;

    Job depJobForDeposit1;
    Job retJobForDeposit1;
    
    Job depJobForDeposit2;
    Job retJobForDeposit2;
    
    Job depJobForDeposit3;
    Job depJobForDeposit4;
    
    Date date1;
    Date date2;
    Date date3;
    Date date4;

    Start e1ForDepJobForDeposit1;
    Start e2ForDepJobForDeposit1;
    Start e3ForDepJobForDeposit1;
    Error e4ForDepJobForDeposit1;

    RetrieveStart e1ForRetJobForDeposit1;
    RetrieveStart e2ForRetJobForDeposit1;
    RetrieveStart e3ForRetJobForDeposit1;
    Error e4ForRetJobForDeposit1;

    Start e1ForDepJobForDeposit2;
    Start e2ForDepJobForDeposit2;
    Start e3ForDepJobForDeposit2;
    Error e4ForDepJobForDeposit2;

    RetrieveStart e1ForRetJobForDeposit2;
    RetrieveStart e2ForRetJobForDeposit2;
    RetrieveStart e3ForRetJobForDeposit2;
    Error e4ForRetJobForDeposit2;

    CompleteCopyUpload e1ForDepJobForDeposit3;
    Complete           e2ForDepJobForDeposit3;
    CompleteCopyUpload e3ForDepJobForDeposit3;
    Error              e4ForDepJobForDeposit3;
    CompleteCopyUpload e5ForDepJobForDeposit3;
    CompleteCopyUpload e6ForDepJobForDeposit3;

    CompleteCopyUpload e1ForDepJobForDeposit4;
    CompleteCopyUpload e2ForDepJobForDeposit4;
    Error              e3ForDepJobForDeposit4;
    CompleteCopyUpload e4ForDepJobForDeposit4;
    CompleteCopyUpload e5ForDepJobForDeposit4;

    @BeforeEach
    void setup() {
      deposit1 = new Deposit();
      deposit1.setHasPersonalData(false);
      deposit1.setName("Deposit1");
      
      deposit2 = new Deposit();
      deposit2.setHasPersonalData(false);
      deposit2.setName("Deposit2");

      deposit3 = new Deposit();
      deposit3.setHasPersonalData(false);
      deposit3.setName("Deposit3");

      deposit4 = new Deposit();
      deposit4.setHasPersonalData(false);
      deposit4.setName("Deposit4");

      depositDAO.save(deposit1);
      depositDAO.save(deposit2);
      depositDAO.save(deposit3);
      depositDAO.save(deposit4);

      depJobForDeposit1 = new Job(Job.TASK_CLASS_DEPOSIT);
      depJobForDeposit1.setDeposit(deposit1);
      retJobForDeposit1 = new Job(Job.TASK_CLASS_RETRIEVE);
      retJobForDeposit1.setDeposit(deposit1);

      depJobForDeposit2 = new Job(Job.TASK_CLASS_DEPOSIT);
      depJobForDeposit2.setDeposit(deposit2);
      retJobForDeposit2 = new Job(Job.TASK_CLASS_RETRIEVE);
      retJobForDeposit2.setDeposit(deposit2);

      depJobForDeposit3 = new Job(Job.TASK_CLASS_DEPOSIT);
      depJobForDeposit3.setDeposit(deposit3);

      depJobForDeposit4 = new Job(Job.TASK_CLASS_DEPOSIT);
      depJobForDeposit4.setDeposit(deposit3);

      jobDAO.save(depJobForDeposit1);
      jobDAO.save(retJobForDeposit1);

      jobDAO.save(depJobForDeposit2);
      jobDAO.save(retJobForDeposit2);

      jobDAO.save(depJobForDeposit3);
      jobDAO.save(depJobForDeposit4);

      date1 = new Date();
      date2 = new Date(date1.getTime() + 1000);
      date3 = new Date(date1.getTime() + 2000);
      date4 = new Date(date1.getTime() + 3000);
      
      e1ForDepJobForDeposit1 = createEvent(Start.class,deposit1, depJobForDeposit1,  date1, 4, "e1ForDepJobForDeposit1");
      e2ForDepJobForDeposit1 = createEvent(Start.class,deposit1, depJobForDeposit1,  date2, 3, "e2ForDepJobForDeposit1");
      e3ForDepJobForDeposit1 = createEvent(Start.class,deposit1, depJobForDeposit1,  date3, 2, "e3ForDepJobForDeposit1");
      e4ForDepJobForDeposit1 = createError(deposit1, depJobForDeposit1,  date4, 1, "e4ForDepJobForDeposit1");

      e1ForDepJobForDeposit2 = createEvent(Start.class, deposit2, depJobForDeposit2,  date1, 4, "e1ForDepJobForDeposit2");
      e2ForDepJobForDeposit2 = createEvent(Start.class, deposit2, depJobForDeposit2,  date2, 3, "e2ForDepJobForDeposit2");
      e3ForDepJobForDeposit2 = createEvent(Start.class, deposit2, depJobForDeposit2,  date3, 2, "e3ForDepJobForDeposit2");
      e4ForDepJobForDeposit2 = createError(deposit2, depJobForDeposit2,  date4, 1, "e4ForDepJobForDeposit2");

      e1ForRetJobForDeposit1 = createEvent(RetrieveStart.class, deposit1, retJobForDeposit1,  date1, 1, "e1ForRetJobForDeposit1");
      e2ForRetJobForDeposit1 = createEvent(RetrieveStart.class, deposit1, retJobForDeposit1,  date2, 3, "e2ForRetJobForDeposit1");
      e3ForRetJobForDeposit1 = createEvent(RetrieveStart.class, deposit1, retJobForDeposit1,  date2, 2, "e3ForRetJobForDeposit1");
      e4ForRetJobForDeposit1 = createError(deposit1, retJobForDeposit1,  date4, 4, "e4ForRetJobForDeposit1");

      e1ForRetJobForDeposit2 = createEvent(RetrieveStart.class, deposit2, retJobForDeposit2,  date1, 1, "e1ForRetJobForDeposit2");
      e2ForRetJobForDeposit2 = createEvent(RetrieveStart.class, deposit2, retJobForDeposit2,  date2, 3, "e2ForRetJobForDeposit2");
      e3ForRetJobForDeposit2 = createEvent(RetrieveStart.class, deposit2, retJobForDeposit2,  date2, 2, "e3ForRetJobForDeposit2");
      e4ForRetJobForDeposit2 = createError(deposit2, retJobForDeposit2,  date2, 4, "e4ForRetJobForDeposit2");

      e1ForDepJobForDeposit3 = createEvent(CompleteCopyUpload.class, deposit3, depJobForDeposit3,  date1, 1, "e1ForDepJobForDeposit3");
      e2ForDepJobForDeposit3 = createEvent(Complete.class,           deposit3, depJobForDeposit3,  date2, 2, "e2ForDepJobForDeposit3");
      e3ForDepJobForDeposit3 = createEvent(CompleteCopyUpload.class, deposit3, depJobForDeposit3,  date3, 3, "e3ForDepJobForDeposit3");
      e4ForDepJobForDeposit3 = createError(deposit3, depJobForDeposit3,  date3, 4, "e4ForDepJobForDeposit3");
      e5ForDepJobForDeposit3 = createEvent(CompleteCopyUpload.class, deposit3, depJobForDeposit3,  date4, 5, "e5ForDepJobForDeposit3");
      e6ForDepJobForDeposit3 = createEvent(CompleteCopyUpload.class, deposit3, depJobForDeposit3,  date4, 6, "e6ForDepJobForDeposit3");

      e1ForDepJobForDeposit4 = createEvent(CompleteCopyUpload.class, deposit4, depJobForDeposit4,  date1, 12345, "e1ForDepJobForDeposit4");
      e2ForDepJobForDeposit4 = createEvent(CompleteCopyUpload.class, deposit4, depJobForDeposit4,  date2, 1, "e2ForDepJobForDeposit4");
      e3ForDepJobForDeposit4 = createError(deposit4, depJobForDeposit4,  date3, 123, "e3ForDepJobForDeposit4");
      e4ForDepJobForDeposit4 = createEvent(CompleteCopyUpload.class, deposit4, depJobForDeposit4,  date3, 12, "e3ForDepJobForDeposit4");
      e5ForDepJobForDeposit4 = createEvent(CompleteCopyUpload.class, deposit4, depJobForDeposit4,  date4, 1234, "e5ForDepJobForDeposit4");

      assertThat(dao.count()).isEqualTo(27);
    }
    
    @Test
    void testNoneFound() {
        assertThat(dao.findLatestEventByDepositIdAndJobTaskClass("depositId","bob")).isEmpty();
    }
    
    @Test
    void testFindLatestEvent() {
      checkFindLatestEvent(deposit1, depJobForDeposit1, e3ForDepJobForDeposit1);
      checkFindLatestEvent(deposit1, retJobForDeposit1, e2ForRetJobForDeposit1);
      checkFindLatestEvent(deposit2, depJobForDeposit2, e3ForDepJobForDeposit2);
      checkFindLatestEvent(deposit2, retJobForDeposit2, e2ForRetJobForDeposit2);
    }
    
    @Test
    void testUploadedChunkNumbers() {
      checkUploadedChunkNumbers(deposit3, List.of(3,5,6));
      checkUploadedChunkNumbers(deposit4, List.of(1,12,1234,12345));
    }
    
    @SuppressWarnings("OptionalGetWithoutIsPresent")
    void checkFindLatestEvent(Deposit deposit, Job job, Event expectedEvent) {
      Optional<Event> result = dao.findLatestEventByDepositIdAndJobTaskClass(deposit.getID(), job.getTaskClass());
      assertThat(result.get().getID()).isEqualTo(expectedEvent.getID());
    }
    
    void checkUploadedChunkNumbers(Deposit deposit, List<Integer> expectedChunkNumbers){
      assertThat(dao.findDepositChunksStored(deposit.getID())).isEqualTo(expectedChunkNumbers);
    }
  }

  @Nested
  class FindDepositChunksStoredTests {
    @Test
    void testNullDepositIt() {
      assertThat(dao.findDepositChunksStored(null)).isEqualTo(Collections.emptyList());
    }

    @Test
    @Transactional
    void testSingleChunkStored() {
      checkChunksStored(List.of(123));
    }

    @Test
    @Transactional
    void testManyChunks() {
      checkChunksStored(IntStream.rangeClosed(1, 1000).boxed().toList());
    }

    void checkChunksStored(List<Integer> chunkNumbers) {
      Deposit deposit = new Deposit();
      deposit.setName("test-deposit");
      deposit.setHasPersonalData(false);
      depositDAO.save(deposit);

      em.flush();

      String archiveStoreId = "dummy-archive-store-id";
      String archiveId = "test-archive-id";
      String jobId = "dummy-job-id";
      for (var chunkNumber : chunkNumbers) {
        CompleteCopyUpload event = new CompleteCopyUpload(deposit.getID(), jobId, "test-type", chunkNumber, archiveStoreId, archiveId);

        assertThat(event.getID()).isNull();

        // Because of the strange way the deposit/depositId is mapped using JPA - we have to use deposit NOT (Transient) depositId
        event.setDeposit(deposit);

        dao.save(event);
      }

      em.flush();
      assertThat(dao.count()).isEqualTo(chunkNumbers.size());

      List<Integer> result = dao.findDepositChunksStored(deposit.getID());
      assertThat(result).isEqualTo(chunkNumbers);
    }

    @Transactional
    @Test
    void testWithNoChunkNumber() {
      Deposit deposit = new Deposit();
      deposit.setName("test-deposit");
      deposit.setHasPersonalData(false);
      depositDAO.save(deposit);

      em.flush();

      String archiveStoreId = "dummy-archive-store-id";
      String archiveId = "test-archive-id";
      String jobId = "dummy-job-id";
      CompleteCopyUpload event = new CompleteCopyUpload(deposit.getID(), jobId, "test-type", null, archiveStoreId, archiveId);

      assertThat(event.getID()).isNull();

      // Because of the strange way the deposit/depositId is mapped using JPA - we have to use deposit NOT (Transient) depositId
      event.setDeposit(deposit);

      dao.save(event);

      em.flush();
      assertThat(dao.count()).isEqualTo(1);

      Event stored = dao.findById(event.getID()).get();
      assertThat(stored.getID()).isEqualTo(event.getID());
      assertThat(stored.getChunkNumber()).isNull();
      
      List<Integer> result = dao.findDepositChunksStored(deposit.getID());
      assertThat(result).isEmpty();
    }
  }
  @SneakyThrows
  <T extends Event> T createEvent(Class<T> clazz, Deposit deposit, Job job, Date timestamp, int sequence, String message) {
    T event = clazz.getConstructor().newInstance();
    event.setEventClass(event.getClass().getName());
    event.setMessage(message);
    event.setDeposit(deposit);
    event.setJob(job);
    event.setTimestamp(timestamp);
    event.setSequence(sequence);
    if (clazz.equals(CompleteCopyUpload.class)){
      event.setChunkNumber(sequence);
    }
    dao.save(event);
    return event;
  }

  Error createError(Deposit deposit, Job job, Date timestamp, int sequence, String message){
    Error event = new Error();
    event.setEventClass(event.getClass().getName());
    event.setMessage(message);
    event.setDeposit(deposit);
    event.setJob(job);
    event.setTimestamp(timestamp);
    event.setSequence(sequence);
    dao.save(event);
    return event;
  }
}
