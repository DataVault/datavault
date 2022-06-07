package org.datavaultplatform.common.model.dao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.datavaultplatform.broker.app.DataVaultBrokerApp;
import org.datavaultplatform.broker.test.AddTestProperties;
import org.datavaultplatform.broker.test.BaseReuseDatabaseTest;
import org.datavaultplatform.common.model.RetentionPolicy;
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
public class RetentionPolicyDAOIT extends BaseReuseDatabaseTest {

  @Autowired
  RetentionPolicyDAO dao;

  @Test
  void testWriteThenRead() {
    RetentionPolicy retentionPolicy1 = getRetentionPolicy1();

    RetentionPolicy retentionPolicy2 = getRetentionPolicy2();

    dao.save(retentionPolicy1);
    assertTrue(retentionPolicy1.getID() > 0);
    assertEquals(1, count());

    dao.save(retentionPolicy2);
    assertTrue(retentionPolicy2.getID() > 0);
    assertEquals(2, count());

    RetentionPolicy foundById1 = dao.findById(retentionPolicy1.getID()).get();
    assertEquals(retentionPolicy1.getID(), foundById1.getID());

    RetentionPolicy foundById2 = dao.findById(retentionPolicy2.getID()).get();
    assertEquals(retentionPolicy2.getID(), foundById2.getID());
  }

  @Test
  void testListIsSortedByNameAscending() {
    RetentionPolicy retentionPolicy1 = getRetentionPolicy1();

    RetentionPolicy retentionPolicy2 = getRetentionPolicy2();

    RetentionPolicy retentionPolicy3 = getRetentionPolicy3();

    assertEquals(0, retentionPolicy1.getID());
    dao.save(retentionPolicy1);
    assertTrue(retentionPolicy1.getID() > 0);
    assertEquals(1, count());

    assertEquals(0, retentionPolicy2.getID());
    dao.save(retentionPolicy2);
    assertTrue(retentionPolicy2.getID() > 0);
    assertEquals(2, count());

    assertEquals(0, retentionPolicy3.getID());
    dao.save(retentionPolicy3);
    assertTrue(retentionPolicy3.getID() > 0);
    assertEquals(3, count());

    List<RetentionPolicy> items = dao.list();
    assertEquals(3, items.size());
    assertEquals(1, items.stream().filter(dr -> dr.getID() == retentionPolicy1.getID()).count());
    assertEquals(1, items.stream().filter(dr -> dr.getID() == retentionPolicy2.getID()).count());
    assertEquals(1, items.stream().filter(dr -> dr.getID() == retentionPolicy3.getID()).count());

    // The RetentionPolicy should be ordered by Ascending Name
    assertEquals(
        Arrays.asList(
            retentionPolicy3.getID(),
            retentionPolicy1.getID(),
            retentionPolicy2.getID()),
        items.stream().map(RetentionPolicy::getID).collect(Collectors.toList()));
  }


  @Test
  void testUpdate() {
    RetentionPolicy retentionPolicy = getRetentionPolicy1();

    dao.save(retentionPolicy);

    retentionPolicy.setName("111-updated");

    dao.update(retentionPolicy);

    RetentionPolicy found = dao.findById(retentionPolicy.getID()).get();
    assertEquals(retentionPolicy.getName(), found.getName());
  }

  @BeforeEach
  void setup() {
    assertEquals(0, count());
  }

  @AfterEach
  void cleanup() {
    template.execute("delete from `RetentionPolicies`");
    assertEquals(0, count());
  }

  private RetentionPolicy getRetentionPolicy1() {
    RetentionPolicy ret = new RetentionPolicy();
    ret.setEngine("engine-1");
    ret.setName("DDD");
    return ret;
  }

  private RetentionPolicy getRetentionPolicy2() {
    RetentionPolicy ret = new RetentionPolicy();
    ret.setName("ZZZ");
    ret.setEngine("engine-2");
    return ret;
  }

  private RetentionPolicy getRetentionPolicy3() {
    RetentionPolicy ret = new RetentionPolicy();
    ret.setName("AAA");
    ret.setEngine("engine-3");
    return ret;
  }


  long count() {
    return dao.count();
  }
}
