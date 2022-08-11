package org.datavaultplatform.broker.app;


import it.burning.cron.CronExpressionDescriptor;
import it.burning.cron.CronExpressionParser.Options;
import java.util.Locale;
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
import org.datavaultplatform.common.crypto.EncryptionValidator;
import org.datavaultplatform.common.monitor.MemoryStats;
import org.springframework.amqp.rabbit.listener.RabbitListenerEndpointRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.SpringBootVersion;
import org.springframework.boot.actuate.scheduling.ScheduledTasksEndpoint;
import org.springframework.boot.actuate.scheduling.ScheduledTasksEndpoint.CronTaskDescription;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.context.event.ApplicationStartingEvent;
import org.springframework.context.annotation.Import;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;

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

  @Value("${spring.application.name}")
  String applicationName;

  @Autowired
  Environment env;

  @Autowired
  EncryptionValidator encryptionValidator;

  @Autowired
  RabbitListenerEndpointRegistry registry;

  @Autowired
  ScheduledTasksEndpoint scheduledTasksEndpoint;

  public static void main(String[] args) {
    SpringApplication.run(DataVaultBrokerApp.class, args);
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

    Stream.of(
        "spring.jpa.hibernate.ddl-auto",
        "spring.sql.init.mode",
        "spring.datasource.username",
        "spring.datasource.password",
        "spring.datasource.url"
    ).forEach(
        propName -> log.info("propName[{}]propValue[{}]", propName, env.getProperty(propName)));

    if (validateEncryptionConfig) {
      encryptionValidator.validate(false, true);
    } else {
      log.info("Encryption Config NOT CHECKED");
    }
  }

  @EventListener
  void onEvent(ApplicationStartingEvent event) {
    log.info("Broker [{}] starting", applicationName);
  }

  @EventListener
  void onEvent(ApplicationReadyEvent readyEvent) {
    log.info("Broker [{}] ready [{}]", applicationName, readyEvent);
    showRabbitListenerContainers();
    showScheduledTasks();
    log.info("{}", MemoryStats.getCurrent().toPretty());
  }

  void showRabbitListenerContainers(){
    registry.getListenerContainers().forEach(container -> {
      if (!container.isRunning()) {
        log.info("application ready - starting listener container [{}]", container);
        container.start();
      }
    });
  }

  void showScheduledTasks() {
    scheduledTasksEndpoint.scheduledTasks().getCron().forEach(td -> {
      CronTaskDescription ctd = (CronTaskDescription)td;
      String cronExpr = ctd.getExpression();
      String description = CronExpressionDescriptor.getDescription(cronExpr, new Options(){{
        setLocale(Locale.getDefault());
        setUse24HourTimeFormat(true);
      }});
      String runnableDescription = ctd.getRunnable().getTarget();
      log.info("CRON[{}][{}][{}]", runnableDescription, cronExpr, description);
    });
  }
}
