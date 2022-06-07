package org.datavaultplatform.common.model.dao;

import static org.datavaultplatform.broker.test.TestUtils.NOW;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;
import org.datavaultplatform.broker.app.DataVaultBrokerApp;
import org.datavaultplatform.broker.test.AddTestProperties;
import org.datavaultplatform.broker.test.BaseReuseDatabaseTest;
import org.datavaultplatform.common.model.VaultReview;
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
public class VaultReviewDAOIT extends BaseReuseDatabaseTest {

  @Autowired
  VaultReviewDAO dao;

  @Test
  void testWriteThenRead() {
    VaultReview vaultReview1 = getVaultReview1();

    VaultReview vaultReview2 = getVaultReview2();

    dao.save(vaultReview1);
    assertNotNull(vaultReview1.getId());
    assertEquals(1, count());

    dao.save(vaultReview2);
    assertNotNull(vaultReview2.getId());
    assertEquals(2, count());

    VaultReview foundById1 = dao.findById(vaultReview1.getId()).get();
    assertEquals(vaultReview1.getId(), foundById1.getId());

    VaultReview foundById2 = dao.findById(vaultReview2.getId()).get();
    assertEquals(vaultReview2.getId(), foundById2.getId());
  }

  @Test
  void testList() {
    VaultReview vaultReview1 = getVaultReview1();

    VaultReview vaultReview2 = getVaultReview2();

    dao.save(vaultReview1);
    assertEquals(1, count());

    dao.save(vaultReview2);
    assertEquals(2, count());

    List<VaultReview> items = dao.list();
    assertEquals(2, items.size());
    assertEquals(1, items.stream().filter(dr -> dr.getId().equals(vaultReview1.getId())).count());
    assertEquals(1, items.stream().filter(dr -> dr.getId().equals(vaultReview2.getId())).count());
  }


  @Test
  void testUpdate() {
    VaultReview vaultReview1 = getVaultReview1();

    dao.save(vaultReview1);

    vaultReview1.setComment("updated-review-one");

    dao.update(vaultReview1);

    VaultReview found = dao.findById(vaultReview1.getId()).get();
    assertEquals(vaultReview1.getComment(), found.getComment());
  }

  @BeforeEach
  void setup() {
    assertEquals(0, count());
  }

  @AfterEach
  void cleanup() {
    template.execute("delete from `VaultReviews`");
    assertEquals(0, count());
  }

  private VaultReview getVaultReview1() {
    VaultReview result = new VaultReview();
    result.setComment("review-1");
    result.setCreationTime(NOW);
    return result;
  }

  private VaultReview getVaultReview2() {
    VaultReview result = new VaultReview();
    result.setComment("review-2");
    result.setCreationTime(NOW);
    return result;
  }

  long count() {
    return dao.count();
  }

  @Test
  void testSearch() {
    VaultReview vaultReview1 = getVaultReview1();
    VaultReview vaultReview2 = getVaultReview2();
    dao.save(vaultReview1);
    dao.save(vaultReview2);
    assertEquals(2, dao.count());

    assertEquals(Stream.of(vaultReview1).map(VaultReview::getId).collect(Collectors.toList()),
        dao.search(vaultReview1.getId()).stream().map(VaultReview::getId).collect(Collectors.toList()));

    assertEquals(Stream.of(vaultReview2).map(VaultReview::getId).collect(Collectors.toList()),
        dao.search(vaultReview2.getId()).stream().map(VaultReview::getId).collect(Collectors.toList()));

    String partOfIDone = vaultReview1.getId().split(",")[0];
    String partOfIDtwo = vaultReview2.getId().split(",")[0];

    assertEquals(Stream.of(vaultReview1).map(VaultReview::getId).collect(Collectors.toList()),
        dao.search(partOfIDone).stream().map(VaultReview::getId).collect(Collectors.toList()));

    assertEquals(Stream.of(vaultReview2).map(VaultReview::getId).collect(Collectors.toList()),
        dao.search(partOfIDtwo).stream().map(VaultReview::getId).collect(Collectors.toList()));
  }
}
