package org.datavaultplatform.broker.config;

import static org.datavaultplatform.broker.test.ScheduledTestUtils.setAllCronExpressionToNever;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.datavaultplatform.broker.scheduled.AuditDepositsChunks;
import org.datavaultplatform.broker.scheduled.BaseScheduledTest;
import org.datavaultplatform.broker.scheduled.CheckEncryptionData;
import org.datavaultplatform.broker.scheduled.CheckForDelete;
import org.datavaultplatform.broker.scheduled.CheckForReview;
import org.datavaultplatform.broker.scheduled.CheckRetentionPolicies;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

/*
In this test, we are checking the Beans which support scheduled tasks have been created.
We are NOT checking the behaviour of the scheduled tasks here.
 */
public class ScheduledBeansConfigIT extends BaseScheduledTest {

  @Autowired
  AuditDepositsChunks auditDepositsChunks;

  @Autowired
  CheckEncryptionData checkEncryptionData;

  @Autowired
  CheckForDelete checkForDelete;

  @Autowired
  CheckForReview checkForReview;

  @Autowired
  CheckRetentionPolicies checkRetentionPolicies;

  @Autowired
  @Qualifier("scheduler")
  ThreadPoolTaskScheduler scheduler;

  @Test
  void testContextLoadsWithScheduledBeans() {

    assertNotNull(auditDepositsChunks);
    assertNotNull(checkEncryptionData);
    assertNotNull(checkForDelete);
    assertNotNull(checkForReview);
    assertNotNull(checkRetentionPolicies);

    assertNotNull(scheduler);

  }

  @DynamicPropertySource
  static void setupProperties(DynamicPropertyRegistry registry) {
    setAllCronExpressionToNever(registry);
  }
}
