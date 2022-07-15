package org.datavaultplatform.broker.initialise;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.datavaultplatform.broker.app.DataVaultBrokerApp;
import org.datavaultplatform.broker.services.ArchiveStoreService;
import org.datavaultplatform.broker.test.BaseDatabaseTest;
import org.datavaultplatform.common.model.ArchiveStore;
import org.datavaultplatform.common.storage.Device;
import org.datavaultplatform.common.storage.impl.LocalFileSystem;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.ApplicationListener;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.testcontainers.shaded.org.awaitility.Awaitility;

@SpringBootTest(classes = DataVaultBrokerApp.class)
@TestPropertySource("classpath:datavault-test.properties")
@TestPropertySource(properties = {"mail.password=dummy","sftp.passphrase=DUMMY"})
@Slf4j
@TestPropertySource(properties = {
    "broker.email.enabled=true",
    "broker.controllers.enabled=false",
    "broker.rabbit.enabled=false",
    "broker.scheduled.enabled=false"
})
@ActiveProfiles({"test","local"})
public class InitialiseDatabaseLocalFileSystemIT extends BaseDatabaseTest {

  @TempDir
  private static File TMP_LOCAL_FS_DIR;

  //TODO - use thread local instead?
  private static BrokerInitialisedEvent initEvent;

  @Autowired
  ArchiveStoreService service;

  @BeforeAll
  static void beforeAll() {
    initEvent = null;
  }

  @Value("${archive.store.local.root.path}")
  private String localFsDirectory;

  @Test
  @SneakyThrows
  void testArchiveStoresInitialiseWithLocalFileSystem() {
    log.info("spring localFsDirectory {}", localFsDirectory);
    long start = System.currentTimeMillis();
    Awaitility.await().atMost(60, TimeUnit.SECONDS).until(() -> initEvent != null);
    long diff = System.currentTimeMillis() - start;
    log.info("time to init {}ms", diff);
    assertEquals(3, initEvent.getAdded().size());
    List<ArchiveStore> savedStores = service.getArchiveStores();
    assertEquals(savedStores.stream().map(ArchiveStore::getStorageClass)
            .collect(Collectors.toSet()),
        initEvent.getAdded().stream().map(ArchiveStore::getStorageClass).collect(
            Collectors.toSet()));

    Set<String> storageClassNames = initEvent.getAdded().stream()
        .map(ArchiveStore::getStorageClass)
        .collect(Collectors.toSet());
    assertTrue(storageClassNames.contains(LocalFileSystem.class.getName()));
    ArchiveStore local = savedStores.stream()
        .filter(as -> as.getStorageClass().equals(LocalFileSystem.class.getName()))
        .findFirst().get();
    assertEquals(local.getProperties().get(LocalFileSystem.ROOT_PATH), localFsDirectory);
    assertTrue(local.isRetrieveEnabled());

    Device device = local.getDevice();
    assertThat(device).isInstanceOf(LocalFileSystem.class);
    assertTrue(device instanceof LocalFileSystem);
  }

  @TestConfiguration
  static class TestConfig implements ApplicationListener<BrokerInitialisedEvent> {

    @Override
    public void onApplicationEvent(BrokerInitialisedEvent event) {
      log.info("event {}", event);
      initEvent = event;
    }
  }

  @DynamicPropertySource
  static void registerProperties(DynamicPropertyRegistry registry) {
    log.info("TEMP DIR IS {}", TMP_LOCAL_FS_DIR);
    registry.add(InitialiseDatabase.ARCHIVE_STORE_LOCAL_ROOT_PATH, () -> TMP_LOCAL_FS_DIR.toPath().toString());
  }
}
