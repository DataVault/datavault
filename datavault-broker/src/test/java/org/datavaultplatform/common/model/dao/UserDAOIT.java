package org.datavaultplatform.common.model.dao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.datavaultplatform.broker.app.DataVaultBrokerApp;
import org.datavaultplatform.broker.test.AddTestProperties;
import org.datavaultplatform.broker.test.BaseReuseDatabaseTest;
import org.datavaultplatform.broker.test.TestUtils;
import org.datavaultplatform.common.model.User;
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
public class UserDAOIT extends BaseReuseDatabaseTest {

  @Autowired
  UserDAO dao;

  @Test
  void testWriteThenRead() {
    User user1 = getUser1();

    User user2 = getUser2();

    dao.save(user1);
    assertNotNull(user1.getID());
    assertEquals(1, dao.count());

    dao.save(user2);
    assertNotNull(user2.getID());
    assertEquals(2, dao.count());

    User foundById1 = dao.findById(user1.getID()).get();
    assertEquals(user1.getEmail(), foundById1.getEmail());

    User foundById2 = dao.findById(user2.getID()).get();
    assertEquals(user2.getEmail(), foundById2.getEmail());
  }

  @Test
  void testList() {
    User user1 = getUser1();

    User user2 = getUser2();

    dao.save(user1);
    assertEquals(1, dao.count());

    dao.save(user2);
    assertEquals(2, dao.count());

    List<User> items = dao.list();
    assertEquals(2, items.size());
    assertEquals(1,items.stream().filter(dr -> dr.getID().equals(user1.getID())).count());
    assertEquals(1,items.stream().filter(dr -> dr.getID().equals(user2.getID())).count());
  }

  @Test
  void testSearch() {
    User user1 = getUser1();

    User user2 = getUser2();

    dao.save(user1);
    assertEquals(1, dao.count());

    dao.save(user2);
    assertEquals(2, dao.count());

    {
      List<User> items1 = dao.search("user1");
      assertEquals(1, items1.size());
      assertEquals(1, items1.stream().filter(dr -> dr.getID().equals(user1.getID())).count());
    }
    {
      List<User> items2 = dao.search("user2");
      assertEquals(1, items2.size());
      assertEquals(1, items2.stream().filter(dr -> dr.getID().equals(user2.getID())).count());
    }
  }

  @Test
  void testUpdate() {

    User user1 = getUser1();

    dao.save(user1);

    user1.setEmail("user1.user1@test.com");

    User found1 = dao.findById(user1.getID()).get();
    assertEquals("user1@test.com", found1.getEmail());

    dao.update(user1);

    User found2 = dao.findById(user1.getID()).get();
    assertEquals("user1.user1@test.com", found2.getEmail());

  }

  @Test
  void testUserPropertiesBLOB() {

    User user1 = getUserWithProperties();

    dao.save(user1);

    User found1 = dao.findById(user1.getID()).get();
    assertEquals(user1.getProperties().keySet(), found1.getProperties().keySet());
    for(String key : user1.getProperties().keySet()){
      assertEquals(user1.getProperties().get(key), found1.getProperties().get(key));
    }
  }

  @BeforeEach
  void setup() {
    assertEquals(0, dao.count());
  }

  @AfterEach
  void cleanup() {
    template.execute("delete from `Users`");
    assertEquals(0, dao.count());
  }

  private User getUser1() {
    User user = new User();
    user.setID("user1-user1");
    user.setFirstname("firstname1");
    user.setLastname("lastname1");
    user.setEmail("user1@test.com");
    return user;
  }

  private User getUser2() {
    User user = new User();
    user.setID("user2-user2");
    user.setFirstname("firstname2");
    user.setLastname("lastname2");
    user.setEmail("user2@test.com");
    return user;
  }

  private User getUserWithProperties() {
    User user = new User();
    user.setID("user3-user3");
    user.setFirstname("firstname3");
    user.setLastname("lastname3");
    user.setEmail("user3@test.com");
    user.setProperties(TestUtils.getRandomMap());
    return user;
  }

}
