package org.datavaultplatform.broker.config;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import lombok.extern.slf4j.Slf4j;
import org.datavaultplatform.broker.app.DataVaultBrokerApp;
import org.datavaultplatform.broker.initialise.InitialiseDatabase;
import org.datavaultplatform.broker.initialise.InitialiseEncryption;
import org.datavaultplatform.broker.test.AddTestProperties;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;

/*
In this test, we are checking the 'Initialise' Beans have been created - not their functionality.
We remove many spring beans but are still left with the 'initialise' beans which are injected with mock service beans
for testing.
 */
@SpringBootTest(classes = DataVaultBrokerApp.class)
@AddTestProperties
@Slf4j
@EnableAutoConfiguration(exclude= {
    DataSourceAutoConfiguration.class,
    DataSourceTransactionManagerAutoConfiguration.class,
    HibernateJpaAutoConfiguration.class })
@TestPropertySource(properties = {
    "broker.controllers.enabled=false",
    "broker.services.enabled=false",
    "broker.scheduled.enabled=false",
    "broker.rabbit.enabled=false",
    "broker.database.enabled=false"})
@Import(MockServicesConfig.class)
public class InitialiseBeansConfigTest {

  @Autowired
  InitialiseDatabase initDatabase;

  @Autowired
  InitialiseEncryption initialiseEncryption;

  @Test
  void testContextLoadsWithInitialiseBeans() {
    assertNotNull(initDatabase);
    assertNotNull(initialiseEncryption);
  }
}
