package org.datavaultplatform.broker.app;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import javax.sql.DataSource;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.datavaultplatform.broker.services.RolesAndPermissionsService;
import org.datavaultplatform.broker.services.VaultsService;
import org.datavaultplatform.broker.test.AddTestProperties;
import org.datavaultplatform.broker.test.BaseDatabaseTest;
import org.datavaultplatform.common.model.User;
import org.datavaultplatform.common.model.dao.UserDAO;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.TransactionManager;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest
@AddTestProperties
@Slf4j
public class DataVaultBrokerAppTest extends BaseDatabaseTest {

  @Autowired
  VaultsService vaultsService;

  @Autowired
  RolesAndPermissionsService rolesAndPermissionsService;

  @Autowired
  DataSource datasource;

  @Autowired
  UserDAO userDAO;

  @Autowired
  SessionFactory sessionFactory;

  @Autowired
  TransactionManager transactionManager;

  @Test
  void testSaveAndReadUser() {
    assertNull(userDAO.findById("1"));
    assertEquals(0, userDAO.count());

    User user = new User();
    user.setID("1");
    user.setEmail("test@test.com");
    user.setFirstname("james");
    user.setLastname("bond");

    userDAO.save(user);
    assertEquals(1, userDAO.count());
    User found = userDAO.findById("1");
    assertEquals("test@test.com", found.getEmail());
    assertEquals("james", found.getFirstname());
    assertEquals("bond", found.getLastname());

    //TODO : yuch - should be easier way to clean up
    Session session = sessionFactory.openSession();
    Transaction tx = session.beginTransaction();
    session.delete(user);
    tx.commit();
    session.close();

    assertNotNull(this.vaultsService);
    assertNotNull(this.rolesAndPermissionsService);
  }


}
