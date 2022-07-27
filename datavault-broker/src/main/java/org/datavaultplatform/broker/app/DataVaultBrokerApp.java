package org.datavaultplatform.broker.app;

import static org.datavaultplatform.broker.scheduled.ScheduledUtils.SCHEDULE_1_AUDIT_DEPOSIT_NAME;
import static org.datavaultplatform.broker.scheduled.ScheduledUtils.SCHEDULE_2_ENCRYPTION_CHECK_NAME;
import static org.datavaultplatform.broker.scheduled.ScheduledUtils.SCHEDULE_3_DELETE_NAME;
import static org.datavaultplatform.broker.scheduled.ScheduledUtils.SCHEDULE_4_REVIEW_NAME;
import static org.datavaultplatform.broker.scheduled.ScheduledUtils.SCHEDULE_5_RETENTION_CHECK_NAME;

import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;
import org.datavaultplatform.broker.config.ActuatorConfig;
import org.datavaultplatform.broker.config.ControllerConfig;
import org.datavaultplatform.broker.config.DatabaseConfig;
import org.datavaultplatform.broker.config.EmailConfig;
import org.datavaultplatform.broker.config.EmailLocalConfig;
import org.datavaultplatform.broker.config.EncryptionConfig;
import org.datavaultplatform.broker.config.InitialiseConfig;
import org.datavaultplatform.broker.config.LdapConfig;
import org.datavaultplatform.broker.config.PropertiesConfig;
import org.datavaultplatform.broker.config.RabbitConfig;
import org.datavaultplatform.broker.config.ScheduleConfig;
import org.datavaultplatform.broker.config.SecurityActuatorConfig;
import org.datavaultplatform.broker.config.SecurityConfig;
import org.datavaultplatform.broker.config.ServiceConfig;
import org.datavaultplatform.common.crypto.Encryption;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.SpringBootVersion;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;
import org.springframework.core.env.Environment;
import org.springframework.util.Assert;

@SpringBootApplication
@Import({
    PropertiesConfig.class, EncryptionConfig.class, ActuatorConfig.class,
    ScheduleConfig.class, InitialiseConfig.class,
    SecurityActuatorConfig.class, SecurityConfig.class, ControllerConfig.class,
    ServiceConfig.class,  DatabaseConfig.class,
    LdapConfig.class, EmailConfig.class, EmailLocalConfig.class, RabbitConfig.class
})
@Slf4j
//@EnableJSONDoc
public class DataVaultBrokerApp implements CommandLineRunner {

  @Value("${validate.encryption.config:false}")
  boolean validateEncryptionConfig;

  @Autowired
  Environment env;

  public static void main(String[] args) {
    SpringApplication.run(DataVaultBrokerApp.class, args);
  }

  @Override
  public void run(String... args) {
    log.info("java.version [{}]", env.getProperty("java.version"));
    log.info("java.vendor [{}]", env.getProperty("java.vendor"));
    log.info("os.arch [{}]", env.getProperty("os.arch"));
    log.info("os.name [{}]", env.getProperty("os.name"));
    log.info("spring.security.debug [{}]", env.getProperty("spring.security.debug"));
    log.info("spring-boot.version [{}]", SpringBootVersion.getVersion());
    log.info("active.profiles {}", (Object) env.getActiveProfiles());
    log.info("git.commit.id.abbrev [{}]", env.getProperty("git.commit.id.abbrev", "-1"));
    log.info("validate.encryption.config [{}]", validateEncryptionConfig);

    Stream.of(
        SCHEDULE_1_AUDIT_DEPOSIT_NAME,
        SCHEDULE_2_ENCRYPTION_CHECK_NAME,
        SCHEDULE_3_DELETE_NAME,
        SCHEDULE_4_REVIEW_NAME,
        SCHEDULE_5_RETENTION_CHECK_NAME).forEach(
        schedule -> log.info("Schedule[{}]value[{}]", schedule, env.getProperty(schedule)));

    Stream.of(
        "spring.jpa.hibernate.ddl-auto",
        "spring.sql.init.mode",
        "spring.datasource.username",
        "spring.datasource.password",
        "spring.datasource.url"
    ).forEach(
        propName -> log.info("propName[{}]propValue[{}]", propName, env.getProperty(propName)));

    if (validateEncryptionConfig) {
      validateEncryptionConfig();
    } else {
      log.info("Encryption Config NOT CHECKED");
    }
  }

  private void validateEncryptionConfig() throws IllegalStateException {
    String randomSecret = UUID.randomUUID().toString();
    String encryptedThenDecrypted = encryptThenDecrypt(randomSecret);
    Assert.isTrue(randomSecret.equals(encryptedThenDecrypted), () -> "Problem  with the setup of Encryption");
    log.info("Encryption Config is Valid");
  }

  private String encryptThenDecrypt(String plainText) throws IllegalStateException {
    try {
      byte[] iv = Encryption.generateIV();
      byte[] encrypted = Encryption.encryptSecret(plainText, null, iv);
      byte[] decrpyted = Encryption.decryptSecret(encrypted, iv, null);
      return new String(decrpyted, StandardCharsets.UTF_8);
    } catch (Exception ex) {
      throw new IllegalStateException("Encryption Config is NOT VALID", ex);
    }
  }

}
