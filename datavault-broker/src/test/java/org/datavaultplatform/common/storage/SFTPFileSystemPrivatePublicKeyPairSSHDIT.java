package org.datavaultplatform.common.storage;

import java.util.Map;
import org.datavaultplatform.common.docker.DockerImage;
import org.datavaultplatform.common.storage.impl.SFTPFileSystemSSHD;

/*
 Tests SFTPFileSystem class against SFTP Server using private/public keypair authentication.
 Uses SSHD.
 */
public class SFTPFileSystemPrivatePublicKeyPairSSHDIT extends BaseSFTPFileSystemPrivatePublicKeyPairIT {

  @Override
  public String getDockerImageName() {
    return DockerImage.OPEN_SSH_9pt0_IMAGE_NAME;
  }

  @Override
  public SFTPFileSystemDriver getSftpFileSystemDriver() {
    Map<String, String> props = getStoreProperties();
    return new SFTPFileSystemSSHD("mina-sshd", props, TEST_CLOCK);
  }

}
