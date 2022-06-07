package org.datavaultplatform.common.model.dao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.datavaultplatform.broker.app.DataVaultBrokerApp;
import org.datavaultplatform.broker.test.AddTestProperties;
import org.datavaultplatform.broker.test.BaseReuseDatabaseTest;
import org.datavaultplatform.common.model.Archive;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
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
public class ArchiveDAOIT extends BaseReuseDatabaseTest {

  @Autowired
  ArchiveDAO dao;

  @Test
  void testWriteThenRead() {
    Archive archive1 = getArchive1();

    Archive archive2 = getArchive2();

    dao.save(archive1);
    assertNotNull(archive1.getId());
    assertEquals(1, count());

    dao.save(archive2);
    assertNotNull(archive2.getId());
    assertEquals(2, count());

    Archive foundById1 = dao.findById(archive1.getId()).get();
    assertEquals(archive1.getId(), foundById1.getId());

    Archive foundById2 = dao.findById(archive2.getId()).get();
    assertEquals(archive2.getId(), foundById2.getId());
  }

  @Test
  void testList() {
    Archive archive1 = getArchive1();

    Archive archive2 = getArchive2();

    dao.save(archive1);
    assertNotNull(archive1.getId());
    assertEquals(1, count());

    dao.save(archive2);
    assertNotNull(archive2.getId());
    assertEquals(2, count());

    List<Archive> items = dao.list();
    assertEquals(2, items.size());
    assertEquals(1, items.stream().filter(dr -> dr.getId().equals(archive1.getId())).count());
    assertEquals(1, items.stream().filter(dr -> dr.getId().equals(archive2.getId())).count());
  }


  @Test
  void testUpdate() {
    Archive archive1 = getArchive1();

    dao.save(archive1);

    archive1.setArchiveId("111-updated");

    dao.update(archive1);

    Archive found = dao.findById(archive1.getId()).get();
    assertEquals(archive1.getArchiveId(), found.getArchiveId());
  }

  @BeforeEach
  void setup() {
    assertEquals(0, count());
  }

  @AfterEach
  void cleanup() {
    template.execute("delete from `Archives`");
    assertEquals(0, count());
  }

  private Archive getArchive1() {
    Archive archive = new Archive();
    archive.setArchiveId("111");
    return archive;
  }

  private Archive getArchive2() {
    Archive archive = new Archive();
    archive.setArchiveId("222");
    return archive;
  }

  long count() {
    return dao.count();
  }
}
