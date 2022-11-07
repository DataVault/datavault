package org.datavaultplatform.common.storage;

import java.util.Map;
import org.datavaultplatform.common.docker.DockerImage;
import org.datavaultplatform.common.storage.impl.SFTPFileSystemJSch;

/*
 Tests SFTPFileSystem class against SFTP Server using private/public keypair authentication.
 Uses JSch.
 */
public class SFTPFileSystemPrivatePublicKeyPairJSchIT extends BaseSFTPFileSystemPrivatePublicKeyPairIT {

  /** NOTE : JSch does not work with OPEN_SSH_8pt8 and above **/
  @Override
  public String getDockerImageName() {
    return DockerImage.OPEN_SSH_8pt6_IMAGE_NAME;
  }

  @Override
  public SFTPFileSystemDriver getSftpFileSystemDriver() {
    Map<String, String> props = getStoreProperties();
    return new SFTPFileSystemJSch("sftp-jsch", props, TEST_CLOCK);
  }

}
