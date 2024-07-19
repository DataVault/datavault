package org.datavaultplatform.common.storage;

import java.util.Map;
import lombok.SneakyThrows;
import org.datavaultplatform.common.PropNames;
import org.datavaultplatform.common.docker.DockerImage;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.MountableFile;

public abstract class BaseSFTPFileSystemUsernamePasswordIT extends BaseSFTPFileSystemIT {

  static final String TEST_PASSWORD = "testPassword";
  static final String ENV_PASSWORD = "USER_PASSWORD";
  static final String ENV_PASSWORD_ACCESS = "PASSWORD_ACCESS";


  static GenericContainer<?> initialiseContainer(String tcName) {

    return new GenericContainer<>(DockerImage.OPEN_SSH_9pt7_IMAGE_NAME)
        .withEnv("TC_NAME", tcName)
        .withEnv(ENV_USER_NAME, TEST_USER)
        .withEnv(ENV_PASSWORD, TEST_PASSWORD)
        .withEnv(ENV_PASSWORD_ACCESS, "true")
        .withExposedPorts(SFTP_SERVER_PORT)
        .withCopyFileToContainer(MountableFile.forHostPath(tempLocalPath),"/config")
        .waitingFor(Wait.forListeningPort());
  }


  @SneakyThrows
  @Override
  public void addAuthenticationProps(Map<String, String> props) {
    props.put(PropNames.PASSWORD, TEST_PASSWORD);
  }




}
