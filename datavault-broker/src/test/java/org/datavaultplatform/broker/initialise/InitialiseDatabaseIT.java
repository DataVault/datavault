package org.datavaultplatform.broker.initialise;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.datavaultplatform.broker.app.DataVaultBrokerApp;
import org.datavaultplatform.broker.services.ArchiveStoreService;
import org.datavaultplatform.broker.test.AddTestProperties;
import org.datavaultplatform.broker.test.BaseDatabaseTest;
import org.datavaultplatform.common.model.ArchiveStore;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.ApplicationListener;
import org.springframework.test.context.TestPropertySource;
import org.testcontainers.shaded.org.awaitility.Awaitility;

@SpringBootTest(classes = DataVaultBrokerApp.class)
@AddTestProperties
@Slf4j
@TestPropertySource(properties = {
    "broker.email.enabled=true",
    "broker.controllers.enabled=false",
    "broker.rabbit.enabled=false",
    "broker.scheduled.enabled=false"
})
public class InitialiseDatabaseIT extends BaseDatabaseTest {

  //TODO - use thread local instead?
  private static BrokerInitialisedEvent initEvent;

  @Autowired
  ArchiveStoreService service;

  @BeforeAll
  static void beforeAll() {
    initEvent = null;
  }

  @Test
  void testSaveAndReadUser() {
    long start = System.currentTimeMillis();
    Awaitility.await().atMost(60, TimeUnit.SECONDS).until(() -> initEvent != null);
    long diff = System.currentTimeMillis() - start;
    log.info("time to init {}ms", diff);
    assertEquals(2, initEvent.getAdded().size());
    assertEquals(service.getArchiveStores().stream().map(ArchiveStore::getStorageClass)
            .collect(Collectors.toSet()),
        initEvent.getAdded().stream().map(ArchiveStore::getStorageClass).collect(
            Collectors.toSet()));
  }

  @TestConfiguration
  static class TestConfig implements ApplicationListener<BrokerInitialisedEvent> {

    @Override
    public void onApplicationEvent(BrokerInitialisedEvent event) {
      log.info("event {}", event);
      initEvent = event;
    }
  }
}
