package org.datavaultplatform.common.model.dao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.datavaultplatform.broker.app.DataVaultBrokerApp;
import org.datavaultplatform.broker.test.AddTestProperties;
import org.datavaultplatform.broker.test.BaseReuseDatabaseTest;
import org.datavaultplatform.broker.test.TestUtils;
import org.datavaultplatform.common.model.FileStore;
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
public class FileStoreDAOIT extends BaseReuseDatabaseTest {

  @Autowired
  FileStoreDAO dao;

  @Test
  void testWriteThenRead() {
    FileStore fileStore1 = getFileStore1();

    FileStore fileStore2 = getFileStore2();

    dao.save(fileStore1);
    assertNotNull(fileStore1.getID());
    assertEquals(1, count());

    dao.save(fileStore2);
    assertNotNull(fileStore2.getID());
    assertEquals(2, count());

    FileStore foundById1 = dao.findById(fileStore1.getID()).get();
    assertEquals(fileStore1.getID(), foundById1.getID());

    FileStore foundById2 = dao.findById(fileStore2.getID()).get();
    assertEquals(fileStore2.getID(), foundById2.getID());
  }

  @Test
  void testList() {
    FileStore fileStore1 = getFileStore1();

    FileStore fileStore2 = getFileStore2();

    dao.save(fileStore1);
    assertNotNull(fileStore1.getID());
    assertEquals(1, count());

    dao.save(fileStore2);
    assertNotNull(fileStore2.getID());
    assertEquals(2, count());

    List<FileStore> items = dao.list();
    assertEquals(2, items.size());
    assertEquals(1, items.stream().filter(dr -> dr.getID().equals(fileStore1.getID())).count());
    assertEquals(1, items.stream().filter(dr -> dr.getID().equals(fileStore2.getID())).count());
  }


  @Test
  void testUpdate() {
    FileStore fileStore1 = getFileStore1();

    dao.save(fileStore1);

    fileStore1.setLabel("111-updated");

    dao.update(fileStore1);

    FileStore found = dao.findById(fileStore1.getID()).get();
    assertEquals(fileStore1.getLabel(), found.getLabel());
  }

  @Test
  void testFileStorePropertiesBLOB(){
    FileStore fileStore = getFileStoreWithProperties();
    dao.save(fileStore);

    FileStore found = dao.findById(fileStore.getID()).get();
    assertEquals(fileStore.getProperties(), found.getProperties());
  }

  @BeforeEach
  void setup() {
    assertEquals(0, count());
  }

  @AfterEach
  void cleanup() {
    template.execute("delete from `FileStores`");
    assertEquals(0, count());
  }

  private FileStore getFileStore1() {
    FileStore fileStore = new FileStore();
    fileStore.setLabel("111");
    return fileStore;
  }

  private FileStore getFileStore2() {
    FileStore fileStore = new FileStore();
    fileStore.setLabel("222");
    return fileStore;
  }

  private FileStore getFileStoreWithProperties() {
    FileStore fileStore = new FileStore();
    fileStore.setLabel("222");
    fileStore.setProperties(TestUtils.getRandomMap());
    return fileStore;
  }

  long count() {
    return dao.count();
  }
}
