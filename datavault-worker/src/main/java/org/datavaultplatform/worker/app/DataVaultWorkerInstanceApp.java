package org.datavaultplatform.worker.app;

import java.io.File;
import java.util.Arrays;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.datavaultplatform.common.crypto.EncryptionValidator;
import org.datavaultplatform.common.monitor.MemoryStats;
import org.datavaultplatform.common.storage.impl.OracleObjectStorageClassic;
import org.datavaultplatform.common.storage.impl.TivoliStorageManager;
import org.datavaultplatform.worker.config.*;
import org.springframework.amqp.rabbit.listener.RabbitListenerEndpointRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringBootVersion;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.ApplicationPidFileWriter;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.context.event.ApplicationStartingEvent;
import org.springframework.context.annotation.Import;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;

@SpringBootApplication
@Import({
    PropertiesConfig.class,
    ActuatorConfig.class,
    SecurityActuatorConfig.class,
    QueueConfig.class,
    EventSenderConfig.class,
    ReceiverConfig.class,
    RabbitConfig.class,
    EncryptionConfig.class,
    StorageClassNameResolverConfig.class,
    ScheduledTaskConfig.class
})
@Slf4j
public class DataVaultWorkerInstanceApp implements CommandLineRunner {

  public static final String DATAVAULT_HOME = "DATAVAULT_HOME";

  @Value("${spring.application.name}")
  String applicationName;

  @Value("${validate.encryption.config:false}")
  boolean validateEncryptionConfig;

  @Value("${check.oracle.cloud.config:false}")
  boolean checkOracleCloudConfig;

  @Value("${check.tsm.tape.driver:false}")
  boolean checkTSMTapeDriver;

  @Value("${chunking.size}")
  String chunkingSize;

  @Value("${keystore.sha1:}")
  String keyStoreSha1;

  @Autowired
  Environment env;

  @Autowired
  EncryptionValidator encryptionValidator;

  @Autowired
  RabbitListenerEndpointRegistry registry;

  @SneakyThrows
  public static void main(String[] args) {

    Class<?> clazz = Class.forName("org.apache.sshd.sftp.client.SftpErrorDataHandler");
    log.info("SftpErrorDataHandler class [{}]", clazz.getName());

    //setup properties BEFORE spring starts
    if (System.getenv(DATAVAULT_HOME) == null) {
      log.error("The ENV variable DATAVAULT_HOME must be defined.");
      System.exit(1);
    }
    System.setProperty("datavault-home", System.getenv(DATAVAULT_HOME));

    SpringApplicationBuilder app = new SpringApplicationBuilder(DataVaultWorkerInstanceApp.class);
    File pidFile = new File("pids/dv-worker-shutdown.pid");
    log.info("pid file [{}]", pidFile.getCanonicalPath());
    app.build().addListeners(new ApplicationPidFileWriter(pidFile.getCanonicalPath()));
    app.run(args);
  }

  @Override
  public void run(String... args) {
    log.info("Worker ARGS {}", Arrays.toString(args));
    log.info("user.home [{}]", env.getProperty("user.home"));
    log.info("user.dir  [{}]", env.getProperty("user.dir"));

    log.info("java.version [{}]", env.getProperty("java.version"));
    log.info("java.vendor [{}]", env.getProperty("java.vendor"));

    log.info("os.arch [{}]", env.getProperty("os.arch"));
    log.info("os.name [{}]", env.getProperty("os.name"));

    log.info("git.commit.id.abbrev [{}]", env.getProperty("git.commit.id.abbrev", "-1"));

    log.info("spring.security.debug [{}]", env.getProperty("spring.security.debug","false"));
    log.info("spring-boot.version [{}]", SpringBootVersion.getVersion());
    log.info("active.profiles {}", (Object) env.getActiveProfiles());

    log.info("validate.encryption.config [{}]", validateEncryptionConfig);
    log.info("chunking.size [{}]", chunkingSize);

    if (validateEncryptionConfig) {
      encryptionValidator.validate(true, true, keyStoreSha1);
    } else {
      log.info("Encryption Config NOT CHECKED");
    }

    if (checkOracleCloudConfig) {
      OracleObjectStorageClassic.checkConfig();
    } else {
      log.info("Oracle Cloud Config NOT CHECKED");
    }

    if (checkTSMTapeDriver) {
      TivoliStorageManager.checkTSMTapeDriver();
    } else {
      log.info("TSM Tape Driver NOT CHECKED");
    }
  }

  @EventListener
  public void onEvent(ApplicationStartingEvent event) {
    log.info("Worker [{}] starting", applicationName);
  }

  @EventListener
  public void onEvent(ApplicationReadyEvent readyEvent) {
    log.info("Worker [{}] ready [{}]", applicationName, readyEvent);
    registry.getListenerContainers().forEach(container -> {
      if (!container.isRunning()) {
        log.info("application ready - starting listener container [{}]", container);
        container.start();
      }
    });
    log.info("{}", MemoryStats.getCurrent().toPretty());
  }
}