package org.datavaultplatform.common.model.dao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.datavaultplatform.broker.app.DataVaultBrokerApp;
import org.datavaultplatform.broker.test.AddTestProperties;
import org.datavaultplatform.broker.test.BaseReuseDatabaseTest;
import org.datavaultplatform.broker.test.TestUtils;
import org.datavaultplatform.common.model.Job;
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
public class JobDAOIT extends BaseReuseDatabaseTest {

  @Autowired
  JobDAO dao;

  @Test
  void testWriteThenRead() {
    Job job1 = getJob1();

    Job job2 = getJob2();

    dao.save(job1);
    assertNotNull(job1.getID());
    assertEquals(1, count());

    dao.save(job2);
    assertNotNull(job2.getID());
    assertEquals(2, count());

    Job foundById1 = dao.findById(job1.getID()).get();
    assertEquals(job1.getID(), foundById1.getID());

    Job foundById2 = dao.findById(job2.getID()).get();
    assertEquals(job2.getID(), foundById2.getID());
  }

  @Test
  void testList() {
    Job job1 = getJob1();

    Job job2 = getJob2();

    dao.save(job1);
    assertNotNull(job1.getID());
    assertEquals(1, count());

    dao.save(job2);
    assertNotNull(job2.getID());
    assertEquals(2, count());

    List<Job> items = dao.list();
    assertEquals(2, items.size());
    assertEquals(1, items.stream().filter(dr -> dr.getID().equals(job1.getID())).count());
    assertEquals(1, items.stream().filter(dr -> dr.getID().equals(job2.getID())).count());
  }


  @Test
  void testUpdate() {
    Job job = getJob1();

    dao.save(job);

    job.setTaskClass("111-updated");

    dao.update(job);

    Job found = dao.findById(job.getID()).get();
    assertEquals(job.getTaskClass(), found.getTaskClass());
  }

  @Test
  void testJobStatesBLOB() {
    Job jobWitStates = getJobWitStates();

    dao.save(jobWitStates);

    Job withStatesFromDB = dao.findById(jobWitStates.getID()).get();

    assertIterableEquals(jobWitStates.getStates(), withStatesFromDB.getStates());
  }

  @BeforeEach
  void setup() {
    assertEquals(0, count());
  }

  @AfterEach
  void cleanup() {
    template.execute("delete from `Jobs`");
    assertEquals(0, count());
  }

  private Job getJob1() {
    Job archive = new Job();
    archive.setTaskClass("111");
    return archive;
  }

  private Job getJob2() {
    Job archive = new Job();
    archive.setTaskClass("222");
    return archive;
  }

  private Job getJobWitStates() {
    Job job = new Job();
    job.setTaskClass("333");
    job.setStates(TestUtils.getRandomList());
    return job;
  }

  long count() {
    return dao.count();
  }
}
