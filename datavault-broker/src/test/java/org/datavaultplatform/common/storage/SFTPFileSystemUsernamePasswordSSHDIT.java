package org.datavaultplatform.common.storage;

import java.util.Map;
import org.datavaultplatform.common.storage.impl.SFTPFileSystemSSHD;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/*
  The actual sftp-server-container instance is test class specific.
  The same sftp-server-container instance will be used for all test methods.

  The initialisation of the sftp-server-container is done in 1 of 2 base classes.
  The common sftp test methods are in the base class BaseSftpIT
 */
@Testcontainers
public class SFTPFileSystemUsernamePasswordSSHDIT extends BaseSFTPFileSystemUsernamePasswordIT {

  @Container
  static final GenericContainer<?> container;

  static {
    container = initialiseContainer("SftpUsernamePasswordSSHDIT");
  }

  @Override
  public SFTPFileSystemDriver getSftpDriver() {
    Map<String, String> props = getStoreProperties();
    return new SFTPFileSystemSSHD("mina-sshd", props, TEST_CLOCK);
  }

  @Override
  public GenericContainer<?> getContainer() {
    return container;
  }

}
