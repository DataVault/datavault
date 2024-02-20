package org.datavaultplatform.broker.config;

import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.hibernate5.jakarta.Hibernate5JakartaModule;
import lombok.extern.slf4j.Slf4j;
import org.datavaultplatform.broker.app.DataVaultBrokerApp;
import org.datavaultplatform.broker.test.AddTestProperties;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(classes = DataVaultBrokerApp.class)
@AddTestProperties
@EnableAutoConfiguration(exclude = {
    DataSourceAutoConfiguration.class,
    DataSourceTransactionManagerAutoConfiguration.class,
    HibernateJpaAutoConfiguration.class})
@TestPropertySource(properties = {
    "broker.controllers.enabled=true",
    //we have to pull in original controllers so we can override them
    "broker.properties.enabled=true",
    "broker.security.enabled=false",
    "broker.scheduled.enabled=false",
    "broker.initialise.enabled=false",
    "broker.services.enabled=false",
    "broker.rabbit.enabled=false",
    "broker.database.enabled=false",
    "broker.email.enabled=true",
    "broker.ldap.enabled=false"})
@Import({MockServicesConfig.class, MockRabbitConfig.class}) //spring security relies on services
@Slf4j
@AutoConfigureMockMvc
@TestPropertySource(properties = "logging.level.org.springframework.security=DEBUG")
public class JacksonConfigTest {

  @Autowired
  MockMvc mvc;

  @Autowired
  ObjectMapper mapper;

  @Test
  void testJacksonHasHibernate5ModuleRegistered() {
    mapper.getRegisteredModuleIds().forEach(System.out::println);
    log.info("moduleIds {}", mapper.getRegisteredModuleIds());
    assertTrue(mapper.getRegisteredModuleIds().contains(Hibernate5JakartaModule.class.getName()));
  }
}
