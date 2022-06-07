package org.datavaultplatform.common.model.dao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.datavaultplatform.broker.app.DataVaultBrokerApp;
import org.datavaultplatform.broker.test.AddTestProperties;
import org.datavaultplatform.broker.test.BaseReuseDatabaseTest;
import org.datavaultplatform.common.model.DataManager;
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
public class DataManagerDAOIT extends BaseReuseDatabaseTest {

  @Autowired
  DataManagerDAO dao;

  @Test
  void testWriteThenRead() {
    DataManager dataManager1 = getDataManager1();

    DataManager dataManager2 = getDataManager2();

    dao.save(dataManager1);
    assertNotNull(dataManager1.getID());
    assertEquals(1, count());

    dao.save(dataManager2);
    assertNotNull(dataManager2.getID());
    assertEquals(2, count());

    DataManager foundById1 = dao.findById(dataManager1.getID()).get();
    assertEquals(dataManager1.getID(), foundById1.getID());

    DataManager foundById2 = dao.findById(dataManager2.getID()).get();
    assertEquals(dataManager2.getID(), foundById2.getID());
  }

  @Test
  void testList() {
    DataManager dataManager1 = getDataManager1();

    DataManager dataManager2 = getDataManager2();

    dao.save(dataManager1);
    assertNotNull(dataManager1.getID());
    assertEquals(1, count());

    dao.save(dataManager2);
    assertNotNull(dataManager2.getID());
    assertEquals(2, count());

    List<DataManager> items = dao.list();
    assertEquals(2, items.size());
    assertEquals(1, items.stream().filter(dr -> dr.getID().equals(dataManager1.getID())).count());
    assertEquals(1, items.stream().filter(dr -> dr.getID().equals(dataManager2.getID())).count());
  }


  @Test
  void testUpdate() {
    DataManager dataManager1 = getDataManager1();

    dao.save(dataManager1);

    dataManager1.setUUN("111-updated");

    dao.update(dataManager1);

    DataManager found = dao.findById(dataManager1.getID()).get();
    assertEquals(dataManager1.getUUN(), found.getUUN());
  }

  @BeforeEach
  void setup() {
    assertEquals(0, count());
  }

  @AfterEach
  void cleanup() {
    template.execute("delete from `DataManagers`");
    assertEquals(0, count());
  }

  private DataManager getDataManager1() {
    DataManager dataManager = new DataManager();
    dataManager.setUUN("111");
    return dataManager;
  }

  private DataManager getDataManager2() {
    DataManager dataManager = new DataManager();
    dataManager.setUUN("222");
    return dataManager;
  }

  long count() {
    return dao.count();
  }
}
