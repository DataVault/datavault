package org.datavaultplatform.common.storage;

import java.util.HashMap;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.datavaultplatform.common.docker.DockerImage;
import org.datavaultplatform.common.model.FileInfo;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Testcontainers;

@Slf4j
@Testcontainers(disabledWithoutDocker = true)
public abstract class BaseSFTPFileSystemUsernamePasswordIT extends BaseSFTPFileSystemIT {

  static final String TEST_PASSWORD = "testPassword";
  static final String ENV_PASSWORD = "USER_PASSWORD";
  static final String ENV_PASSWORD_ACCESS = "PASSWORD_ACCESS";

  @Override
  GenericContainer<?> getSftpTestContainer() {
    return new GenericContainer<>(DockerImage.OPEN_SSH_9pt0_IMAGE_NAME)
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

  @Test
  void testSFTP() {
    List<FileInfo> items = this.sftpDriver.list(".");
    items.forEach(System.out::println);
  }
}
