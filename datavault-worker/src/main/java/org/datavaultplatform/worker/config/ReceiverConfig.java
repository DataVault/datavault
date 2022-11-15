package org.datavaultplatform.worker.config;

import java.io.File;
import java.security.Security;
import javax.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.datavaultplatform.common.event.RecordingEventSender;
import org.datavaultplatform.common.task.Context.AESMode;
import org.datavaultplatform.common.util.StorageClassNameResolver;
import org.datavaultplatform.worker.queue.Receiver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.Assert;

@Configuration
@Slf4j
public class ReceiverConfig {

  @Value("${tempDir}")
  private String tempDir;

  @Value("${metaDir}")
  private String metaDir;

  @Value("${chunking.enabled:false}")
  private boolean chunkingEnabled;

  @Value("${chunking.size:0}")
  private String chunkingByteSize;

  @Value("${encryption.enabled:false}")
  private boolean encryptionEnabled;

  @Value("${encryption.mode:GCM}")
  private String encryptionMode;

  @Value("${validate.multiple.enabled:true}")
  private boolean multipleValidationEnabled;

  @Value("${chunk.threads:20}")
  private int noChunkThreads;

  @Value("${validate.worker.dirs:false}")
  private boolean validateWorkerDirs;

  @Autowired
  RecordingEventSender eventSender;

  @Value("${sftp.driver.use.jsch:true}")
  private boolean sftpDriverUserJSch;

  /*
      <bean id="receiver" class="org.datavaultplatform.worker.queue.Receiver">
        <property name="tempDir" value="${tempDir}"/>
        <property name="metaDir" value="${metaDir}"/>

        <property name="chunkingEnabled" value="${chunking.enabled:false}"/>
        <property name="chunkingByteSize" value="${chunking.size:0}"/>

        <property name="EncryptionEnabled" value="${encryption.enabled:false}"/>
        <property name="EncryptionMode" value="${encryption.mode:GCM}"/>

        <property name="multipleValidationEnabled" value="${validate.multiple.enabled:true}"/>
        <property name="noChunkThreads" value="${chunk.threads:20}" />
    </bean>

   */
  @Bean
  public Receiver receiver(StorageClassNameResolver resolver) {
    Receiver result = new Receiver(
        this.tempDir,
        this.metaDir,
        this.chunkingEnabled,
        this.chunkingByteSize,
        this.encryptionEnabled,
        this.encryptionMode,
        this.multipleValidationEnabled,
        this.noChunkThreads,
        this.eventSender,
        resolver
    );
    if(result.isEncryptionEnabled() && result.getEncryptionMode() == AESMode.GCM ) {
      Security.addProvider(new BouncyCastleProvider());
    }
    return result;
  }

  @Bean
  StorageClassNameResolver createStorageClassNameResolver() {
    return new StorageClassNameResolver(sftpDriverUserJSch);
  }

  @PostConstruct
  void init() {
    checkFileExistsAndIsDirectory("tempDir", tempDir);
    checkFileExistsAndIsDirectory("metaDir", metaDir);
  }

  void checkFileExistsAndIsDirectory(String dirLabel, String dirValue) {
    if (!validateWorkerDirs) {
      log.warn("[{}] : [{}] : NOT VALIDATED", dirLabel, dirValue);
      return;
    }
    File file = new File(dirValue);
    Assert.isTrue(file.exists(),
        () -> String.format("The [%s] file [%s] does not exist", dirLabel, dirValue));
    Assert.isTrue(file.isDirectory(),
        () -> String.format("The [%s] file [%s] is not a directory", dirLabel, dirValue));
    Assert.isTrue(file.canRead(),
        () -> String.format("The [%s] directory [%s] is not readable", dirLabel, dirValue));
    Assert.isTrue(file.canWrite(),
        () -> String.format("The [%s] directory [%s] is not writable", dirLabel, dirValue));
    log.warn("[{}] : [{}] : is Valid", dirLabel, dirValue);
  }

}
