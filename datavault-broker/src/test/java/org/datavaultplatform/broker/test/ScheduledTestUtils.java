package org.datavaultplatform.broker.test;

import static org.datavaultplatform.broker.scheduled.ScheduledUtils.SCHEDULE_1_AUDIT_DEPOSIT_NAME;
import static org.datavaultplatform.broker.scheduled.ScheduledUtils.SCHEDULE_2_ENCRYPTION_CHECK_NAME;
import static org.datavaultplatform.broker.scheduled.ScheduledUtils.SCHEDULE_3_DELETE_NAME;
import static org.datavaultplatform.broker.scheduled.ScheduledUtils.SCHEDULE_4_REVIEW_NAME;
import static org.datavaultplatform.broker.scheduled.ScheduledUtils.SCHEDULE_5_RETENTION_CHECK_NAME;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.test.context.DynamicPropertyRegistry;

public abstract class ScheduledTestUtils {

  public static void setCronExpression(DynamicPropertyRegistry registry, String scheduleName, String cronExpression) {
    registry.add(scheduleName, () -> cronExpression);
  }

  public static void setAllCronExpressionToNever(DynamicPropertyRegistry registry) {
    setAllCronExression(registry, Scheduled.CRON_DISABLED);
  }

  public static void setAllCronExression(DynamicPropertyRegistry registry, String cronExpression){
    setCronExpression(registry, SCHEDULE_1_AUDIT_DEPOSIT_NAME, cronExpression);
    setCronExpression(registry, SCHEDULE_2_ENCRYPTION_CHECK_NAME, cronExpression);
    setCronExpression(registry, SCHEDULE_3_DELETE_NAME, cronExpression);
    setCronExpression(registry, SCHEDULE_4_REVIEW_NAME, cronExpression);
    setCronExpression(registry, SCHEDULE_5_RETENTION_CHECK_NAME, cronExpression);
  }

}
