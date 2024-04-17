package org.datavaultplatform.common.storage;

import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.datavaultplatform.common.storage.impl.SFTPFileSystemJSch;
import org.junit.jupiter.api.BeforeAll;
import org.slf4j.Logger;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/*
  The actual sftp-server-container instance is test class specific.
  The same sftp-server-container instance will be used for all test methods.

  The initialisation of the sftp-server-container is done in 1 of 2 base classes.
  The common sftp test methods are in the base class BaseSftpIT
 */
@Testcontainers(disabledWithoutDocker = true)
@Slf4j
public class SFTPFileSystemUsernamePasswordJSchIT extends BaseSFTPFileSystemUsernamePasswordIT implements SFTPFileSystemJSchIT {

  @Container
  static final GenericContainer<?> container= initialiseContainer("SftpUsernamePasswordJSchDIT");

  @BeforeAll
  static void setupContainer() throws Exception {
    setupFilesWithinContainer(container);
  }

  @Override
  public SFTPFileSystemDriver getSftpDriver() {
    Map<String, String> props = getStoreProperties();
    return new SFTPFileSystemJSch("sftp-jsch", props, TEST_CLOCK);
  }

  @Override
  public GenericContainer<?> getContainer() {
    return container;
  }

  @Override
  Logger getLog() {
    return log;
  }
}
