package org.datavaultplatform.common.model.dao;

import static org.datavaultplatform.broker.test.TestUtils.NOW;
import static org.datavaultplatform.broker.test.TestUtils.ONE_WEEK_AGO;
import static org.datavaultplatform.broker.test.TestUtils.TWO_WEEKS_AGO;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.persistence.criteria.CriteriaBuilder.In;
import lombok.extern.slf4j.Slf4j;
import org.datavaultplatform.broker.app.DataVaultBrokerApp;
import org.datavaultplatform.broker.test.AddTestProperties;
import org.datavaultplatform.broker.test.BaseReuseDatabaseTest;
import org.datavaultplatform.broker.test.TestUtils;
import org.datavaultplatform.common.model.Group;
import org.datavaultplatform.common.model.Permission;
import org.datavaultplatform.common.model.User;
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
public class VaultDAOIT extends BaseReuseDatabaseTest {


  @Autowired
  VaultDAO dao;

  @Autowired
  GroupDAO groupDAO;

  @Test
  void testWriteThenRead() {
    Vault vault1 = getVault1();

    Vault vault2 = getVault2();

    dao.save(vault1);
    assertNotNull(vault1.getID());
    assertEquals(1, count());

    dao.save(vault2);
    assertNotNull(vault2.getID());
    assertEquals(2, count());

    Vault foundById1 = dao.findById(vault1.getID()).get();
    assertEquals(vault1.getID(), foundById1.getID());

    Vault foundById2 = dao.findById(vault2.getID()).get();
    assertEquals(vault2.getID(), foundById2.getID());
  }

  @Test
  void testListIsSortedByCreationTimeAscending() {
    Vault vault1 = getVault1();

    Vault vault2 = getVault2();

    Vault vault3 = getVault3();

    dao.save(vault1);
    assertEquals(1, count());

    dao.save(vault2);
    assertEquals(2, count());

    dao.save(vault3);
    assertEquals(3, count());

    List<Vault> items = dao.list();
    assertEquals(3, items.size());
    assertEquals(1, items.stream().filter(dr -> dr.getID().equals(vault1.getID())).count());
    assertEquals(1, items.stream().filter(dr -> dr.getID().equals(vault2.getID())).count());
    assertEquals(1, items.stream().filter(dr -> dr.getID().equals(vault3.getID())).count());

    // The Vaults should be ordered by Ascending Creation Time
    assertEquals(
        Arrays.asList(
            vault3.getID(),
            vault1.getID(),
            vault2.getID()),
        items.stream().map(Vault::getID).collect(Collectors.toList()));
  }


  @Test
  void testUpdate() {
    Vault vault = getVault1();

    dao.save(vault);

    vault.setName("updated-name");

    dao.update(vault);

    Vault found = dao.findById(vault.getID()).get();
    assertEquals(vault.getName(), found.getName());
  }

  @Test
  void testVaultSnapshotBLOB(){

    Vault vault = getVaultWithSnapshot();
    dao.save(vault);

    Vault found = dao.findById(vault.getID()).get();

    assertEquals(vault.getSnapshot(), found.getSnapshot());
  }

  @BeforeEach
  void setup() {
    assertEquals(0, count());
  }

  @AfterEach
  void cleanup() {
    template.execute("delete from `Vaults`");
    assertEquals(0, count());
  }

  @Test
  void testGetTotalNumberOfVaultsForUser() {

    Vault v1 = getVault1();
    Vault v2 = getVault2();
    Vault v3 = getVault3();

    String schoolId = "lfcs-id";

    Group group = new Group();
    group.setID(schoolId);
    group.setName("LFCS");
    group.setEnabled(true);
    groupDAO.save(group);

    v1.setGroup(group);
    v2.setGroup(group);
    v3.setGroup(group);

    dao.save(v1);
    dao.save(v2);
    dao.save(v3);

    assertEquals(3, dao.count());
    createTestUser("denied1", schoolId);
    assertEquals(0, dao.getTotalNumberOfVaults("denied1"));

    createTestUser("allowed", schoolId, Permission.CAN_MANAGE_VAULTS );
    assertEquals(3, dao.getTotalNumberOfVaults("allowed"));

    createTestUser("denied2", schoolId, Permission.CAN_MANAGE_DEPOSITS );
    assertEquals(0, dao.getTotalNumberOfVaults("denied2"));
  }

  @Test
  void testGetTotalNumberOfVaultsWithQueryForUser() {

    Vault v1 = getVault1();
    Vault v2 = getVault2();
    Vault v3 = getVault3();

    String schoolId = "lfcs-id";

    Group group = new Group();
    group.setID(schoolId);
    group.setName("LFCS");
    group.setEnabled(true);
    groupDAO.save(group);

    v1.setGroup(group);
    v2.setGroup(group);
    v3.setGroup(group);


    v1.setDescription("desc-for-1");
    v2.setDescription("desc-for-12");
    v3.setDescription("desc-for-123");

    v1.setName("name-for-1");
    v2.setName("name-for-12");
    v3.setName("name-for-123");

    dao.save(v1);
    dao.save(v2);
    dao.save(v3);

    assertEquals(3, dao.count());
    createTestUser("denied1", schoolId);
    assertEquals(0, dao.getTotalNumberOfVaults("denied1", null));

    createTestUser("allowed", schoolId, Permission.CAN_MANAGE_VAULTS );
    assertEquals(3, dao.getTotalNumberOfVaults("allowed", null));

    createTestUser("denied2", schoolId, Permission.CAN_MANAGE_DEPOSITS );
    assertEquals(0, dao.getTotalNumberOfVaults("denied2", null));

    // check that query uses description field
    assertEquals(3, dao.getTotalNumberOfVaults("allowed", "desc-for-1"));
    assertEquals(2, dao.getTotalNumberOfVaults("allowed", "desc-for-12"));
    assertEquals(1, dao.getTotalNumberOfVaults("allowed", "desc-for-123"));
    assertEquals(0, dao.getTotalNumberOfVaults("allowed", "desc-for-1234"));

    // check that query uses name field
    assertEquals(3, dao.getTotalNumberOfVaults("allowed", "name-for-1"));
    assertEquals(2, dao.getTotalNumberOfVaults("allowed", "name-for-12"));
    assertEquals(1, dao.getTotalNumberOfVaults("allowed", "name-for-123"));
    assertEquals(0, dao.getTotalNumberOfVaults("allowed", "name-for-1234"));

    // check that query uses id field
    assertEquals(1, dao.getTotalNumberOfVaults("allowed", v1.getID()));
    assertEquals(1, dao.getTotalNumberOfVaults("allowed", v2.getID()));
    assertEquals(1, dao.getTotalNumberOfVaults("allowed", v3.getID()));
  }

  @Test
  void testListForUser() {

    Vault v1 = getVault1();
    Vault v2 = getVault2();
    Vault v3 = getVault3();

    String schoolId = "lfcs-id";

    Group group = new Group();
    group.setID(schoolId);
    group.setName("LFCS");
    group.setEnabled(true);
    groupDAO.save(group);

    v1.setGroup(group);
    v2.setGroup(group);
    v3.setGroup(group);

    v1.setDescription("desc-for-1");
    v2.setDescription("desc-for-12");
    v3.setDescription("desc-for-123");

    v1.setName("name-Z");
    v2.setName("name-Y");
    v3.setName("name-X");

    dao.save(v1);
    dao.save(v2);
    dao.save(v3);

    assertEquals(3, dao.count());
    createTestUser("denied1", schoolId);
    //    List<Vault> list(String userId, String sort, String order, String offset, String maxResult);
    assertTrue(dao.list("denied1", "description", "asc", null,null).isEmpty());

    createTestUser("denied2", schoolId, Permission.CAN_MANAGE_DEPOSITS);
    assertTrue(dao.list("denied2", "description", "asc", null,null).isEmpty());

    createTestUser("allowed", schoolId, Permission.CAN_MANAGE_VAULTS);
    List<Vault> vaultsByNameAsc = dao.list("allowed", "name", "asc", null,null);
    assertEquals(3, vaultsByNameAsc.size());
    assertEquals(v3, vaultsByNameAsc.get(0));
    assertEquals(v2, vaultsByNameAsc.get(1));
    assertEquals(v1, vaultsByNameAsc.get(2));

    List<Vault> vaultsByNameDesc = dao.list("allowed", "name", "desc", null,null);
    assertEquals(3, vaultsByNameDesc.size());
    assertEquals(v1, vaultsByNameDesc.get(0));
    assertEquals(v2, vaultsByNameDesc.get(1));
    assertEquals(v3, vaultsByNameDesc.get(2));


    List<Vault> vaultsByDescriptionAsc = dao.list("allowed", "description", "asc", null,null);
    assertEquals(3, vaultsByDescriptionAsc.size());
    assertEquals(v1, vaultsByDescriptionAsc.get(0));
    assertEquals(v2, vaultsByDescriptionAsc.get(1));
    assertEquals(v3, vaultsByDescriptionAsc.get(2));

    List<Vault> vaultsByDescriptionDesc = dao.list("allowed", "description", "desc", null,null);
    assertEquals(3, vaultsByDescriptionDesc.size());
    assertEquals(v3, vaultsByDescriptionDesc.get(0));
    assertEquals(v2, vaultsByDescriptionDesc.get(1));
    assertEquals(v1, vaultsByDescriptionDesc.get(2));


    //from 0..
    List<Vault> from0 = dao.list("allowed", "description", "desc", "0", String.valueOf(Integer.MAX_VALUE));
    assertEquals(3, from0.size());

    //from 1..
    List<Vault> from1 = dao.list("allowed", "description", "desc", "1", String.valueOf(Integer.MAX_VALUE));
    assertEquals(2, from1.size());

    //from 2..
    List<Vault> from2 = dao.list("allowed", "description", "desc", "2", String.valueOf(Integer.MAX_VALUE));
    assertEquals(1, from2.size());

    //from 3..
    List<Vault> from3 = dao.list("allowed", "description", "desc", "3", String.valueOf(Integer.MAX_VALUE));
    assertEquals(0, from3.size());

    //Max result of 0 is ignored - this is intended
    List<Vault> from0to0 = dao.list("allowed", "description", "desc", "0", "0");
    assertEquals(3, from0to0.size());

    //from 0..1
    List<Vault> from0to1 = dao.list("allowed", "description", "desc", "0", "1");
    assertEquals(1, from0to1.size());

    //from 0..2
    List<Vault> from0to2 = dao.list("allowed", "description", "desc", "0", "2");
    assertEquals(2, from0to2.size());

    //from 0..3
    List<Vault> from0to3 = dao.list("allowed", "description", "desc", "0", "3");
    assertEquals(3, from0to3.size());
  }

  @Test
  void testSearchForUser() {

    Vault v1 = getVault1();
    Vault v2 = getVault2();
    Vault v3 = getVault3();

    String schoolId = "lfcs-id";

    Group group = new Group();
    group.setID(schoolId);
    group.setName("LFCS");
    group.setEnabled(true);
    groupDAO.save(group);

    v1.setGroup(group);
    v2.setGroup(group);
    v3.setGroup(group);

    v1.setDescription("desc-for-1");
    v2.setDescription("desc-for-12");
    v3.setDescription("desc-for-123");

    v1.setName("name-Z");
    v2.setName("name-Y");
    v3.setName("name-X");

    dao.save(v1);
    dao.save(v2);
    dao.save(v3);

    assertEquals(3, dao.count());
    createTestUser("denied1", schoolId);
    //    List<Vault> list(String userId, String sort, String order, String offset, String maxResult);
    assertTrue(dao.search("denied1", null, "description", "asc", null,null).isEmpty());

    createTestUser("denied2", schoolId, Permission.CAN_MANAGE_DEPOSITS);
    assertTrue(dao.search("denied2", null, "description", "asc", null,null).isEmpty());

    createTestUser("allowed", schoolId, Permission.CAN_MANAGE_VAULTS);
    List<Vault> vaultsByNameAsc = dao.search("allowed", null, "name", "asc", null,null);

    assertEquals(3, vaultsByNameAsc.size());
    assertEquals(v3, vaultsByNameAsc.get(0));
    assertEquals(v2, vaultsByNameAsc.get(1));
    assertEquals(v1, vaultsByNameAsc.get(2));

    List<Vault> vaultsByNameDesc = dao.search("allowed", null,"name", "desc", null,null);
    assertEquals(3, vaultsByNameDesc.size());
    assertEquals(v1, vaultsByNameDesc.get(0));
    assertEquals(v2, vaultsByNameDesc.get(1));
    assertEquals(v3, vaultsByNameDesc.get(2));


    List<Vault> vaultsByDescriptionAsc = dao.search("allowed", null, "description", "asc", null,null);
    assertEquals(3, vaultsByDescriptionAsc.size());
    assertEquals(v1, vaultsByDescriptionAsc.get(0));
    assertEquals(v2, vaultsByDescriptionAsc.get(1));
    assertEquals(v3, vaultsByDescriptionAsc.get(2));

    List<Vault> vaultsByDescriptionDesc = dao.search("allowed", null, "description", "desc", null,null);
    assertEquals(3, vaultsByDescriptionDesc.size());
    assertEquals(v3, vaultsByDescriptionDesc.get(0));
    assertEquals(v2, vaultsByDescriptionDesc.get(1));
    assertEquals(v1, vaultsByDescriptionDesc.get(2));


    //from 0..
    List<Vault> from0 = dao.search("allowed", null,"description", "desc", "0", String.valueOf(Integer.MAX_VALUE));
    assertEquals(3, from0.size());

    //from 1..
    List<Vault> from1 = dao.search("allowed", null, "description", "desc", "1", String.valueOf(Integer.MAX_VALUE));
    assertEquals(2, from1.size());

    //from 2..
    List<Vault> from2 = dao.search("allowed", null, "description", "desc", "2", String.valueOf(Integer.MAX_VALUE));
    assertEquals(1, from2.size());

    //from 3..
    List<Vault> from3 = dao.search("allowed", null, "description", "desc", "3", String.valueOf(Integer.MAX_VALUE));
    assertEquals(0, from3.size());

    //Max result of 0 is ignored - this is intended
    List<Vault> from0to0 = dao.search("allowed", null, "description", "desc", "0", "0");
    assertEquals(3, from0to0.size());

    //from 0..1
    List<Vault> from0to1 = dao.search("allowed", null, "description", "desc", "0", "1");
    assertEquals(1, from0to1.size());

    //from 0..2
    List<Vault> from0to2 = dao.search("allowed", null, "description", "desc", "0", "2");
    assertEquals(2, from0to2.size());

    //from 0..3
    List<Vault> from0to3 = dao.search("allowed", null, "description", "desc", "0", "3");
    assertEquals(3, from0to3.size());


    List<Vault> from0to3likeName1 = dao.search("allowed", "name-X", "description", "desc", "0", "3");
    assertEquals(1, from0to3likeName1.size());

    List<Vault> from0to3likeName2 = dao.search("allowed", "name-", "description", "desc", "0", "3");
    assertEquals(3, from0to3likeName2.size());


    List<Vault> from0to3likeDesc = dao.search("allowed", "desc-for-12", "description", "desc", "0", "3");
    assertEquals(2, from0to3likeDesc.size());
  }

  @Test
  void testGetCountForUser() {
    /*
    TODO - getTotalNumberOfVaults is almost the same as count
    count does not include 'DISTINCT_ROOT_ENTITY' - we should check this in the test
     */

    Vault v1 = getVault1();
    Vault v2 = getVault2();
    Vault v3 = getVault3();

    String schoolId = "lfcs-id";

    Group group = new Group();
    group.setID(schoolId);
    group.setName("LFCS");
    group.setEnabled(true);
    groupDAO.save(group);

    v1.setGroup(group);
    v2.setGroup(group);
    v3.setGroup(group);

    dao.save(v1);
    dao.save(v2);
    dao.save(v3);

    assertEquals(3, dao.count());
    createTestUser("denied1", schoolId);
    assertEquals(0, dao.count("denied1"));

    createTestUser("allowed", schoolId, Permission.CAN_MANAGE_VAULTS );
    assertEquals(3, dao.count("allowed"));

    createTestUser("denied2", schoolId, Permission.CAN_MANAGE_DEPOSITS );
    assertEquals(0, dao.count("denied2"));
  }

  @Test
  void getGetRetentionPolicyCountFor() {

    Vault v1 = getVault1();
    Vault v2 = getVault2();
    Vault v3 = getVault3();

    v1.setRetentionPolicyStatus(222);
    v2.setRetentionPolicyStatus(111);
    v3.setRetentionPolicyStatus(222);

    dao.save(v1);
    dao.save(v2);
    dao.save(v3);

    assertEquals(3, dao.count());
    assertEquals(0, dao.getRetentionPolicyCount(Integer.MIN_VALUE));
    assertEquals(0, dao.getRetentionPolicyCount(Integer.MAX_VALUE));
    assertEquals(0, dao.getRetentionPolicyCount(-1));
    assertEquals(0, dao.getRetentionPolicyCount(0));
    assertEquals(1, dao.getRetentionPolicyCount(111));
    assertEquals(2, dao.getRetentionPolicyCount(222));
  }

  @Test
  void testGetAllProjectSize() {

    Vault v1 = getVault1();
    Vault v2 = getVault2();
    Vault v3 = getVault3();

    v1.setProjectId("222");
    v1.setSize(1000L);

    v2.setProjectId("111");
    v2.setSize(1500L);

    v3.setProjectId("222");
    v3.setSize(2000L);

    dao.save(v1);
    dao.save(v2);
    dao.save(v3);

    assertEquals(3, dao.count());

    List<Object[]> result = dao.getAllProjectsSize();
    assertEquals(2, result.size());

    Map<String, Long> resultByProjectId = result.stream()
        .collect(Collectors.toMap(
            (Object[] arr) -> (String) arr[0],
            (Object[] arr) -> (Long) arr[1]));

    assertEquals(1500L, resultByProjectId.get("111"));
    assertEquals(3000L, resultByProjectId.get("222"));

  }

  static  Vault getVault1() {
    Vault result = new Vault();
    result.setContact("contact-1");
    result.setName("vault-1");
    result.setReviewDate(NOW);
    result.setCreationTime(ONE_WEEK_AGO);
    return result;
  }

  static Vault getVault2() {
    Vault result = new Vault();
    result.setContact("contact-2");
    result.setName("vault-2");
    result.setReviewDate(NOW);
    result.setCreationTime(NOW);
    return result;
  }

  static Vault getVault3() {
    Vault result = new Vault();
    result.setContact("contact-2");
    result.setName("vault-2");
    result.setReviewDate(NOW);
    result.setCreationTime(TWO_WEEKS_AGO);
    return result;
  }

  static Vault getVaultWithSnapshot() {
    Vault result = new Vault();
    result.setContact("contact-3");
    result.setName("vault-3");
    result.setReviewDate(NOW);
    result.setCreationTime(NOW);
    result.setSnapshot(String.join(",", TestUtils.getRandomList()));
    return result;
  }

  long count() {
     return dao.count();
  }
}
