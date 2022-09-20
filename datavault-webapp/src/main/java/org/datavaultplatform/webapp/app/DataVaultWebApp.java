package org.datavaultplatform.webapp.app;

import static java.util.Collections.singletonList;

import lombok.extern.slf4j.Slf4j;
import org.datavaultplatform.common.monitor.MemoryStats;
import org.datavaultplatform.webapp.config.ActutatorConfig;
import org.datavaultplatform.webapp.config.LdapConfig;
import org.datavaultplatform.webapp.config.MailConfig;
import org.datavaultplatform.webapp.config.MvcConfig;
import org.datavaultplatform.webapp.config.PropertiesConfig;
import org.datavaultplatform.webapp.config.RestTemplateConfig;
import org.datavaultplatform.webapp.config.SecurityActuatorConfig;
import org.datavaultplatform.webapp.config.SecurityConfig;
import org.datavaultplatform.webapp.config.WebConfig;
import org.datavaultplatform.webapp.config.database.DatabaseProfileConfig;
import org.datavaultplatform.webapp.config.shib.ShibProfileConfig;
import org.datavaultplatform.webapp.config.standalone.StandaloneProfileConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.SpringBootVersion;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.context.event.ApplicationStartingEvent;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer;

@SpringBootApplication
@ComponentScan({
    "org.datavaultplatform.webapp.controllers",
    "org.datavaultplatform.webapp.services"})
@Import({PropertiesConfig.class, WebConfig.class, MvcConfig.class, ActutatorConfig.class,
    SecurityActuatorConfig.class, SecurityConfig.class, MailConfig.class, LdapConfig.class,
        StandaloneProfileConfig.class, DatabaseProfileConfig.class,
    ShibProfileConfig.class, RestTemplateConfig.class})
@Slf4j
public class DataVaultWebApp implements CommandLineRunner {

  @Value("${spring.application.name}")
  String applicationName;

  @Autowired
  Environment env;

  public DataVaultWebApp(FreeMarkerConfigurer freeMarkerConfigurer) {
    freeMarkerConfigurer.getTaglibFactory()
        .setClasspathTlds(singletonList("/META-INF/security.tld"));
  }

  public static void main(String[] args) {
    SpringApplication.run(DataVaultWebApp.class, args);
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

    log.info("broker.url [{}]",env.getProperty("broker.url"));
    log.info("broker.timeout.ms [{}]",env.getProperty("broker.timeout.ms"));
    log.info("broker.api.key [{}]",env.getProperty("broker.api.key"));
  }

  @EventListener
  void onEvent(ApplicationStartingEvent event) {
    log.info("WebApp [{}] starting", applicationName);
  }

  @EventListener
  void onEvent(ApplicationReadyEvent event) {
    log.info("WebApp [{}] ready", applicationName);
    log.info("{}", MemoryStats.getCurrent().toPretty());
  }

}