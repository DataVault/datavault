package org.datavaultplatform.worker.app;

import lombok.extern.slf4j.Slf4j;
import org.datavaultplatform.worker.config.ActuatorConfig;
import org.datavaultplatform.worker.config.PropertiesConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;
import org.springframework.core.env.Environment;

@SpringBootApplication
/*
@ComponentScan({
    "org.datavaultplatform.webapp.controllers",
    "org.datavaultplatform.webapp.services"})
 */
@Import({
    PropertiesConfig.class,
    ActuatorConfig.class
})
@Slf4j
public class DataVaultWorkerInstanceApp {

  @Value("${spring.application.name}")
  String applicationName;

  @Autowired
  Environment env;

  public static void main(String[] args) {
    SpringApplication.run(DataVaultWorkerInstanceApp.class, args);
  }
}