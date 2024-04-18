package org.datavaultplatform.broker.services;

import lombok.extern.slf4j.Slf4j;
import org.datavaultplatform.common.docker.DockerImage;
import org.testcontainers.utility.DockerImageName;

/**
 * This test with new version of JSch <a href="https://mvnrepository.com/artifact/com.github.mwiede/jsch">new JSch</a>
 * - we CAN use an 'ssh-rsa' KeyPair get make an SSH/SFTP connection
 * to a server running OpenSSH 8.8+
 */
@Slf4j
public class UserKeyPairServiceOpenSSH8pt8IT extends BaseUserKeyPairServiceOpenSSHTest {


  @Override
  public DockerImageName getDockerImageForOpenSSH() {
    return DockerImageName.parse(DockerImage.OPEN_SSH_8pt8_IMAGE_NAME);
  }

  @Override
  public boolean isSuccessExpected() {
    return true;
  }

}
