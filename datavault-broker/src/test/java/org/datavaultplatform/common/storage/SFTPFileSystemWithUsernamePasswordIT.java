package org.datavaultplatform.common.storage;

import java.util.HashMap;
import lombok.extern.slf4j.Slf4j;
import org.datavaultplatform.common.docker.DockerImage;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Testcontainers;

/*
 Tests SFTPFileSystem class against SFTP Server using username/password authentication.
 */
@Slf4j
@Testcontainers(disabledWithoutDocker = true)
public class SFTPFileSystemWithUsernamePasswordIT extends BaseSFTPFileSystemIT {

  static final String TEST_PASSWORD = "testPassword";
  static final String ENV_PASSWORD = "USER_PASSWORD";
  static final String ENV_PASSWORD_ACCESS = "PASSWORD_ACCESS";

  @Override
  GenericContainer<?> getSftpTestContainer() {
    return new GenericContainer<>(DockerImage.OPEN_SSH_8pt6_IMAGE_NAME)
        .withEnv(ENV_USER_NAME, TEST_USER)
        .withEnv(ENV_PASSWORD, TEST_PASSWORD)
        .withEnv(ENV_PASSWORD_ACCESS, "true")
        .withExposedPorts(2222)
        .waitingFor(Wait.forListeningPort());
  }

  @Override
  void addAuthenticationProps(HashMap<String, String> props) {
    props.put("password", TEST_PASSWORD);
  }
}
