package org.datavaultplatform.common.model.dao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.datavaultplatform.broker.app.DataVaultBrokerApp;
import org.datavaultplatform.broker.test.AddTestProperties;
import org.datavaultplatform.broker.test.BaseReuseDatabaseTest;
import org.datavaultplatform.common.model.Client;
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
public class ClientDAOIT extends BaseReuseDatabaseTest {

  @Autowired
  ClientDAO dao;

  @Test
  void testWriteThenRead() {
    Client client1 = getClient1();

    Client client2 = getClient2();

    dao.save(client1);
    assertNotNull(client1.getId());
    assertEquals(1, count());

    dao.save(client2);
    assertNotNull(client2.getId());
    assertEquals(2, count());

    Client foundById1 = dao.findById(client1.getId()).get();
    assertEquals(client1.getId(), foundById1.getId());

    Client foundById2 = dao.findById(client2.getId()).get();
    assertEquals(client2.getId(), foundById2.getId());
  }

  @Test
  void testList() {
    Client client1 = getClient1();

    Client client2 = getClient2();

    dao.save(client1);
    assertNotNull(client1.getId());
    assertEquals(1, count());

    dao.save(client2);
    assertNotNull(client2.getId());
    assertEquals(2, count());

    List<Client> items = dao.list();
    assertEquals(2, items.size());
    assertEquals(1, items.stream().filter(dr -> dr.getId().equals(client1.getId())).count());
    assertEquals(1, items.stream().filter(dr -> dr.getId().equals(client2.getId())).count());
  }


  @Test
  void testUpdate() {
    Client client1 = getClient1();

    dao.save(client1);

    client1.setName("111-updated");

    dao.update(client1);

    Client found = dao.findById(client1.getId()).get();
    assertEquals(client1.getName(), found.getName());
  }

  @Test
  void testFindByApiKey() {

    Client client1 = getClient1();

    Client client2 = getClient2();

    dao.save(client1);
    assertNotNull(client1.getId());
    assertEquals(1, count());

    dao.save(client2);
    assertNotNull(client2.getId());
    assertEquals(2, count());

    Client apiKeyOne = dao.findByApiKey(client1.getApiKey());
    assertEquals(client1.getId(), apiKeyOne.getId());
    Client apiKeyTwo = dao.findByApiKey(client2.getApiKey());
    assertEquals(client2.getId(), apiKeyTwo.getId());

    assertNull(dao.findByApiKey("XXX"));
  }

  @BeforeEach
  void setup() {
    assertEquals(0, count());
  }

  @AfterEach
  void cleanup() {
    template.execute("delete from `Clients`");
    assertEquals(0, count());
  }

  private Client getClient1() {
    Client result = new Client();
    result.setId("CLIENT-1");
    result.setName("111");
    result.setApiKey("ONE");
    return result;
  }

  private Client getClient2() {
    Client result = new Client();
    result.setId("CLIENT-2");
    result.setName("222");
    result.setApiKey("TWO");
    return result;
  }

  long count() {
    return dao.count();
  }
}
