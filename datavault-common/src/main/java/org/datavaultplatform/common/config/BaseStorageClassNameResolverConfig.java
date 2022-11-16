package org.datavaultplatform.common.config;

import org.datavaultplatform.common.util.StorageClassNameResolver;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;

public class BaseStorageClassNameResolverConfig {

  @Value("${sftp.driver.use.apache.sshd:true}")
  private boolean sftpDriverUseApacheSSHD;

  @Bean
  public StorageClassNameResolver storageClassNameResolver() {
    return new StorageClassNameResolver(sftpDriverUseApacheSSHD);
  }

}

