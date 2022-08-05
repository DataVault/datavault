package org.datavaultplatform.worker.app;

import lombok.extern.slf4j.Slf4j;
import org.datavaultplatform.common.crypto.EncryptionValidator;
import org.datavaultplatform.worker.config.ActuatorConfig;
import org.datavaultplatform.worker.config.EncryptionConfig;
import org.datavaultplatform.worker.config.EventSenderConfig;
import org.datavaultplatform.worker.config.PropertiesConfig;
import org.datavaultplatform.worker.config.QueueConfig;
import org.datavaultplatform.worker.config.RabbitConfig;
import org.datavaultplatform.worker.config.ReceiverConfig;
import org.datavaultplatform.worker.config.SecurityActuatorConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.SpringBootVersion;
import org.springframework.boot.autoconfigure.SpringBootApplication;
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
    EncryptionConfig.class
})
@Slf4j
public class DataVaultWorkerInstanceApp implements CommandLineRunner {

  public static final String DATAVAULT_HOME = "DATAVAULT_HOME";

  @Value("${spring.application.name}")
  String applicationName;

  @Value("${validate.encryption.config:false}")
  boolean validateEncryptionConfig;

  @Autowired
  Environment env;

  @Autowired
  EncryptionValidator encryptionValidator;

  public static void main(String[] args) {

    //setup properties BEFORE spring starts
    if (System.getenv(DATAVAULT_HOME) == null) {
      log.error("The ENV variable DATAVAULT_HOME must be defined.");
      System.exit(1);
    }
    System.setProperty("datavault-home", System.getenv(DATAVAULT_HOME));

    SpringApplication.run(DataVaultWorkerInstanceApp.class, args);
  }

  @Override
  public void run(String... args) {
    log.info("java.version [{}]", env.getProperty("java.version"));
    log.info("java.vendor [{}]", env.getProperty("java.vendor"));

    log.info("os.arch [{}]", env.getProperty("os.arch"));
    log.info("os.name [{}]", env.getProperty("os.name"));

    log.info("git.commit.id.abbrev [{}]", env.getProperty("git.commit.id.abbrev", "-1"));

    log.info("spring.security.debug [{}]", env.getProperty("spring.security.debug","false"));
    log.info("spring-boot.version [{}]", SpringBootVersion.getVersion());
    log.info("active.profiles {}", (Object) env.getActiveProfiles());

    log.info("validate.encryption.config [{}]", validateEncryptionConfig);

    if (validateEncryptionConfig) {
      encryptionValidator.validate(true, true);
    } else {
      log.info("Encryption Config NOT CHECKED");
    }
  }

  @EventListener
  void onEvent(ApplicationStartingEvent event) {
    log.info("Worker [{}] starting", applicationName);
  }

  @EventListener
  void onEvent(ApplicationReadyEvent event) {
    log.info("Worker [{}] ready", applicationName);
  }
}