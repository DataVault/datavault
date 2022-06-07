package org.datavaultplatform.common.model.dao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;
import org.datavaultplatform.broker.app.DataVaultBrokerApp;
import org.datavaultplatform.broker.test.AddTestProperties;
import org.datavaultplatform.broker.test.BaseReuseDatabaseTest;
import org.datavaultplatform.common.model.DepositChunk;
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
public class DepositChunkDAOIT extends BaseReuseDatabaseTest {

  @Autowired
  DepositChunkDAO dao;


  @Test
  void testWriteThenRead() {
    DepositChunk depositChunk1 = getDepositChunk1();

    DepositChunk depositChunk2 = getDepositChunk2();

    dao.save(depositChunk1);
    assertNotNull(depositChunk1.getID());
    assertEquals(1, dao.count());

    dao.save(depositChunk2);
    assertNotNull(depositChunk2.getID());
    assertEquals(2, dao.count());

    DepositChunk foundById1 = dao.findById(depositChunk1.getID()).get();
    assertEquals(depositChunk1.getArchiveDigest(), foundById1.getArchiveDigest());

    DepositChunk foundById2 = dao.findById(depositChunk2.getID()).get();
    assertEquals(depositChunk2.getArchiveDigest(), foundById2.getArchiveDigest());
  }

  @Test
  void testList() {
    DepositChunk depositChunk1 = new DepositChunk();
    DepositChunk depositChunk2 = new DepositChunk();

    dao.save(depositChunk1);
    assertNotNull(depositChunk1.getID());
    assertEquals(1, dao.count());

    dao.save(depositChunk2);
    assertNotNull(depositChunk2.getID());
    assertEquals(2, dao.count());

    List<DepositChunk> items = dao.list();
    assertEquals(2, items.size());
    assertEquals(1, items.stream().filter(dr -> dr.getID().equals(depositChunk1.getID())).count());
    assertEquals(1, items.stream().filter(dr -> dr.getID().equals(depositChunk2.getID())).count());
  }

  @Test
  void testUpdate() {

    DepositChunk depositReview = getDepositChunk1();

    dao.save(depositReview);

    depositReview.setArchiveDigest("ad-updated");

    DepositChunk found1 = dao.findById(depositReview.getID()).get();
    assertEquals("ad1", found1.getArchiveDigest());

    dao.update(depositReview);

    DepositChunk found2 = dao.findById(depositReview.getID()).get();
    assertEquals("ad-updated", found2.getArchiveDigest());

  }

  @Test
  void testListBySortField() {

    DepositChunk depositChunk1 = new DepositChunk();
    depositChunk1.setChunkNum(2);
    DepositChunk depositChunk2 = new DepositChunk();
    depositChunk2.setChunkNum(1);
    dao.save(depositChunk1);
    dao.save(depositChunk2);
    assertEquals(2, dao.count());

    assertEquals(
        Stream.of(depositChunk1, depositChunk2).map(DepositChunk::getID).sorted().collect(Collectors.toList()),
        dao.list("id").stream().map(DepositChunk::getID).collect(Collectors.toList())
    );

    assertEquals(
        Stream.of(depositChunk2, depositChunk1).map(DepositChunk::getID).collect(Collectors.toList()),
        dao.list("chunkNum").stream().map(DepositChunk::getID).collect(Collectors.toList())
    );

    // Anything other than id will be sorted by chunkNum ASC
    assertEquals(
        Stream.of(depositChunk2, depositChunk1).map(DepositChunk::getID).collect(Collectors.toList()),
        dao.list("XXX").stream().map(DepositChunk::getID).collect(Collectors.toList())
    );
  }

  @BeforeEach
  void setup() {
    assertEquals(0, dao.count());
  }

  @AfterEach
  void tidyUp() {
    template.execute("delete from `DepositChunks`");
    assertEquals(0, dao.count());
  }


  DepositChunk getDepositChunk1() {
    DepositChunk result = new DepositChunk();
    result.setArchiveDigest("ad1");
    return result;
  }

  DepositChunk getDepositChunk2(){
    DepositChunk result = new DepositChunk();
    result.setArchiveDigest("ad2");
    return result;
  }
}
