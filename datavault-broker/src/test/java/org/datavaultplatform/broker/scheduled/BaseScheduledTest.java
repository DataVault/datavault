package org.datavaultplatform.broker.scheduled;

import lombok.extern.slf4j.Slf4j;
import org.datavaultplatform.broker.app.DataVaultBrokerApp;
import org.datavaultplatform.broker.config.MockRabbitConfig;
import org.datavaultplatform.broker.config.MockServicesConfig;
import org.datavaultplatform.broker.test.AddTestProperties;
import org.datavaultplatform.common.services.LDAPService;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;

/*
In this test, we are checking the Beans which will have methods run on a schedule.
We are NOT checking the invocation of the scheduled tasks here.
We tell Spring not to create any DB/Dao classes, not create any Services and not create any Controllers.
We create any services that are still required (by the scheduled beans ) via Mock Beans.
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
    "broker.rabbit.enabled=false",
    "broker.ldap.enabled=false",
    "broker.initialise.enabled=false",
    "broker.email.enabled=false",
    "broker.database.enabled=false"})
@Import({MockServicesConfig.class, MockRabbitConfig.class}) //cos spring security requires some services so we have to mock them
public abstract class BaseScheduledTest {

  @MockBean
  LDAPService service;
}
