package org.datavaultplatform.broker.ldap;

import org.datavaultplatform.broker.app.DataVaultBrokerApp;
import org.datavaultplatform.broker.config.MockServicesConfig;
import org.datavaultplatform.broker.test.AddTestProperties;
import org.datavaultplatform.common.ldap.BaseLDAPServiceTest;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.TestPropertySource;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest(classes = DataVaultBrokerApp.class)
@AddTestProperties
@DirtiesContext(classMode = ClassMode.AFTER_CLASS)
@EnableAutoConfiguration(exclude = {
    DataSourceAutoConfiguration.class,
    DataSourceTransactionManagerAutoConfiguration.class,
    HibernateJpaAutoConfiguration.class})
@TestPropertySource(properties = {
    "broker.controllers.enabled=false",
    "broker.services.enabled=false",
    "broker.scheduled.enabled=false",
    "broker.rabbit.enabled=false",
    "broker.initialise.enabled=false",
    "broker.database.enabled=false"})
@Testcontainers(disabledWithoutDocker = true)
@Import(MockServicesConfig.class) //spring security relies on services
/*
 The LDAPService is defined in datavault-commons but we have to test it in broker because are not just testing LDAPService functionality
 but also that it's wired up/configured correctly within the datavault-broker too.
 This test class uses OpenLdap in a testcontainer with users add via an '.ldif' file.
 */
public class LDAPServiceIT extends BaseLDAPServiceTest {

}
