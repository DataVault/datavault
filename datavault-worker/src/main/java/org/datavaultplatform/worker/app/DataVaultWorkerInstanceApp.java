package org.datavaultplatform.worker.app;

import lombok.extern.slf4j.Slf4j;
import org.datavaultplatform.worker.config.ActuatorConfig;
import org.datavaultplatform.worker.config.EncryptionConfig;
import org.datavaultplatform.worker.config.EventSenderConfig;
import org.datavaultplatform.worker.config.PropertiesConfig;
import org.datavaultplatform.worker.config.QueueConfig;
import org.datavaultplatform.worker.config.RabbitConfig;
import org.datavaultplatform.worker.config.ReceiverConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;
import org.springframework.core.env.Environment;

@SpringBootApplication
@Import({
    PropertiesConfig.class,
    ActuatorConfig.class,
    QueueConfig.class,
    EventSenderConfig.class,
    ReceiverConfig.class,
    RabbitConfig.class,
    EncryptionConfig.class
})
@Slf4j
public class DataVaultWorkerInstanceApp {

  public static final String DATAVAULT_HOME = "DATAVAULT_HOME";
  @Autowired
  Environment env;

  public static void main(String[] args) {

    //setup properties BEFORE spring starts
    if (System.getenv(DATAVAULT_HOME) == null) {
      log.error("The ENV variable DATAVAULT_HOME must be defined.");
      System.exit(1);
    }
    System.setProperty("datavault-home", System.getenv(DATAVAULT_HOME));

    SpringApplication.run(DataVaultWorkerInstanceApp.class, args);
  }
}