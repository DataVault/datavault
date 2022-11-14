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

  private static final String EMAIL_HOME_PAGE = "home-page";
  private static final String EMAIL_HELP_PAGE = "help-page";
  private static final String EMAIL_VAULT_NAME = "vault-name";
  private static final String EMAIL_GROUP_NAME = "group-name";
  private static final String EMAIL_VAULT_ID = "vault-id";
  private static final String EMAIL_VAULT_REVIEW_DATE = "vault-review-date";
  private static final String EMAIL_ROLE_NAME = "role-name";

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
    model.put(EMAIL_HOME_PAGE, "https://www.google.com");
    model.put(EMAIL_HELP_PAGE, "https://stackoverflow.com");
    model.put(EMAIL_VAULT_NAME, "test vault name");
    model.put(EMAIL_GROUP_NAME, "test group name");
    model.put(EMAIL_VAULT_ID, "test-vault-id");
    model.put(EMAIL_VAULT_REVIEW_DATE, vault.getReviewDate());
    model.put(EMAIL_ROLE_NAME, "TEST_ROLE_NAME");
    emailService.sendTemplateMail("admin@test.com", "broker-startup-mail-test@"  + instant, EmailTemplate.USER_VAULT_CREATE, model);
    log.info("SENT TEST EMAIL MESSAGE");
  }

}
