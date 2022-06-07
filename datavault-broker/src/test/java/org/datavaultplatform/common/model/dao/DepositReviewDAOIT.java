package org.datavaultplatform.common.model.dao;

import static org.datavaultplatform.broker.test.TestUtils.NOW;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.datavaultplatform.broker.app.DataVaultBrokerApp;
import org.datavaultplatform.broker.test.AddTestProperties;
import org.datavaultplatform.broker.test.BaseReuseDatabaseTest;
import org.datavaultplatform.common.model.DepositReview;
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
public class DepositReviewDAOIT extends BaseReuseDatabaseTest {

  @Autowired
  DepositReviewDAO dao;

  @Test
  void testWriteThenRead() {
    DepositReview depositReview1 = getDepositReview1();

    DepositReview depositReview2 = getDepositReview2();

    dao.save(depositReview1);
    assertNotNull(depositReview1.getId());
    assertEquals(1, dao.count());

    dao.save(depositReview2);
    assertNotNull(depositReview2.getId());
    assertEquals(2, dao.count());

    DepositReview foundById1 = dao.findById(depositReview1.getId()).get();
    assertEquals(depositReview1.getComment(), foundById1.getComment());

    DepositReview foundById2 = dao.findById(depositReview2.getId()).get();
    assertEquals(depositReview2.getComment(), foundById2.getComment());
  }

  @Test
  void testList() {
    DepositReview depositReview1 = new DepositReview();
    depositReview1.setComment("dr1-comment");
    depositReview1.setCreationTime(NOW);

    DepositReview depositReview2 = new DepositReview();
    depositReview2.setComment("dr2-comment");
    depositReview2.setCreationTime(NOW);

    dao.save(depositReview1);
    assertNotNull(depositReview1.getId());
    assertEquals(1, dao.count());

    dao.save(depositReview2);
    assertNotNull(depositReview2.getId());
    assertEquals(2, dao.count());

    List<DepositReview> items = dao.list();
    assertEquals(2, items.size());
    assertEquals(1, items.stream().filter(dr -> dr.getId().equals(depositReview1.getId())).count());
    assertEquals(1, items.stream().filter(dr -> dr.getId().equals(depositReview2.getId())).count());
  }


  @Test
  void testSearch() {

    DepositReview depositReview1 = getDepositReview1();
    dao.save(depositReview1);
    assertNotNull(depositReview1.getId());
    assertEquals(1, dao.count());

    DepositReview depositReview2 = getDepositReview2();
    dao.save(depositReview2);
    assertNotNull(depositReview2.getId());
    assertEquals(2, dao.count());

    {
      String search1 = depositReview1.getId().split("-")[0];
      List<DepositReview> items1 = dao.search(search1);
      assertEquals(1, items1.size());
      assertEquals(1, items1.stream().filter(dr -> dr.getId().equals(depositReview1.getId())).count());
    }
    {
      String search2 = depositReview2.getId().split("-")[0];
      List<DepositReview> items2 = dao.search(search2);
      assertEquals(1, items2.size());
      assertEquals(1, items2.stream().filter(dr -> dr.getId().equals(depositReview2.getId())).count());
    }
  }

  @Test
  void testUpdate() {

    DepositReview depositReview = getDepositReview1();

    dao.save(depositReview);

    depositReview.setComment("dr1-comment-updated");

    DepositReview found1 = dao.findById(depositReview.getId()).get();
    assertEquals("dr1-comment", found1.getComment());

    dao.update(depositReview);

    DepositReview found2 = dao.findById(depositReview.getId()).get();
    assertEquals("dr1-comment-updated", found2.getComment());

  }

  @BeforeEach
  void setup() {
    assertEquals(0, dao.count());
  }

  @AfterEach
  void tidyUp() {
    template.execute("delete from `DepositReviews`");
    assertEquals(0, dao.count());
  }

  DepositReview getDepositReview1() {
    DepositReview result = new DepositReview();
    result.setComment("dr1-comment");
    result.setCreationTime(NOW);
    return result;
  }

  DepositReview getDepositReview2(){
    DepositReview result = new DepositReview();
    result.setComment("dr2-comment");
    result.setCreationTime(NOW);
    return result;
  }
}
