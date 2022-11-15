package org.datavaultplatform.broker.config;

import org.datavaultplatform.common.util.StorageClassNameResolver;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class StorageClassNameResolverConfig {

  @Value("${sftp.driver.use.jsch:true}")
  private boolean sftpDriverUseJsch;

  @Bean
  StorageClassNameResolver storageClassNameResolver() {
    return new StorageClassNameResolver(sftpDriverUseJsch);
  }

}
