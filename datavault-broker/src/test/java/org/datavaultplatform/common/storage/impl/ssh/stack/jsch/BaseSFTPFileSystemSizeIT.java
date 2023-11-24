package org.datavaultplatform.common.storage.impl.ssh.stack.jsch;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.Map;
import lombok.SneakyThrows;
import org.datavaultplatform.common.PropNames;
import org.datavaultplatform.common.storage.SFTPFileSystemDriver;
import org.slf4j.Logger;
import org.testcontainers.containers.GenericContainer;

public abstract class BaseSFTPFileSystemSizeIT {

  static final int SFTP_SERVER_PORT = 2222;

  static final String ENV_USER_NAME = "USER_NAME";
  static final String TEST_USER = "testuser";

  static final String SFTP_ROOT_DIR = "/config";
  static final Clock TEST_CLOCK = Clock.fixed(Instant.parse("2022-03-26T09:44:33.22Z"),
      ZoneId.of("Europe/London"));
  static final String TEMP_PREFIX = "dvSftpTempDir";
  static final long EXPECTED_SPACE_AVAILABLE_ON_SFTP_SERVER = 100_000;
  private static final int FREE_SPACE_FACTOR = 10;

  public abstract GenericContainer<?> getContainer();

  public abstract SFTPFileSystemDriver getSftpDriver();

  public abstract void addAuthenticationProps(Map<String,String> props);

  public final int getSftpServerPort() {
    return getContainer().getMappedPort(SFTP_SERVER_PORT);
  }

  public final String getSftpServerHost() {
    return getContainer().getHost();
  }

  @SneakyThrows
  protected Map<String, String> getStoreProperties() {
    HashMap<String, String> props = new HashMap<>();

    //standard sftp properties
    props.put(PropNames.USERNAME, TEST_USER);
    props.put(PropNames.ROOT_PATH, SFTP_ROOT_DIR); //this is the directory ON THE SFTP SERVER - for ALL OpenSSH containers, it's config
    props.put(PropNames.HOST, getSftpServerHost());
    props.put(PropNames.PORT, String.valueOf(getSftpServerPort()));

    addAuthenticationProps(props);

    return props;
  }

  abstract Logger getLog();
}
