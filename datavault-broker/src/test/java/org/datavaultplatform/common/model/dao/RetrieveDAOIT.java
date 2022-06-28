package org.datavaultplatform.common.model.dao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.datavaultplatform.broker.app.DataVaultBrokerApp;
import org.datavaultplatform.broker.test.AddTestProperties;
import org.datavaultplatform.broker.test.BaseReuseDatabaseTest;
import org.datavaultplatform.broker.test.TestUtils;
import org.datavaultplatform.common.model.Deposit;
import org.datavaultplatform.common.model.Group;
import org.datavaultplatform.common.model.Permission;
import org.datavaultplatform.common.model.Retrieve;
import org.datavaultplatform.common.model.Retrieve.Status;
import org.datavaultplatform.common.model.Vault;
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
public class RetrieveDAOIT extends BaseReuseDatabaseTest {

  @Autowired
  RetrieveDAO dao;

  @Autowired
  GroupDAO groupDAO;

  @Autowired
  DepositDAO depositDAO;

  @Autowired
  VaultDAO vaultDAO;

  @Test
  void testWriteThenRead() {
    Retrieve retrieve1 = getRetrieve1();

    Retrieve retrieve2 = getRetrieve2();

    dao.save(retrieve1);
    assertNotNull(retrieve1.getID());
    assertEquals(1, count());

    dao.save(retrieve2);
    assertNotNull(retrieve2.getID());
    assertEquals(2, count());

    Retrieve foundById1 = dao.findById(retrieve1.getID()).get();
    assertEquals(retrieve1.getID(), foundById1.getID());

    Retrieve foundById2 = dao.findById(retrieve2.getID()).get();
    assertEquals(retrieve2.getID(), foundById2.getID());
  }

  @Test
  void testList() {
    Retrieve retrieve1 = getRetrieve1();

    Retrieve retrieve2 = getRetrieve2();

    dao.save(retrieve1);
    assertNotNull(retrieve1.getID());
    assertEquals(1, count());

    dao.save(retrieve2);
    assertNotNull(retrieve2.getID());
    assertEquals(2, count());

    List<Retrieve> items = dao.list();
    assertEquals(2, items.size());
    assertEquals(1, items.stream().filter(dr -> dr.getID().equals(retrieve1.getID())).count());
    assertEquals(1, items.stream().filter(dr -> dr.getID().equals(retrieve2.getID())).count());
  }

  @Test
  void testUpdate() {
    Retrieve retrieve1 = getRetrieve1();

    dao.save(retrieve1);

    retrieve1.setNote("updated-note");

    dao.update(retrieve1);

    Retrieve found = dao.findById(retrieve1.getID()).get();
    assertEquals(retrieve1.getNote(), found.getNote());
  }

  @Test
  void testInProgress() {
    Retrieve retrieve1 = getRetrieve1();
    Retrieve retrieve2 = getRetrieve2();
    Retrieve retrieve3 = getRetrieve3();
    Retrieve retrieve4 = getRetrieve4();

    dao.save(retrieve1);
    dao.save(retrieve2);
    dao.save(retrieve3);
    dao.save(retrieve4);

    List<Retrieve> items = dao.list();
    assertEquals(4, items.size());

    List<Retrieve> inProg = dao.inProgress();
    checkOrderOfRetrives(inProg, retrieve4, retrieve2);
  }

  void checkOrderOfRetrives(Collection<Retrieve> actual, Retrieve... expected){
    assertEquals(
        Arrays.stream(expected).map(Retrieve::getNote).collect(Collectors.toList()),
        actual.stream().map(Retrieve::getNote).collect(Collectors.toList()));
  }


  @BeforeEach
  void setup() {
    assertEquals(0, count());
  }

  @AfterEach
  void cleanup() {
    template.execute("delete from `Users`");
    template.execute("delete from `Retrieves`");
    template.execute("delete from `Deposits`");
    template.execute("delete from `Vaults`");
    template.execute("delete from `Groups`");
    assertEquals(0, count());
  }

  private Retrieve getRetrieve1() {
    Retrieve result = new Retrieve();
    result.setHasExternalRecipients(false);
    result.setNote("note-1");
    result.setStatus(Status.NOT_STARTED);
    result.setTimestamp(TestUtils.TWO_YEARS_AGO);
     return result;
  }

  private Retrieve getRetrieve2() {
    Retrieve result = new Retrieve();
    result.setHasExternalRecipients(false);
    result.setNote("note-2");
    result.setStatus(Status.IN_PROGRESS);
    result.setTimestamp(TestUtils.TWO_YEARS_AGO);
    return result;
  }

  private Retrieve getRetrieve3() {
    Retrieve result = new Retrieve();
    result.setHasExternalRecipients(false);
    result.setNote("note-3");
    result.setStatus(Status.COMPLETE);
    result.setTimestamp(TestUtils.NOW);
    return result;
  }

  private Retrieve getRetrieve4() {
    Retrieve result = new Retrieve();
    result.setHasExternalRecipients(false);
    result.setNote("note-4");
    result.setStatus(Status.IN_PROGRESS);
    result.setTimestamp(TestUtils.THREE_YEARS_AGO);
    return result;
  }

  long count() {
    return dao.count();
  }

  @Test
  void testCountByUser() {

    String schoolId = "lfcs-id";

    createTestUser("denied", schoolId);
    createTestUser("allowed", schoolId, Permission.CAN_VIEW_RETRIEVES);

    Group group = new Group();
    group.setID(schoolId);
    group.setName("LFCS");
    group.setEnabled(true);
    groupDAO.save(group);

    Vault vault = new Vault();
    vault.setName("vault-1");
    vault.setGroup(group);
    vault.setContact("James Bond");
    vault.setReviewDate(TestUtils.NOW);
    vaultDAO.save(vault);

    Deposit deposit = new Deposit();
    deposit.setName("deposit-1");
    deposit.setVault(vault);
    deposit.setHasPersonalData(false);
    depositDAO.save(deposit);

    Retrieve ret1 = getRetrieve1();
    ret1.setDeposit(deposit);
    Retrieve ret2 = getRetrieve2();
    ret2.setDeposit(deposit);
    Retrieve ret3 = getRetrieve3();
    ret3.setDeposit(deposit);

    dao.save(ret1);
    dao.save(ret2);
    dao.save(ret3);

    assertEquals(3, count());

    assertEquals(0, dao.count("denied"));
    assertEquals(3, dao.count("allowed"));
  }
  @Test
  void testListByUser() {

    String schoolId = "lfcs-id";

    createTestUser("denied", schoolId);
    createTestUser("allowed", schoolId, Permission.CAN_VIEW_RETRIEVES);

    Group group = new Group();
    group.setID(schoolId);
    group.setName("LFCS");
    group.setEnabled(true);
    groupDAO.save(group);

    Vault vault = new Vault();
    vault.setName("vault-1");
    vault.setGroup(group);
    vault.setContact("James Bond");
    vault.setReviewDate(TestUtils.NOW);
    vaultDAO.save(vault);

    Deposit deposit = new Deposit();
    deposit.setName("deposit-1");
    deposit.setVault(vault);
    deposit.setHasPersonalData(false);
    depositDAO.save(deposit);

    Retrieve ret1 = getRetrieve1();
    ret1.setDeposit(deposit);
    ret1.setTimestamp(TestUtils.NOW);
    Retrieve ret2 = getRetrieve2();
    ret2.setDeposit(deposit);
    ret2.setTimestamp(TestUtils.TWO_YEARS_AGO);
    Retrieve ret3 = getRetrieve3();
    ret3.setDeposit(deposit);
    ret3.setTimestamp(TestUtils.ONE_YEAR_AGO);

    dao.save(ret1);
    dao.save(ret2);
    dao.save(ret3);

    assertEquals(3, count());

    assertEquals(0, dao.list("denied").size());

    //the returned Retrieves should be sorted by ascending timestamp
    assertEquals(
        Arrays.asList(ret2.getID(), ret3.getID(), ret1.getID())
        , dao.list("allowed").stream().map(Retrieve::getID).collect(Collectors.toList()));
  }

  @Test
  void testInProgressCountByUser(){
    String schoolId = "lfcs-id";

    createTestUser("denied", schoolId);
    createTestUser("allowed", schoolId, Permission.CAN_VIEW_IN_PROGRESS);

    Group group = new Group();
    group.setID(schoolId);
    group.setName("LFCS");
    group.setEnabled(true);
    groupDAO.save(group);

    Vault vault = new Vault();
    vault.setName("vault-1");
    vault.setGroup(group);
    vault.setContact("James Bond");
    vault.setReviewDate(TestUtils.NOW);
    vaultDAO.save(vault);

    Deposit deposit = new Deposit();
    deposit.setName("deposit-1");
    deposit.setVault(vault);
    deposit.setHasPersonalData(false);
    depositDAO.save(deposit);

    Retrieve ret1 = getRetrieve1();
    ret1.setDeposit(deposit);
    ret1.setStatus(Status.IN_PROGRESS);

    Retrieve ret2 = getRetrieve2();
    ret2.setDeposit(deposit);
    ret2.setStatus(Status.NOT_STARTED);

    Retrieve ret3 = getRetrieve3();
    ret3.setDeposit(deposit);
    ret3.setStatus(Status.COMPLETE);

    dao.save(ret1);
    dao.save(ret2);
    dao.save(ret3);

    assertEquals(3, count());

    assertEquals(0, dao.list("denied").size());

    // can't be COMPLETE or NOT_STARTED
    assertEquals(1
        , dao.inProgressCount("allowed"));
  }
  @Test
  void testQueueCount(){
    String schoolId = "lfcs-id";

    createTestUser("denied", schoolId);
    createTestUser("allowed", schoolId, Permission.CAN_VIEW_QUEUES);

    Group group = new Group();
    group.setID(schoolId);
    group.setName("LFCS");
    group.setEnabled(true);
    groupDAO.save(group);

    Vault vault = new Vault();
    vault.setName("vault-1");
    vault.setGroup(group);
    vault.setContact("James Bond");
    vault.setReviewDate(TestUtils.NOW);
    vaultDAO.save(vault);

    Deposit deposit = new Deposit();
    deposit.setName("deposit-1");
    deposit.setVault(vault);
    deposit.setHasPersonalData(false);
    depositDAO.save(deposit);

    Retrieve ret1 = getRetrieve1();
    ret1.setDeposit(deposit);
    ret1.setStatus(Status.IN_PROGRESS);

    Retrieve ret2 = getRetrieve2();
    ret2.setDeposit(deposit);
    ret2.setStatus(Status.NOT_STARTED);

    Retrieve ret3 = getRetrieve3();
    ret3.setDeposit(deposit);
    ret3.setStatus(Status.COMPLETE);

    Retrieve ret4 = getRetrieve4();
    ret4.setDeposit(deposit);
    ret4.setStatus(Status.NOT_STARTED);

    dao.save(ret1);
    dao.save(ret2);
    dao.save(ret3);
    dao.save(ret4);

    assertEquals(4, count());

    assertEquals(0, dao.queueCount("denied"));

    //we want to count those queued - or NOT_STARTED
    assertEquals(2
        , dao.queueCount("allowed"));
  }

  @Test
  void testInProgressCount() {
    String schoolId = "lfcs-id";

    createTestUser("denied", schoolId);
    createTestUser("allowed", schoolId, Permission.CAN_VIEW_IN_PROGRESS);

    Group group = new Group();
    group.setID(schoolId);
    group.setName("LFCS");
    group.setEnabled(true);
    groupDAO.save(group);

    Vault vault = new Vault();
    vault.setName("vault-1");
    vault.setGroup(group);
    vault.setContact("James Bond");
    vault.setReviewDate(TestUtils.NOW);
    vaultDAO.save(vault);

    Deposit deposit = new Deposit();
    deposit.setName("deposit-1");
    deposit.setVault(vault);
    deposit.setHasPersonalData(false);
    depositDAO.save(deposit);

    Retrieve ret1 = getRetrieve1();
    ret1.setDeposit(deposit);
    ret1.setStatus(Status.IN_PROGRESS);

    Retrieve ret2 = getRetrieve2();
    ret2.setDeposit(deposit);
    ret2.setStatus(Status.NOT_STARTED);

    Retrieve ret3 = getRetrieve3();
    ret3.setDeposit(deposit);
    ret3.setStatus(Status.COMPLETE);

    Retrieve ret4 = getRetrieve4();
    ret4.setDeposit(deposit);
    ret4.setStatus(Status.NOT_STARTED);

    dao.save(ret1);
    dao.save(ret2);
    dao.save(ret3);
    dao.save(ret4);

    assertEquals(4, count());

    assertEquals(0, dao.inProgressCount("denied"));

    //we want to count those 'IN PROGRESS'
    assertEquals(1, dao.inProgressCount("allowed"));
  }
}
