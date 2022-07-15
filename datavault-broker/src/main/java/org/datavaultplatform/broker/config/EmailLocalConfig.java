package org.datavaultplatform.broker.config;

import java.sql.Date;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.HashMap;
import lombok.extern.slf4j.Slf4j;
import org.datavaultplatform.broker.services.EmailService;
import org.datavaultplatform.common.email.EmailTemplate;
import org.datavaultplatform.common.model.Vault;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;

@Configuration
@Profile("local")
@Slf4j
public class EmailLocalConfig {

  @Autowired
  EmailService emailService;

  @EventListener
  void messageFromTemp(ApplicationReadyEvent event) {
    log.info("SENDING TEST EMAIL MESSAGE");
    Instant instant = Instant.ofEpochMilli(event.getTimestamp());
    ZonedDateTime now = ZonedDateTime.ofInstant(instant, ZoneId.of("Europe/London"));
    ZonedDateTime plus1year = now.plusYears(1);
    Vault vault = new Vault();
    vault.setReviewDate(new Date(plus1year.toInstant().toEpochMilli()));
    HashMap<String, Object> model = new HashMap<>();
    model.put("home-page", "https://www.google.com");
    model.put("help-page", "https://stackoverflow.com");
    model.put("vault-name", "test vault name");
    model.put("group-name", "test group name");
    model.put("vault-id", "test-vault-id");
    model.put("vault-review-date", vault.getReviewDate());
    model.put("role-name", "TEST_ROLE_NAME");
    emailService.sendTemplateMail("admin@test.com", "broker-startup-mail-test@"  + instant, EmailTemplate.USER_VAULT_CREATE, model);
    log.info("SENT TEST EMAIL MESSAGE");
  }

}
