package org.datavaultplatform.common.model.dao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.datavaultplatform.broker.app.DataVaultBrokerApp;
import org.datavaultplatform.broker.test.AddTestProperties;
import org.datavaultplatform.broker.test.BaseDatabaseTest;
import org.datavaultplatform.common.model.ArchiveStore;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
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
    "broker.initialise.enabled=false",
    "broker.scheduled.enabled=false"
})
public class ArchiveStoreDAOIT extends BaseDatabaseTest {

  @Autowired
  ArchiveStoreDAO dao;

  @Autowired
  JdbcTemplate template;

  @Test
  void testWriteThenRead() {
    ArchiveStore archiveStore1 = getArchiveStore1();

    ArchiveStore archiveStore2 = getArchiveStore2();

    dao.save(archiveStore1);
    assertNotNull(archiveStore1.getID());
    assertEquals(1, count());

    dao.save(archiveStore2);
    assertNotNull(archiveStore2.getID());
    assertEquals(2, count());

    ArchiveStore foundById1 = dao.findById(archiveStore1.getID()).get();
    assertEquals(archiveStore1.getID(), foundById1.getID());

    ArchiveStore foundById2 = dao.findById(archiveStore2.getID()).get();
    assertEquals(archiveStore2.getID(), foundById2.getID());
  }

  @Test
  void testList() {
    ArchiveStore archiveStore1 = getArchiveStore1();

    ArchiveStore archiveStore2 = getArchiveStore2();

    dao.save(archiveStore1);
    assertNotNull(archiveStore1.getID());
    assertEquals(1, count());

    dao.save(archiveStore2);
    assertNotNull(archiveStore2.getID());
    assertEquals(2, count());

    List<ArchiveStore> items = dao.list();
    assertEquals(2, items.size());
    assertEquals(1, items.stream().filter(dr -> dr.getID().equals(archiveStore1.getID())).count());
    assertEquals(1, items.stream().filter(dr -> dr.getID().equals(archiveStore2.getID())).count());
  }


  @Test
  void testUpdate() {
    ArchiveStore store = getArchiveStore1();

    dao.save(store);

    store.setLabel("111-updated");

    dao.update(store);

    ArchiveStore found = dao.findById(store.getID()).get();
    assertEquals(store.getLabel(), found.getLabel());
  }

  @Test
  void testArchiveStorePropertiesBLOB() {
    ArchiveStore store = getArchiveStoreWithProperties();
    dao.save(store);

    ArchiveStore found = dao.findById(store.getID()).get();

    assertEquals(store.getProperties(), found.getProperties());
  }

  @BeforeEach
  void setup() {
    assertEquals(0, count());
  }

  @AfterEach
  void cleanup() {
    template.execute("delete from `ArchiveStores`");
    assertEquals(0, count());
  }

  private ArchiveStore getArchiveStore1() {
    ArchiveStore archiveStore = new ArchiveStore();
    archiveStore.setLabel("111");
    archiveStore.setRetrieveEnabled(false);
    return archiveStore;
  }

  private ArchiveStore getArchiveStore2() {
    ArchiveStore archiveStore = new ArchiveStore();
    archiveStore.setLabel("222");
    archiveStore.setRetrieveEnabled(true);
    return archiveStore;
  }

  private ArchiveStore getArchiveStoreWithProperties() {
    ArchiveStore archiveStore = new ArchiveStore();
    archiveStore.setLabel("222");
    archiveStore.setRetrieveEnabled(true);
    return archiveStore;
  }

  long count() {
    return dao.count();
  }

  @Test
  void testFindForRetrieval(){

    ArchiveStore archiveStore1 = getArchiveStore1();

    ArchiveStore archiveStore2 = getArchiveStore2();

    dao.save(archiveStore1);
    assertNotNull(archiveStore1.getID());
    assertEquals(1, count());

    dao.save(archiveStore2);
    assertNotNull(archiveStore2.getID());
    assertEquals(2, count());

    ArchiveStore result = dao.findForRetrieval();
    assertEquals(archiveStore2.getID(), result.getID());
  }
}
