package org.datavaultplatform.worker.sftp;

import static org.junit.jupiter.api.Assertions.assertEquals;

import lombok.extern.slf4j.Slf4j;
import org.datavaultplatform.common.event.RecordingEventSender;
import org.datavaultplatform.common.storage.SFTPFileSystemDriver;
import org.datavaultplatform.common.storage.StorageConstants;
import org.datavaultplatform.common.util.StorageClassNameResolver;
import org.datavaultplatform.worker.app.DataVaultWorkerInstanceApp;
import org.datavaultplatform.worker.test.AddTestProperties;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest(classes = DataVaultWorkerInstanceApp.class)
@Slf4j
@DirtiesContext
@TestPropertySource(properties = "worker.rabbit.enabled=false")
@AddTestProperties
abstract class BaseSftpConfigTest {

  @Autowired
  StorageClassNameResolver storageClassNameResolver;

  @MockBean
  //we have to mock this because it depends on Rabbit which we've not configured
  RecordingEventSender mEventSender;

  @Test
  void testSftpDriverIsAsConfigured() {
    assertEquals(getExpectedSftpDriverClass().getName(), storageClassNameResolver.resolveStorageClassName(StorageConstants.SFTP_FILE_SYSTEM));
  }
  abstract Class<? extends SFTPFileSystemDriver> getExpectedSftpDriverClass();

}
