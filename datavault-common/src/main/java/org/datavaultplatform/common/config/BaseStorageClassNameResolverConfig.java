package org.datavaultplatform.common.config;

import lombok.extern.slf4j.Slf4j;
import org.datavaultplatform.common.storage.StorageConstants;
import org.datavaultplatform.common.util.StorageClassNameResolver;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;

@Slf4j
public class BaseStorageClassNameResolverConfig implements ApplicationListener<ApplicationReadyEvent> {

  @Value("${sftp.driver.use.apache.sshd:false}")
  private boolean sftpDriverUseApacheSSHD;

  @Bean
  public StorageClassNameResolver storageClassNameResolver() {
    return new StorageClassNameResolver(sftpDriverUseApacheSSHD);
  }

  @Override
  public void onApplicationEvent(ApplicationReadyEvent event) {
    ApplicationContext ctx = event.getApplicationContext();
    StorageClassNameResolver resolver =  ctx.getBean(StorageClassNameResolver.class);
    String sftpDriverClassName = resolver.resolveStorageClassName(StorageConstants.SFTP_FILE_SYSTEM);
    log.info("For SFTP[{}]using[{}]", StorageConstants.SFTP_FILE_SYSTEM, sftpDriverClassName);
  }
}

