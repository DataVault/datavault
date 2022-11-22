package org.datavaultplatform.worker.sftp;

import org.datavaultplatform.common.storage.SFTPFileSystemDriver;
import org.datavaultplatform.common.storage.impl.SFTPFileSystemJSch;
import org.springframework.test.context.TestPropertySource;

@TestPropertySource(properties = "sftp.driver.use.apache.sshd=false")
class SftpConfigJcraftJSchTest extends BaseSftpConfigTest {

  @Override
  Class<? extends SFTPFileSystemDriver> getExpectedSftpDriverClass() {
    return SFTPFileSystemJSch.class;
  }
}
