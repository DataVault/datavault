package org.datavaultplatform.broker.test;

import static org.datavaultplatform.broker.scheduled.ScheduledUtils.SCHEDULE_1_AUDIT_DEPOSIT;
import static org.datavaultplatform.broker.scheduled.ScheduledUtils.SCHEDULE_2_ENCRYPTION_CHECK;
import static org.datavaultplatform.broker.scheduled.ScheduledUtils.SCHEDULE_3_DELETE;
import static org.datavaultplatform.broker.scheduled.ScheduledUtils.SCHEDULE_4_REVIEW;
import static org.datavaultplatform.broker.scheduled.ScheduledUtils.SCHEDULE_5_RETENTION_CHECK;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.test.context.DynamicPropertyRegistry;

public abstract class ScheduledTestUtils {

  static final Pattern SPEL_PATTERN = Pattern.compile("^(\\$\\{)(.*)(\\})$");

  public static String getPropertyFromCronSpelExpression(String cronSpel) {
    Matcher matcher = SPEL_PATTERN.matcher(cronSpel);
    matcher.matches();
    String propertyName = matcher.group(2);
    return propertyName;
  }

  public static void setCronExpression(DynamicPropertyRegistry registry, String cronSpel, String cronExpression) {
    registry.add(getPropertyFromCronSpelExpression(cronSpel), () -> cronExpression);
  }

  public static void setAllCronExpressionToNever(DynamicPropertyRegistry registry) {
    setAllCronExression(registry, Scheduled.CRON_DISABLED);
  }

  public static void setAllCronExression(DynamicPropertyRegistry registry, String cronExpression){
    setCronExpression(registry, SCHEDULE_1_AUDIT_DEPOSIT, cronExpression);
    setCronExpression(registry, SCHEDULE_2_ENCRYPTION_CHECK, cronExpression);
    setCronExpression(registry, SCHEDULE_3_DELETE, cronExpression);
    setCronExpression(registry, SCHEDULE_4_REVIEW, cronExpression);
    setCronExpression(registry, SCHEDULE_5_RETENTION_CHECK, cronExpression);
  }

}
