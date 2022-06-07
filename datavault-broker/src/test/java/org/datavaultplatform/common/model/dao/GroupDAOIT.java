package org.datavaultplatform.common.model.dao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.datavaultplatform.broker.app.DataVaultBrokerApp;
import org.datavaultplatform.broker.test.AddTestProperties;
import org.datavaultplatform.broker.test.BaseReuseDatabaseTest;
import org.datavaultplatform.common.model.Group;
import org.datavaultplatform.common.model.Permission;
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
public class GroupDAOIT extends BaseReuseDatabaseTest {

  @Autowired
  GroupDAO dao;

  @Test
  void testWriteThenRead() {
    Group group1 = getGroup1();

    Group group2 = getGroup2();

    dao.save(group1);
    assertNotNull(group1.getID());
    assertEquals(1, count());

    dao.save(group2);
    assertNotNull(group2.getID());
    assertEquals(2, count());

    Group foundById1 = dao.findById(group1.getID()).get();
    assertEquals(group1.getID(), foundById1.getID());

    Group foundById2 = dao.findById(group2.getID()).get();
    assertEquals(group2.getID(), foundById2.getID());
  }

  @Test
  void testListSortedByNameAscending() {
    Group group1 = getGroup1();

    Group group2 = getGroup2();

    Group group3 = getGroup3();

    dao.save(group1);
    assertNotNull(group1.getID());
    assertEquals(1, count());

    dao.save(group2);
    assertNotNull(group2.getID());
    assertEquals(2, count());

    dao.save(group3);
    assertNotNull(group3.getID());
    assertEquals(3, count());

    List<Group> items = dao.list();
    assertEquals(3, items.size());
    assertEquals(1, items.stream().filter(dr -> dr.getID().equals(group1.getID())).count());
    assertEquals(1, items.stream().filter(dr -> dr.getID().equals(group2.getID())).count());
    assertEquals(1, items.stream().filter(dr -> dr.getID().equals(group3.getID())).count());

    // The Groups should be ordered by Ascending Name
    assertEquals(
        Arrays.asList(
            group3.getID(),
            group1.getID(),
            group2.getID()),
        items.stream().map(Group::getID).collect(Collectors.toList()));
  }


  @Test
  void testUpdate() {
    Group group1 = getGroup1();

    dao.save(group1);

    group1.setName("111-updated");

    dao.update(group1);

    Group found = dao.findById(group1.getID()).get();
    assertEquals(group1.getName(), found.getName());
  }

  @BeforeEach
  void setup() {
    assertEquals(0, count());
  }

  @AfterEach
  void cleanup() {
    template.execute("delete from `Groups`");
    assertEquals(0, count());
  }

  private Group getGroup1() {
    Group group = new Group();
    group.setID("ID-1");
    group.setEnabled(true);
    group.setName("DDD");
    return group;
  }

  private Group getGroup2() {
    Group group = new Group();
    group.setID("ID-2");
    group.setEnabled(false);
    group.setName("ZZZ");
    return group;
  }

  private Group getGroup3() {
    Group group = new Group();
    group.setID("ID-3");
    group.setEnabled(false);
    group.setName("AAA");
    return group;
  }

  long count() {
    return dao.count();
  }


  @Test
  void testCountByUser() {
    String schoolId1 = "lfcs-id-1";
    String schoolId2 = "lfcs-id-2";
    String schoolId3 = "lfcs-id-3";

    Group group1 = getGroup1();
    group1.setID(schoolId1);

    Group group2 = getGroup2();
    group2.setID(schoolId2);

    Group group3 = getGroup3();
    group3.setID(schoolId3);

    dao.save(group1);
    dao.save(group2);
    dao.save(group3);
    assertEquals(3, dao.count());

    createTestUser("denied1", schoolId1);
    createTestUser("allowed1", schoolId1, Permission.CAN_VIEW_SCHOOL_ROLE_ASSIGNMENTS);
    assertEquals(0, dao.count("denied1"));
    assertEquals(1, dao.count("allowed1"));

    createTestUser("denied2", schoolId2);
    createTestUser("allowed2", schoolId2, Permission.CAN_VIEW_SCHOOL_ROLE_ASSIGNMENTS);
    assertEquals(0, dao.count("denied2"));
    assertEquals(1, dao.count("allowed2"));
  }


  @Test
  void testListByUser(){
    String schoolId1 = "lfcs-id-1";
    String schoolId2 = "lfcs-id-2";
    String schoolId3 = "lfcs-id-3";

    Group group1 = getGroup1();
    group1.setID(schoolId1);

    Group group2 = getGroup2();
    group2.setID(schoolId2);

    Group group3 = getGroup3();
    group3.setID(schoolId3);

    dao.save(group1);
    dao.save(group2);
    dao.save(group3);
    assertEquals(3, dao.count());

    createTestUser("denied1", schoolId1);
    createTestUser("allowed1", schoolId1, Permission.CAN_VIEW_SCHOOL_ROLE_ASSIGNMENTS);
    assertEquals(0, dao.list("denied1").size());
    assertEquals(1, dao.list("allowed1").size());
    assertEquals(group1.getID(), dao.list("allowed1").get(0).getID());

    createTestUser("denied2", schoolId2);
    createTestUser("allowed2", schoolId2, Permission.CAN_VIEW_SCHOOL_ROLE_ASSIGNMENTS);
    assertEquals(0, dao.list("denied2").size());
    assertEquals(1, dao.list("allowed2").size());
    assertEquals(group2.getID(), dao.list("allowed2").get(0).getID());
  }

}
