package org.datavaultplatform.broker.app;

import lombok.extern.slf4j.Slf4j;
import org.datavaultplatform.broker.config.DatabaseConfig;
import org.datavaultplatform.broker.config.SecurityConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.SpringBootVersion;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.core.env.Environment;

@SpringBootApplication
@ComponentScan({
    "org.datavaultplatform.broker.controllers",
    "org.datavaultplatform.broker.services"})
@Import({SecurityConfig.class, DatabaseConfig.class})
@Slf4j
public class DataVaultBrokerApp implements CommandLineRunner {
  @Autowired
  Environment env;

  public static void main(String[] args) {
    SpringApplication.run(DataVaultBrokerApp.class, args);
  }

  @Override
  public void run(String... args) {
    log.info("java.version [{}]",env.getProperty("java.version"));
    log.info("java.vendor [{}]",env.getProperty("java.vendor"));
    log.info("os.arch [{}]",env.getProperty("os.arch"));
    log.info("os.name [{}]",env.getProperty("os.name"));
    log.info("spring-boot.version [{}]", SpringBootVersion.getVersion());
    log.info("active.profiles {}", (Object) env.getActiveProfiles());
    log.info("git.commit.id.abbrev [{}]", env.getProperty("git.commit.id.abbrev","-1"));
  }


}
