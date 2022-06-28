package org.datavaultplatform.common.model.dao;

import static org.datavaultplatform.broker.test.TestUtils.NOW;
import static org.datavaultplatform.broker.test.TestUtils.ONE_WEEK_AGO;
import static org.datavaultplatform.broker.test.TestUtils.TWO_WEEKS_AGO;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.datavaultplatform.broker.app.DataVaultBrokerApp;
import org.datavaultplatform.broker.test.AddTestProperties;
import org.datavaultplatform.broker.test.BaseDatabaseTest;
import org.datavaultplatform.broker.test.TestUtils;
import org.datavaultplatform.common.model.Group;
import org.datavaultplatform.common.model.PendingVault;
import org.datavaultplatform.common.model.Permission;
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
public class PendingVaultDAOIT extends BaseDatabaseTest {

  @Autowired
  PendingVaultDAO dao;

  @Autowired
  GroupDAO groupDAO;

  @Autowired
  VaultDAO vaultDAO;

  @Autowired
  JdbcTemplate template;

  @Test
  void testWriteThenRead() {
    PendingVault pendingVault1 = getPendingVault1();

    PendingVault pendingVault2 = getPendingVault2();

    dao.save(pendingVault1);
    assertNotNull(pendingVault1.getId());
    assertEquals(1, count());

    dao.save(pendingVault2);
    assertNotNull(pendingVault2.getId());
    assertEquals(2, count());

    PendingVault foundById1 = dao.findById(pendingVault1.getId()).get();
    assertEquals(pendingVault1.getId(), foundById1.getId());

    PendingVault foundById2 = dao.findById(pendingVault2.getId()).get();
    assertEquals(pendingVault2.getId(), foundById2.getId());
  }

  @Test
  void testListIsSortedByAscendingCreationTime() {
    PendingVault pendingVault1 = getPendingVault1();

    PendingVault pendingVault2 = getPendingVault2();

    PendingVault pendingVault3 = getPendingVault3();

    dao.save(pendingVault1);
    assertNotNull(pendingVault1.getId());
    assertEquals(1, count());

    dao.save(pendingVault2);
    assertNotNull(pendingVault2.getId());
    assertEquals(2, count());

    dao.save(pendingVault3);
    assertNotNull(pendingVault3.getId());
    assertEquals(3, count());

    List<PendingVault> items = dao.list();
    assertEquals(3, items.size());
    assertEquals(1, items.stream().filter(dr -> dr.getId().equals(pendingVault1.getId())).count());
    assertEquals(1, items.stream().filter(dr -> dr.getId().equals(pendingVault2.getId())).count());
    assertEquals(1, items.stream().filter(dr -> dr.getId().equals(pendingVault3.getId())).count());

    // The PendingVaults should be ordered by Ascending CreationTime
    assertEquals(
        Arrays.asList(
            pendingVault3.getId(),
            pendingVault1.getId(),
            pendingVault2.getId()),
        items.stream().map(PendingVault::getId).collect(Collectors.toList()));
  }

  @Test
  void testUpdate() {
    PendingVault pendingVault1 = getPendingVault1();
    assertNull(pendingVault1.getId());
    dao.save(pendingVault1);
    assertNotNull(pendingVault1.getId());

    List<String> ids = template.query("select id from `PendingVaults`", (rs, rowNum) -> rs.getString(1));
    assertEquals(pendingVault1.getId(), ids.get(0));

    assertTrue(dao.findById(pendingVault1.getId()).isPresent());
    assertEquals(1, dao.count());

    pendingVault1.setName("111-updated");

    dao.update(pendingVault1);

    PendingVault found = dao.findById(pendingVault1.getId()).get();
    assertEquals(pendingVault1.getName(), found.getName());
  }

  @BeforeEach
  void setup() {
    assertEquals(0, count());
  }

  @AfterEach
  void cleanup() {
    template.execute("delete from `PendingVaults`");
    assertEquals(0, count());
  }

  private PendingVault getPendingVault1() {
    PendingVault pendingVault = new PendingVault();
    pendingVault.setName("111");
    pendingVault.setContact("contact-1");
    pendingVault.setCreationTime(ONE_WEEK_AGO);
    return pendingVault;
  }

  private PendingVault getPendingVault2() {
    PendingVault pendingVault = new PendingVault();
    pendingVault.setName("222");
    pendingVault.setContact("contact-2");
    pendingVault.setCreationTime(NOW);
    return pendingVault;
  }

  private PendingVault getPendingVault3() {
    PendingVault pendingVault = new PendingVault();
    pendingVault.setName("333");
    pendingVault.setContact("contact-3");
    pendingVault.setCreationTime(TWO_WEEKS_AGO);
    return pendingVault;
  }

  long count() {
    return dao.count();
  }

  @Test
  void testSearchByUser() {
    String schoolId1 = "school-id-1";
    String schoolId2 = "school-id-2";

    Group group1 = new Group();
    group1.setID(schoolId1);
    group1.setName("LFCS-ONE");
    group1.setEnabled(true);
    groupDAO.save(group1);

    Group group2 = new Group();
    group2.setID(schoolId2);
    group2.setName("LFCS-TWO");
    group2.setEnabled(true);
    groupDAO.save(group2);

    PendingVault pendingVault1 = getPendingVault1();
    pendingVault1.setName("pv-name-1");
    pendingVault1.setGroup(group1);
    pendingVault1.setConfirmed(true);
    pendingVault1.setCreationTime(TestUtils.TWO_YEARS_AGO);

    PendingVault pendingVault2 = getPendingVault2();
    pendingVault2.setName("pv-name-12");
    pendingVault2.setGroup(group2);

    PendingVault pendingVault3 = getPendingVault3();
    pendingVault3.setName("pv-name-12");
    pendingVault3.setGroup(group1);
    pendingVault3.setConfirmed(false);
    pendingVault3.setCreationTime(TestUtils.ONE_YEAR_AGO);

    dao.save(pendingVault1);
    dao.save(pendingVault2);
    dao.save(pendingVault3);

    assertEquals(3, count());

    createTestUser("denied1", schoolId1);
    createTestUser("allowed1", schoolId1, Permission.CAN_MANAGE_VAULTS);
    createTestUser("allowed2", schoolId2, Permission.CAN_MANAGE_VAULTS);

    List<PendingVault> results1 = dao.search("denied1", "pv-name-1", "creationTime", "asc",
        null, null, null);
    assertEquals(0, results1.size());

    List<PendingVault> results2 = dao.search("allowed1", "pv-name-1", "creationTime", "asc",
        null, null, null);
    assertEquals(2, results2.size());
    assertTrue(results2.stream().map(PendingVault::getId).collect(Collectors.toList()).contains(pendingVault1.getId()));
    assertTrue(results2.stream().map(PendingVault::getId).collect(Collectors.toList()).contains(pendingVault3.getId()));


    List<PendingVault> results3 = dao.search("allowed2", "pv-name-1", "creationTime", "asc",
        null, null, null);
    assertEquals(1, results3.size());
    assertTrue(results3.stream().map(PendingVault::getId).collect(Collectors.toList()).contains(pendingVault2.getId()));

    List<Object[]> queryRes1 = template.query("select id,confirmed from `PendingVaults`",
        (rs, rowNum) -> new Object[] {
            rs.getString(1),
            rs.getBoolean(2)
        });

    queryRes1.forEach(item -> {
      System.out.printf("item %s", Arrays.toString(item));
    });

    List<PendingVault> results4 = dao.search("allowed1", "pv-name-1", "creationTime", "asc",
        null, null, Boolean.TRUE.toString());
    assertEquals(1, results4.size());
    assertTrue(results4.stream().map(PendingVault::getId).collect(Collectors.toList()).contains(pendingVault1.getId()));

    List<PendingVault> results5 = dao.search("allowed1", "pv-name-1", "creationTime", "asc",
        null, null, Boolean.FALSE.toString());
    assertEquals(1, results5.size());
    assertTrue(results5.stream().map(PendingVault::getId).collect(Collectors.toList()).contains(pendingVault3.getId()));

    List<PendingVault> results6 = dao.search("allowed2", "pv-name-1", "creationTime", "asc",
        null, null, Boolean.TRUE.toString());
    assertEquals(0, results6.size());

    List<PendingVault> results7 = dao.search("allowed2", "pv-name-1", "creationTime", "asc",
        null, null, Boolean.FALSE.toString());
    assertEquals(1, results7.size());
    assertTrue(results7.stream().map(PendingVault::getId).collect(Collectors.toList()).contains(pendingVault2.getId()));

    List<String> results8 = dao.search("allowed1", "pv-name-1", "creationTime", "asc",
        null, null, null).stream().map(PendingVault::getId).collect(Collectors.toList());
    assertEquals(Arrays.asList(pendingVault1.getId(), pendingVault3.getId()), results8);

    List<String> results9 = dao.search("allowed1", "pv-name-1", "creationTime", "desc",
        null, null, null).stream().map(PendingVault::getId).collect(Collectors.toList());
    assertEquals(Arrays.asList(pendingVault3.getId(), pendingVault1.getId()), results9);

    List<String> results10 = dao.search("allowed1", "pv-name-1", "creationTime", "desc",
        "1", "10", null).stream().map(PendingVault::getId).collect(Collectors.toList());
    assertEquals(Arrays.asList(pendingVault1.getId()), results10);

    List<String> results11 = dao.search("allowed1", "pv-name-1", "creationTime", "desc",
        "0", "1", null).stream().map(PendingVault::getId).collect(Collectors.toList());
    assertEquals(Arrays.asList(pendingVault3.getId()), results11);
  }

  @Test
  void testListByUser() {
    String schoolId1 = "school-id-1";
    String schoolId2 = "school-id-2";

    Group group1 = new Group();
    group1.setID(schoolId1);
    group1.setName("LFCS-ONE");
    group1.setEnabled(true);
    groupDAO.save(group1);

    Group group2 = new Group();
    group2.setID(schoolId2);
    group2.setName("LFCS-TWO");
    group2.setEnabled(true);
    groupDAO.save(group2);

    PendingVault pendingVault1 = getPendingVault1();
    pendingVault1.setGroup(group1);
    pendingVault1.setCreationTime(TestUtils.TWO_YEARS_AGO);

    PendingVault pendingVault2 = getPendingVault2();
    pendingVault2.setGroup(group2);

    PendingVault pendingVault3 = getPendingVault3();
    pendingVault3.setGroup(group1);
    pendingVault3.setCreationTime(TestUtils.ONE_YEAR_AGO);

    dao.save(pendingVault1);
    dao.save(pendingVault2);
    dao.save(pendingVault3);

    assertEquals(3, count());

    createTestUser("denied1", schoolId1);
    createTestUser("allowed1", schoolId1, Permission.CAN_MANAGE_VAULTS);
    createTestUser("allowed2", schoolId2, Permission.CAN_MANAGE_VAULTS);

    List<PendingVault> results1 = dao.list("denied1", "creationTime", "asc",
        null, null);
    assertEquals(0, results1.size());

    List<PendingVault> results2 = dao.list("allowed1", "creationTime", "asc",
        null, null);
    assertEquals(2, results2.size());
    assertTrue(results2.stream().map(PendingVault::getId).collect(Collectors.toList()).contains(pendingVault1.getId()));
    assertTrue(results2.stream().map(PendingVault::getId).collect(Collectors.toList()).contains(pendingVault3.getId()));

    List<PendingVault> results3 = dao.list("allowed2", "creationTime", "asc",
        null, null);
    assertEquals(1, results3.size());
    assertTrue(results3.stream().map(PendingVault::getId).collect(Collectors.toList()).contains(pendingVault2.getId()));

    List<PendingVault> results7 = dao.list("denied2", "creationTime", "asc",
        null, null);
    assertEquals(0, results7.size());

    List<String> results8 = dao.list("allowed1", "creationTime", "asc",
        null, null).stream().map(PendingVault::getId).collect(Collectors.toList());
    assertEquals(Arrays.asList(pendingVault1.getId(),pendingVault3.getId()), results8);

    List<String> results9 = dao.list("allowed1", "creationTime", "desc",
        null, null).stream().map(PendingVault::getId).collect(Collectors.toList());
    assertEquals(Arrays.asList(pendingVault3.getId(),pendingVault1.getId()), results9);

    List<String> results10 = dao.list("allowed1", "creationTime", "desc",
        "1", "10").stream().map(PendingVault::getId).collect(Collectors.toList());
    assertEquals(Arrays.asList(pendingVault1.getId()), results10);

    List<String> results11 = dao.list("allowed1", "creationTime", "desc",
        "0", "1").stream().map(PendingVault::getId).collect(Collectors.toList());
    assertEquals(Arrays.asList(pendingVault3.getId()), results11);

    //ordering by userid asc - for coverage really
    Set<String> results12 = dao.list("allowed1", "user", "asc",
        null, null).stream().map(PendingVault::getId).collect(Collectors.toSet());
    assertEquals(new HashSet<>(Arrays.asList(pendingVault3.getId(),pendingVault1.getId())),results12);

    //ordering by userid desc - for coverage really
    Set<String> results13 = dao.list("allowed1", "user", "desc",
        null, null).stream().map(PendingVault::getId).collect(Collectors.toSet());
    assertEquals(new HashSet<>(Arrays.asList(pendingVault3.getId(),pendingVault1.getId())),results13);
  }


  @Test
  void testCountByUser() {
    String schoolId1 = "school-id-1";
    String schoolId2 = "school-id-2";

    Group group1 = new Group();
    group1.setID(schoolId1);
    group1.setName("LFCS-ONE");
    group1.setEnabled(true);
    groupDAO.save(group1);

    Group group2 = new Group();
    group2.setID(schoolId2);
    group2.setName("LFCS-TWO");
    group2.setEnabled(true);
    groupDAO.save(group2);

    PendingVault pendingVault1 = getPendingVault1();
    pendingVault1.setGroup(group1);
    pendingVault1.setCreationTime(TestUtils.TWO_YEARS_AGO);

    PendingVault pendingVault2 = getPendingVault2();
    pendingVault2.setGroup(group2);

    PendingVault pendingVault3 = getPendingVault3();
    pendingVault3.setGroup(group1);
    pendingVault3.setCreationTime(TestUtils.ONE_YEAR_AGO);

    dao.save(pendingVault1);
    dao.save(pendingVault2);
    dao.save(pendingVault3);

    assertEquals(3, count());

    createTestUser("denied1", schoolId1);
    createTestUser("allowed1", schoolId1, Permission.CAN_MANAGE_VAULTS);
    createTestUser("allowed2", schoolId2, Permission.CAN_MANAGE_VAULTS);

    assertEquals(0, dao.count("denied1"));
    assertEquals(2, dao.count("allowed1"));

    assertEquals(0, dao.count("denied2"));
    assertEquals(1, dao.count("allowed2"));
  }

  @Test
  void testTotalNumberOfPendingVaultsNoQuery() {
    String schoolId1 = "school-id-1";
    String schoolId2 = "school-id-2";

    Group group1 = new Group();
    group1.setID(schoolId1);
    group1.setName("LFCS-ONE");
    group1.setEnabled(true);
    groupDAO.save(group1);

    Group group2 = new Group();
    group2.setID(schoolId2);
    group2.setName("LFCS-TWO");
    group2.setEnabled(true);
    groupDAO.save(group2);

    PendingVault pendingVault1 = getPendingVault1();
    pendingVault1.setGroup(group1);
    pendingVault1.setConfirmed(true);

    PendingVault pendingVault2 = getPendingVault2();
    pendingVault2.setGroup(group2);
    pendingVault2.setConfirmed(true);

    PendingVault pendingVault3 = getPendingVault3();
    pendingVault3.setGroup(group1);
    pendingVault3.setConfirmed(false);

    dao.save(pendingVault1);
    dao.save(pendingVault2);
    dao.save(pendingVault3);

    assertEquals(3, count());

    createTestUser("denied1", schoolId1);
    createTestUser("allowed1", schoolId1, Permission.CAN_MANAGE_VAULTS);
    createTestUser("allowed2", schoolId2, Permission.CAN_MANAGE_VAULTS);

    assertEquals(0, dao.getTotalNumberOfPendingVaults("denied1",null));
    assertEquals(2, dao.getTotalNumberOfPendingVaults("allowed1",null));
    assertEquals(1, dao.getTotalNumberOfPendingVaults("allowed1","true"));
    assertEquals(1, dao.getTotalNumberOfPendingVaults("allowed1","false"));

    assertEquals(0, dao.getTotalNumberOfPendingVaults("denied2",null));
    assertEquals(1, dao.getTotalNumberOfPendingVaults("allowed2",null));
    assertEquals(1, dao.getTotalNumberOfPendingVaults("allowed2","true"));
    assertEquals(0, dao.getTotalNumberOfPendingVaults("allowed2","false"));
  }


  @Test
  void testTotalNumberOfPendingVaultsWithQuery() {
    String schoolId1 = "school-id-1";
    String schoolId2 = "school-id-2";

    Group group1 = new Group();
    group1.setID(schoolId1);
    group1.setName("LFCS-ONE");
    group1.setEnabled(true);
    groupDAO.save(group1);

    Group group2 = new Group();
    group2.setID(schoolId2);
    group2.setName("LFCS-TWO");
    group2.setEnabled(true);
    groupDAO.save(group2);

    PendingVault pendingVault1 = getPendingVault1();
    pendingVault1.setGroup(group1);
    pendingVault1.setConfirmed(true);
    pendingVault1.setName("name1");

    PendingVault pendingVault2 = getPendingVault2();
    pendingVault2.setGroup(group2);
    pendingVault2.setConfirmed(true);
    pendingVault2.setName("name12");

    PendingVault pendingVault3 = getPendingVault3();
    pendingVault3.setGroup(group1);
    pendingVault3.setConfirmed(false);
    pendingVault3.setName("name123");

    dao.save(pendingVault1);
    dao.save(pendingVault2);
    dao.save(pendingVault3);

    assertEquals(3, count());

    createTestUser("denied1", schoolId1);
    createTestUser("allowed1", schoolId1, Permission.CAN_MANAGE_VAULTS);
    createTestUser("allowed2", schoolId2, Permission.CAN_MANAGE_VAULTS);

    assertEquals(0, dao.getTotalNumberOfPendingVaults("denied1","",null));
    assertEquals(2, dao.getTotalNumberOfPendingVaults("allowed1","",null));
    assertEquals(2, dao.getTotalNumberOfPendingVaults("allowed1","name1",null));
    assertEquals(1, dao.getTotalNumberOfPendingVaults("allowed1","name12",null));
    assertEquals(1, dao.getTotalNumberOfPendingVaults("allowed1","name123",null));
    assertEquals(1, dao.getTotalNumberOfPendingVaults("allowed1","","true"));
    assertEquals(1, dao.getTotalNumberOfPendingVaults("allowed1","","false"));

    assertEquals(0, dao.getTotalNumberOfPendingVaults("denied2","",null));
    assertEquals(1, dao.getTotalNumberOfPendingVaults("allowed2","",null));
    assertEquals(1, dao.getTotalNumberOfPendingVaults("allowed2","name1",null));
    assertEquals(1, dao.getTotalNumberOfPendingVaults("allowed2","name12",null));
    assertEquals(0, dao.getTotalNumberOfPendingVaults("allowed2","name123",null));
    assertEquals(1, dao.getTotalNumberOfPendingVaults("allowed2","","true"));
    assertEquals(0, dao.getTotalNumberOfPendingVaults("allowed2","","false"));
  }
}
