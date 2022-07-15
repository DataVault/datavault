package org.datavaultplatform.broker.email;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.datavaultplatform.broker.app.DataVaultBrokerApp;
import org.datavaultplatform.broker.config.MockServicesConfig;
import org.datavaultplatform.broker.test.AddTestProperties;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest(classes = DataVaultBrokerApp.class)
@Slf4j
@DirtiesContext
@EnableAutoConfiguration(exclude= {
    DataSourceAutoConfiguration.class,
    DataSourceTransactionManagerAutoConfiguration.class,
    HibernateJpaAutoConfiguration.class })
@TestPropertySource(properties = {
    "broker.controllers.enabled=false",
    "broker.services.enabled=false",
    "broker.rabbit.enabled=false",
    "broker.scheduled.enabled=false",
    "broker.initialise.enabled=false",
    "broker.database.enabled=false"})
@AddTestProperties
@Import(MockServicesConfig.class) //spring security relies on services
public class EmailBodyGeneratorTest {

  @Autowired
  EmailBodyGenerator generator;

  @Test
  void testBasicVeloctyTemplateWithVariable() {
    String random = UUID.randomUUID().toString();
    Map<String,Object> model = new HashMap<String,Object>(){{
          put("data-data-data",random);
    }};
    String body = generator.generate("test.vm", model);
    assertEquals(String.format("BEFORE%sAFTER", random), body);
  }

}
