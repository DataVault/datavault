package org.datavaultplatform.common.model.dao;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.datavaultplatform.broker.app.DataVaultBrokerApp;
import org.datavaultplatform.broker.test.AddTestProperties;
import org.datavaultplatform.broker.test.BaseDatabaseTest;
import org.datavaultplatform.broker.test.TestUtils;
import org.datavaultplatform.common.event.Event;
import org.datavaultplatform.common.event.deposit.Complete;
import org.datavaultplatform.common.event.deposit.ComputedChunks;
import org.datavaultplatform.common.event.deposit.ComputedEncryption;
import org.datavaultplatform.common.event.deposit.UploadComplete;
import org.datavaultplatform.common.model.Vault;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest(classes = DataVaultBrokerApp.class)
@AddTestProperties
@Slf4j
@TestPropertySource(properties = {
    "broker.email.enabled=false",
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
  JdbcTemplate template;

  @Test
  public void testComputedChunksChunksDigestBLOB() {
    ComputedChunks event = getEvent1_ComputedChunkEvent();
    dao.save(event);

    Event foundEvent = dao.findById(event.getID()).get();
    assertTrue(foundEvent instanceof ComputedChunks);
    ComputedChunks foundCCEvent = (ComputedChunks) foundEvent;

    assertEquals(event.getChunksDigest(), foundCCEvent.getChunksDigest());
  }

  @Test
  public void testCompleteArchiveIdsBLOB() {
    Complete event = getEvent2_CompleteEvent();
    dao.save(event);

    Event foundEvent = dao.findById(event.getID()).get();
    assertTrue(foundEvent instanceof Complete);
    Complete foundCompleteEvent = (Complete) foundEvent;

    assertEquals(event.getArchiveIds(), foundCompleteEvent.getArchiveIds());
  }

  @Test
  public void testComputedEncryptionEventBLOBS() {
    ComputedEncryption event = getEvent3_ComputedEncryption();
    dao.save(event);

    Event foundEvent = dao.findById(event.getID()).get();
    assertTrue(foundEvent instanceof ComputedEncryption);
    ComputedEncryption foundComputedEncryptionEvent = (ComputedEncryption) foundEvent;

    assertEquals(event.getChunksDigest(), foundComputedEncryptionEvent.getChunksDigest());
    assertEquals(event.getEncChunkDigests(), foundComputedEncryptionEvent.getEncChunkDigests());
    assertEquals(event.getChunkIVs().keySet(), foundComputedEncryptionEvent.getChunkIVs().keySet());

    for(Integer key : event.getChunkIVs().keySet()){
      assertArrayEquals(event.getChunkIVs().get(key), foundComputedEncryptionEvent.getChunkIVs().get(key));
    }
  }

  @Test
  public void testUploadCompleteArchiveIdsBLOB() {
    UploadComplete event = getEvent4_UploadComplete();
    dao.save(event);

    Event foundEvent = dao.findById(event.getID()).get();
    assertTrue(foundEvent instanceof UploadComplete);
    UploadComplete foundCompleteEvent = (UploadComplete) foundEvent;

    assertEquals(event.getArchiveIds(), foundCompleteEvent.getArchiveIds());
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
        Arrays.stream(expected).map(Event::getMessage).sorted().collect(Collectors.toList()),
        actual.stream().map(Event::getMessage).sorted().collect(Collectors.toList()));
  }
  void checkOrderOfEventMessages(Collection<Event> actual, Event... expected){
    assertEquals(
        Arrays.stream(expected).map(Event::getMessage).collect(Collectors.toList()),
        actual.stream().map(Event::getMessage).collect(Collectors.toList()));
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
    event.setArchiveIds(TestUtils.getRandomMap());
    event.setMessage("event2");
    event.setTimestamp(TestUtils.THREE_YEARS_AGO);
    return event;
  }

  private ComputedEncryption getEvent3_ComputedEncryption() {
    ComputedEncryption event = new ComputedEncryption();
    event.setMessage("event3");
    event.setTarIV(String.join(",", TestUtils.getRandomList()).getBytes(
        StandardCharsets.UTF_8));
    event.setChunkIVs(TestUtils.getRandomMapIntegerKeyByteArrayValue());
    event.setEncChunkDigests(TestUtils.getRandomMapIntegerKey());
    event.setChunksDigest(TestUtils.getRandomMapIntegerKey());
    event.setTimestamp(TestUtils.TWO_YEARS_AGO);
    return event;
  }

  private UploadComplete getEvent4_UploadComplete() {
    UploadComplete event = new UploadComplete();
    event.setArchiveIds(TestUtils.getRandomMap());
    event.setMessage("event4");
    event.setTimestamp(TestUtils.NOW);
    return event;
  }

  @AfterEach
  void tidyUp() {
    template.execute("delete from `Events`");
    assertEquals(0, dao.count());
  }

}
