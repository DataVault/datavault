package org.datavaultplatform.common.model.dao;

import static org.datavaultplatform.broker.test.TestUtils.NOW;
import static org.datavaultplatform.broker.test.TestUtils.ONE_WEEK_AGO;
import static org.datavaultplatform.broker.test.TestUtils.TWO_WEEKS_AGO;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.datavaultplatform.broker.app.DataVaultBrokerApp;
import org.datavaultplatform.broker.test.AddTestProperties;
import org.datavaultplatform.broker.test.BaseReuseDatabaseTest;
import org.datavaultplatform.broker.test.TestUtils;
import org.datavaultplatform.common.model.Audit;
import org.datavaultplatform.common.model.AuditChunkStatus;
import org.datavaultplatform.common.model.AuditChunkStatus.Status;
import org.datavaultplatform.common.model.Deposit;
import org.datavaultplatform.common.model.DepositChunk;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
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
public class AuditChunkStatusDAOIT extends BaseReuseDatabaseTest {

  @Autowired
  AuditChunkStatusDAO dao;

  @Autowired
  AuditDAO auditDAO;

  @Autowired
  DepositChunkDAO depositChunkDAO;

  @Autowired
  DepositDAO depositDAO;

  @Autowired
  JdbcTemplate template;

  @Test
  void testWriteThenRead() {
    AuditChunkStatus auditChunkStatus1 = getAuditChunkStatus1();

    AuditChunkStatus auditChunkStatus2 = getAuditChunkStatus2();

    dao.save(auditChunkStatus1);
    assertNotNull(auditChunkStatus1.getID());
    assertEquals(1, count());

    dao.save(auditChunkStatus2);
    assertNotNull(auditChunkStatus2.getID());
    assertEquals(2, count());

    AuditChunkStatus foundById1 = dao.findById(auditChunkStatus1.getID()).get();
    assertEquals(auditChunkStatus1.getID(), foundById1.getID());

    AuditChunkStatus foundById2 = dao.findById(auditChunkStatus2.getID()).get();
    assertEquals(auditChunkStatus2.getID(), foundById2.getID());
  }

  @Test
  void testListIsSortedByAscendingTimestamp() {
    AuditChunkStatus auditChunkStatus1 = getAuditChunkStatus1();

    AuditChunkStatus auditChunkStatus2 = getAuditChunkStatus2();

    AuditChunkStatus auditChunkStatus3 = getAuditChunkStatus3();

    dao.save(auditChunkStatus1);
    assertNotNull(auditChunkStatus1.getID());
    assertEquals(1, count());

    dao.save(auditChunkStatus2);
    assertNotNull(auditChunkStatus2.getID());
    assertEquals(2, count());

    dao.save(auditChunkStatus3);
    assertNotNull(auditChunkStatus3.getID());
    assertEquals(3, count());

    List<AuditChunkStatus> items = dao.list();
    assertEquals(3, items.size());
    assertEquals(1, items.stream().filter(dr -> dr.getID().equals(auditChunkStatus1.getID())).count());
    assertEquals(1, items.stream().filter(dr -> dr.getID().equals(auditChunkStatus2.getID())).count());
    assertEquals(1, items.stream().filter(dr -> dr.getID().equals(auditChunkStatus3.getID())).count());

    // The AuditChunkStatus should be ordered by Ascending Timestamp
    assertEquals(
        Arrays.asList(
            auditChunkStatus3.getID(),
            auditChunkStatus1.getID(),
            auditChunkStatus2.getID()),
        items.stream().map(AuditChunkStatus::getID).collect(Collectors.toList()));
  }

  @Test
  void testUpdate() {
    AuditChunkStatus auditChunkStatus1 = getAuditChunkStatus1();

    dao.save(auditChunkStatus1);

    auditChunkStatus1.setNote("111-updated");

    dao.update(auditChunkStatus1);

    AuditChunkStatus found = dao.findById(auditChunkStatus1.getID()).get();
    assertEquals(auditChunkStatus1.getNote(), found.getNote());
  }

  @BeforeEach
  void setup() {
    //assertEquals(0, count());
  }

  @AfterEach
  void cleanup() {
    template.execute("delete from `AuditChunkStatus`");
    template.execute("delete from `DepositChunks`");
    template.execute("delete from `Deposits`");
    //assertEquals(0, count());
  }

  private AuditChunkStatus getAuditChunkStatus1() {
    AuditChunkStatus result = new AuditChunkStatus();
    result.setTimestamp(ONE_WEEK_AGO);
    result.setNote("111");
    return result;
  }

  private AuditChunkStatus getAuditChunkStatus2() {
    AuditChunkStatus result = new AuditChunkStatus();
    result.setTimestamp(NOW);
    result.setNote("222");
    return result;
  }

  private AuditChunkStatus getAuditChunkStatus3() {
    AuditChunkStatus result = new AuditChunkStatus();
    result.setTimestamp(TWO_WEEKS_AGO);
    result.setNote("333");
    return result;
  }


  long count() {
    return dao.count();
  }

  @Test
  void testFindByAudit() {

    Audit audit1 = new Audit();
    audit1.setNote("audit-1");

    Audit audit2 = new Audit();
    audit2.setNote("audit-2");

    auditDAO.save(audit1);
    auditDAO.save(audit2);

    AuditChunkStatus item1 = getAuditChunkStatus1();
    item1.setAudit(audit1);
    dao.save(item1);

    AuditChunkStatus item2 = getAuditChunkStatus2();
    item2.setAudit(audit2);
    dao.save(item2);

    AuditChunkStatus item3 = getAuditChunkStatus3();
    item3.setAudit(audit1);
    dao.save(item3);

    assertEquals(3, dao.findAll().size());

    List<AuditChunkStatus> byAudit1 = dao.findByAudit(audit1);
    assertEquals(2, byAudit1.size());
    assertTrue(byAudit1.stream().map(AuditChunkStatus::getID).collect(Collectors.toList()).contains(item1.getID()));
    assertTrue(byAudit1.stream().map(AuditChunkStatus::getID).collect(Collectors.toList()).contains(item3.getID()));

    List<AuditChunkStatus> byAudit2 = dao.findByAudit(audit2);
    assertEquals(1, byAudit2.size());
    assertTrue(byAudit2.stream().map(AuditChunkStatus::getID).collect(Collectors.toList()).contains(item2.getID()));
  }

  @Test
  void testFindByDepositChunk() {

    DepositChunk depositChunk1 = new DepositChunk();
    depositChunk1.setChunkNum(1);
    depositChunk1.setArchiveDigest("chunk-1");

    DepositChunk depositChunk2 = new DepositChunk();
    depositChunk1.setChunkNum(2);
    depositChunk1.setArchiveDigest("chunk-2");

    depositChunkDAO.save(depositChunk1);
    depositChunkDAO.save(depositChunk2);

    AuditChunkStatus item1 = getAuditChunkStatus1();
    item1.setDepositChunk(depositChunk1);
    dao.save(item1);

    AuditChunkStatus item2 = getAuditChunkStatus2();
    item2.setDepositChunk(depositChunk2);
    dao.save(item2);

    AuditChunkStatus item3 = getAuditChunkStatus3();
    item3.setDepositChunk(depositChunk1);
    dao.save(item3);

    assertEquals(3, dao.findAll().size());

    List<AuditChunkStatus> byDepositChunk1 = dao.findByDepositChunk(depositChunk1);
    assertEquals(2, byDepositChunk1.size());
    assertTrue(byDepositChunk1.stream().map(AuditChunkStatus::getID).collect(Collectors.toList()).contains(item1.getID()));
    assertTrue(byDepositChunk1.stream().map(AuditChunkStatus::getID).collect(Collectors.toList()).contains(item3.getID()));

    List<AuditChunkStatus> byDepositChunk2 = dao.findByDepositChunk(depositChunk2);
    assertEquals(1, byDepositChunk2.size());
    assertTrue(byDepositChunk2.stream().map(AuditChunkStatus::getID).collect(Collectors.toList()).contains(item2.getID()));
  }

  @Test
  void testGetLastChunkAuditTime() {

    DepositChunk depositChunk1 = new DepositChunk();
    depositChunk1.setChunkNum(1);
    depositChunk1.setArchiveDigest("chunk-1");

    DepositChunk depositChunk2 = new DepositChunk();
    depositChunk1.setChunkNum(2);
    depositChunk1.setArchiveDigest("chunk-2");

    depositChunkDAO.save(depositChunk1);
    depositChunkDAO.save(depositChunk2);

    AuditChunkStatus item1 = getAuditChunkStatus1();
    item1.setTimestamp(TestUtils.TWO_YEARS_AGO);
    item1.setDepositChunk(depositChunk1);
    dao.save(item1);

    AuditChunkStatus item2 = getAuditChunkStatus2();
    item2.setDepositChunk(depositChunk2);
    dao.save(item2);

    AuditChunkStatus item3 = getAuditChunkStatus3();
    item3.setDepositChunk(depositChunk1);
    item3.setTimestamp(TestUtils.ONE_YEAR_AGO);
    dao.save(item3);

    assertEquals(3, dao.findAll().size());

    AuditChunkStatus last1 = dao.getLastChunkAuditTime(depositChunk1);
    assertEquals(item3.getID(), last1.getID());

    AuditChunkStatus last2 = dao.getLastChunkAuditTime(depositChunk2);
    assertEquals(item2.getID(), last2.getID());
  }

  @Test
  void testFindByMultiple() {

    AuditChunkStatus item1 = getAuditChunkStatus1();
    item1.setStatus(Status.COMPLETE);
    item1.setNote("noteA");
    item1.setTimestamp(TestUtils.TWO_YEARS_AGO);
    dao.save(item1);

    AuditChunkStatus item2 = getAuditChunkStatus2();
    item2.setStatus(Status.ERROR);
    item2.setNote("noteA");
    dao.save(item2);

    AuditChunkStatus item3 = getAuditChunkStatus3();
    item3.setTimestamp(TestUtils.ONE_YEAR_AGO);
    item3.setStatus(Status.IN_PROGRESS);
    item3.setNote("noteB");
    dao.save(item3);

    assertEquals(3, dao.findAll().size());

    assertEquals(item1.getID(), dao.findBy(new HashMap<String, Object>(){
      {
        this.put("status", Status.COMPLETE);
      }
    }).get(0).getID());

    assertEquals(item2.getID(), dao.findBy(new HashMap<String, Object>(){
      {
        this.put("status", Status.ERROR);
      }
    }).get(0).getID());


    assertEquals(item3.getID(), dao.findBy(new HashMap<String, Object>(){
      {
        this.put("status", Status.IN_PROGRESS);
      }
    }).get(0).getID());

    assertEquals(new HashSet<>(Arrays.asList(item1.getID(), item2.getID())), dao.findBy(new HashMap<String, Object>(){
      {
        this.put("note", "noteA");
      }
    }).stream().map(AuditChunkStatus::getID).collect(Collectors.toSet()));

    assertEquals(new HashSet<>(Arrays.asList(item3.getID())), dao.findBy(new HashMap<String, Object>(){
      {
        this.put("note", "noteB");
      }
    }).stream().map(AuditChunkStatus::getID).collect(Collectors.toSet()));


    assertEquals(new HashSet<>(Arrays.asList(item1.getID())), dao.findBy(new HashMap<String, Object>(){
      {
        this.put("note", "noteA");
        this.put("status", Status.COMPLETE);
      }
    }).stream().map(AuditChunkStatus::getID).collect(Collectors.toSet()));
  }

  @Test
  void testFindBySingle() {

    AuditChunkStatus item1 = getAuditChunkStatus1();
    item1.setStatus(Status.COMPLETE);
    item1.setNote("noteA");
    item1.setTimestamp(TestUtils.TWO_YEARS_AGO);
    dao.save(item1);

    AuditChunkStatus item2 = getAuditChunkStatus2();
    item2.setStatus(Status.ERROR);
    item2.setNote("noteA");
    dao.save(item2);

    AuditChunkStatus item3 = getAuditChunkStatus3();
    item3.setTimestamp(TestUtils.ONE_YEAR_AGO);
    item3.setStatus(Status.IN_PROGRESS);
    item3.setNote("noteB");
    dao.save(item3);

    assertEquals(3, dao.findAll().size());

    assertEquals(item1.getID(), dao.findBy("status", Status.COMPLETE).get(0).getID());

    assertEquals(item2.getID(), dao.findBy("status", Status.ERROR).get(0).getID());

    assertEquals(item3.getID(), dao.findBy("status", Status.IN_PROGRESS).get(0).getID());

    assertEquals(new HashSet<>(Arrays.asList(item1.getID(), item2.getID())), dao.findBy("note", "noteA").stream().map(AuditChunkStatus::getID).collect(Collectors.toSet()));

    assertEquals(new HashSet<>(Arrays.asList(item3.getID())), dao.findBy("note", "noteB").stream().map(AuditChunkStatus::getID).collect(Collectors.toSet()));
  }

  @Test
  void testFindByDepositChunkId() {

    DepositChunk depositChunk1 = new DepositChunk();
    depositChunk1.setChunkNum(1);
    depositChunk1.setArchiveDigest("chunk-1");

    DepositChunk depositChunk2 = new DepositChunk();
    depositChunk1.setChunkNum(2);
    depositChunk1.setArchiveDigest("chunk-2");

    depositChunkDAO.save(depositChunk1);
    depositChunkDAO.save(depositChunk2);

    AuditChunkStatus item1 = getAuditChunkStatus1();
    item1.setDepositChunk(depositChunk1);
    dao.save(item1);

    AuditChunkStatus item2 = getAuditChunkStatus2();
    item2.setDepositChunk(depositChunk2);
    dao.save(item2);

    AuditChunkStatus item3 = getAuditChunkStatus3();
    item3.setDepositChunk(depositChunk1);
    dao.save(item3);

    assertEquals(3, dao.findAll().size());

    List<AuditChunkStatus> byDepositChunk1 = dao.findByDepositChunkId(depositChunk1.getID());
    assertEquals(2, byDepositChunk1.size());
    assertTrue(byDepositChunk1.stream().map(AuditChunkStatus::getID).collect(Collectors.toList()).contains(item1.getID()));
    assertTrue(byDepositChunk1.stream().map(AuditChunkStatus::getID).collect(Collectors.toList()).contains(item3.getID()));

    List<AuditChunkStatus> byDepositChunk2 = dao.findByDepositChunkId(depositChunk2.getID());
    assertEquals(1, byDepositChunk2.size());
    assertTrue(byDepositChunk2.stream().map(AuditChunkStatus::getID).collect(Collectors.toList()).contains(item2.getID()));
  }


  @Test
  void testFindByDepositAndDepositId() {

    DepositChunk depositChunk1 = new DepositChunk();
    depositChunk1.setChunkNum(1);
    depositChunk1.setArchiveDigest("chunk-1");

    DepositChunk depositChunk2 = new DepositChunk();
    depositChunk1.setChunkNum(2);
    depositChunk1.setArchiveDigest("chunk-2");

    Deposit deposit1 = new Deposit();
    deposit1.setHasPersonalData(false);
    deposit1.setName("deposit-1");

    Deposit deposit2 = new Deposit();
    deposit2.setHasPersonalData(true);
    deposit2.setName("deposit-2");

    deposit1.setDepositChunks(Collections.singletonList(depositChunk1));
    depositChunk1.setDeposit(deposit1);

    deposit2.setDepositChunks(Collections.singletonList(depositChunk2));
    depositChunk2.setDeposit(deposit2);

    assertEquals(0, depositChunkDAO.findAll().size());

    depositDAO.save(deposit1);
    depositDAO.save(deposit2);

    assertEquals(2, depositChunkDAO.findAll().size());
    assertEquals(0, dao.findAll().size());

    AuditChunkStatus item1 = getAuditChunkStatus1();
    item1.setDepositChunk(depositChunk1);
    dao.save(item1);

    AuditChunkStatus item2 = getAuditChunkStatus2();
    item2.setDepositChunk(depositChunk2);
    dao.save(item2);

    AuditChunkStatus item3 = getAuditChunkStatus3();
    item3.setDepositChunk(depositChunk1);
    dao.save(item3);

    assertEquals(3, dao.findAll().size());

    List<AuditChunkStatus> byDepositChunk0 = dao.findByDepositChunkId(depositChunk1.getID());
    assertEquals(2, byDepositChunk0.size());

    List<AuditChunkStatus> byDepositChunk00 = dao.findByDepositChunk(depositChunk1);
    assertEquals(2, byDepositChunk00.size());


    long countACS = dao.count();
    long countDeposit = depositDAO.count();
    long countDepositChunk = depositChunkDAO.count();

    assertEquals(3, countACS);
    assertEquals(2, countDeposit);
    assertEquals(2, countDepositChunk);

    List<DepositChunk> chunks = depositChunkDAO.findAll();

    List<String> acsidDepost1 = template.queryForList("select distinct acs.id from `AuditChunkStatus` acs INNER JOIN `DepositChunks` dc on acs.depositChunk_id = dc.id where dc.deposit_id = ?", String.class, deposit1.getID());
    assertEquals(2, acsidDepost1.size());

    List<String> acsidDepost2 = template.queryForList("select distinct acs.id from `AuditChunkStatus` acs INNER JOIN `DepositChunks` dc on acs.depositChunk_id = dc.id where dc.deposit_id = ?", String.class, deposit2.getID());
    assertEquals(1, acsidDepost2.size());

    List<AuditChunkStatus> byDeposit1 = dao.findByDeposit(deposit1);
    assertEquals(2, byDeposit1.size());
    assertTrue(byDeposit1.stream().map(AuditChunkStatus::getID).collect(Collectors.toList()).contains(item1.getID()));
    assertTrue(byDeposit1.stream().map(AuditChunkStatus::getID).collect(Collectors.toList()).contains(item3.getID()));

    List<AuditChunkStatus> byDeposit2 = dao.findByDeposit(deposit2);
    assertEquals(1, byDeposit2.size());
    assertTrue(byDeposit2.stream().map(AuditChunkStatus::getID).collect(Collectors.toList()).contains(item2.getID()));

    List<AuditChunkStatus> byDepositId1 = dao.findByDepositId(deposit1.getID());
    assertEquals(2, byDepositId1.size());
    assertTrue(byDepositId1.stream().map(AuditChunkStatus::getID).collect(Collectors.toList()).contains(item1.getID()));
    assertTrue(byDepositId1.stream().map(AuditChunkStatus::getID).collect(Collectors.toList()).contains(item3.getID()));

    List<AuditChunkStatus> byDepositId2 = dao.findByDepositId(deposit2.getID());
    assertEquals(1, byDeposit2.size());
    assertTrue(byDepositId2.stream().map(AuditChunkStatus::getID).collect(Collectors.toList()).contains(item2.getID()));
  }
}
