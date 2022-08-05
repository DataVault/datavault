package org.datavaultplatform.worker.config;

import java.security.Security;
import org.datavaultplatform.common.task.Context.AESMode;
import org.datavaultplatform.worker.queue.EventSender;
import org.datavaultplatform.worker.queue.Receiver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
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

  @Autowired
  EventSender eventSender;
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
  public Receiver receiver() {
    Receiver result = new Receiver(
        this.tempDir,
        this.metaDir,
        this.chunkingEnabled,
        this.chunkingByteSize,
        this.encryptionEnabled,
        this.encryptionMode,
        this.multipleValidationEnabled,
        this.noChunkThreads,
        this.eventSender
    );
    if(result.isEncryptionEnabled() && result.getEncryptionMode() == AESMode.GCM ) {
      Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
    }
    return result;
  }
}
