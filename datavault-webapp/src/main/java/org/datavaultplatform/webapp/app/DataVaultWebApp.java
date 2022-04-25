package org.datavaultplatform.webapp.app;

import static java.util.Collections.singletonList;

import lombok.extern.slf4j.Slf4j;
import org.datavaultplatform.webapp.config.ActutatorConfig;
import org.datavaultplatform.webapp.config.LdapConfig;
import org.datavaultplatform.webapp.config.MailConfig;
import org.datavaultplatform.webapp.config.MvcConfig;
import org.datavaultplatform.webapp.config.PropertiesConfig;
import org.datavaultplatform.webapp.config.RestTemplateConfig;
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
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.core.env.Environment;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer;

@SpringBootApplication
@ComponentScan({
    "org.datavaultplatform.webapp.controllers",
    "org.datavaultplatform.webapp.services"})
@Import({PropertiesConfig.class, WebConfig.class, MvcConfig.class, ActutatorConfig.class,
    SecurityConfig.class, MailConfig.class, LdapConfig.class,
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
    log.info("java.version [{}]",env.getProperty("java.version"));
    log.info("java.vendor [{}]",env.getProperty("java.vendor"));
    log.info("os.arch [{}]",env.getProperty("os.arch"));
    log.info("os.name [{}]",env.getProperty("os.name"));
    log.info("spring-boot.version [{}]", SpringBootVersion.getVersion());
    log.info("active.profiles {}", (Object) env.getActiveProfiles());
    log.info("git.commit.id.abbrev [{}]", env.getProperty("git.commit.id.abbrev","-1"));
  }
}
