package org.datavaultplatform.worker.sftp;

import org.datavaultplatform.common.storage.SFTPFileSystemDriver;
import org.datavaultplatform.common.storage.impl.SFTPFileSystemSSHD;
import org.springframework.test.context.TestPropertySource;

@TestPropertySource(properties = "sftp.driver.use.apache.sshd=true")
class SftpConfigApacheSSHDTest extends BaseSftpConfigTest {

  @Override
  Class<? extends SFTPFileSystemDriver> getExpectedSftpDriverClass() {
    return SFTPFileSystemSSHD.class;
  }
}
