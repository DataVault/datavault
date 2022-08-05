package org.datavaultplatform.broker.services;

import lombok.extern.slf4j.Slf4j;
import org.datavaultplatform.common.docker.DockerImage;
import org.testcontainers.utility.DockerImageName;

/**
 * This test that we CAN use an 'ssh-rsa' KeyPair get make an SSH/SFTP connection
 * to a server running OpenSSH 8.6
 */
@Slf4j
public class UserKeyPairServiceOpenSSH8pt6IT extends BaseUserKeyPairServiceOpenSSHTest {


  @Override
  public DockerImageName getDockerImageForOpenSSH() {
    return DockerImageName.parse(DockerImage.OPEN_SSH_8pt6_IMAGE_NAME);
  }

  @Override
  public boolean isSuccessExpected() {
    return true;
  }

}
