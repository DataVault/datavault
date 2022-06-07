package org.datavaultplatform.broker.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.Date;
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
public class ArchivesServiceIT extends BaseReuseDatabaseTest {

  @Autowired
  ArchivesService service;

  @Autowired
  JdbcTemplate template;

  Date now = new Date();

  @Test
  void testWriteThenRead() {
    Archive arc1 = getArchive1();

    Archive arc2 = getArchive2();

    service.saveArchive(arc1);
    assertNotNull(arc1.getArchiveId());
    assertEquals(1, count());

    service.saveArchive(arc2);
    assertNotNull(arc2.getArchiveId());
    assertEquals(2, count());

    Archive foundById1 = service.findById(arc1.getId());
    assertEquals(arc1.getArchiveId(), foundById1.getArchiveId());

    Archive foundById2 = service.findById(arc2.getId());
    assertEquals(arc2.getArchiveId(), foundById2.getArchiveId());
  }

  @Test
  void testList() {
    Archive arc1 = getArchive1();

    Archive arc2 = getArchive2();

    service.saveArchive(arc1);
    assertEquals(1, count());

    service.saveArchive(arc2);
    assertEquals(2, count());

    List<Archive> items = service.getArchives();
    assertEquals(2, items.size());
    assertEquals(1,items.stream().filter(dr -> dr.getId().equals(arc1.getId())).count());
    assertEquals(1,items.stream().filter(dr -> dr.getId().equals(arc2.getId())).count());
  }


  @Test
  void testUpdate() {
    Archive arc1 = getArchive1();

    service.saveArchive(arc1);

    arc1.setArchiveId("updated-archive-id-1");

    service.updateArchive(arc1);

    Archive found = service.findById(arc1.getId());
    assertEquals(arc1.getArchiveId(), found.getArchiveId());
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
    archive.setArchiveId("arc1-arc2");
    archive.setCreationTime(now);
    return archive;
  }

  private Archive getArchive2() {
    Archive archive = new Archive();
    archive.setArchiveId("user2-user2");
    archive.setCreationTime(now);
    return archive;
  }

  int count() {
    return template.queryForObject(
        "select count(*) from Archives", Integer.class);
  }

}
