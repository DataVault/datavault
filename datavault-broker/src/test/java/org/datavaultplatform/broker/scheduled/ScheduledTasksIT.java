package org.datavaultplatform.broker.scheduled;

import static org.assertj.core.api.Assertions.assertThat;
import static org.datavaultplatform.broker.test.ScheduledTestUtils.setAllCronExression;
import static org.mockito.Mockito.doAnswer;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.datavaultplatform.broker.scheduled.BaseScheduledTest;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

/*
The scheduled tasks are configured to be once-every-5-seconds.
This test uses @SpyBean to capture the timestamps of scheduled task invocations.
By waiting 12 seconds - each of the 5 scheduled tasks should be invoked twice.
 */
public class ScheduledTasksIT extends BaseScheduledTest {

  public static String EVERY_FIVE_SECONDS = "*/5 * * * * *";

  @SpyBean
  AuditDepositsChunks task1AuditDepositChunks;
  List<Instant> timestamps1 = new ArrayList<>();

  @SpyBean
  CheckEncryptionData task2CheckEncryptionData;
  List<Instant>timestamps2 = new ArrayList<>();

  @SpyBean
  CheckForDelete task3CheckForDelete;
  List<Instant> timestamps3 = new ArrayList<>();

  @SpyBean
  CheckForReview task4CheckForReview;
  List<Instant> timestamps4  = new ArrayList<>();

  @SpyBean
  CheckRetentionPolicies task5CheckRetentionPolicies;
  List<Instant> timestamps5 = new ArrayList<>();

  List<List<Instant>> allTimestamps = Arrays.asList(timestamps1, timestamps2, timestamps3, timestamps4, timestamps5);


  @Test
  void testTasksEvery5secsHappenAtLeastTwiceIn12secs() throws Exception {
    captureTaskInvocationTimestamps(timestamps1, task1AuditDepositChunks);
    captureTaskInvocationTimestamps(timestamps2, task2CheckEncryptionData);
    captureTaskInvocationTimestamps(timestamps3, task3CheckForDelete);
    captureTaskInvocationTimestamps(timestamps4, task4CheckForReview);
    captureTaskInvocationTimestamps(timestamps5, task5CheckRetentionPolicies);

    //12 seconds is long enough for 2 task invocations of each scheduled task
    //no task should be executed more than 2 times within twelve seconds.
    TimeUnit.SECONDS.sleep(12);

    for (List<Instant> timestamps : allTimestamps) {
      int size = timestamps.size();
      assertThat(size).isGreaterThanOrEqualTo(2);//at least 2 timestamps
      Instant lastTimetamp = timestamps.get(size-1);
      Instant beforeLastTimestamp = timestamps.get(size-2);
      long gapMs = Duration.between(beforeLastTimestamp, lastTimetamp).toMillis();
      assertThat(gapMs).isBetween(4900L, 5200L);
    }

  }

  void captureTaskInvocationTimestamps(List<Instant> timestamps, ScheduledTask scheduledTask) throws Exception {
    doAnswer(invocation -> {
      timestamps.add(Instant.now());
      return null;
    }).when(scheduledTask).execute();
  }

  @DynamicPropertySource
  static void setupProperties(DynamicPropertyRegistry registry) {
    setAllCronExression(registry, EVERY_FIVE_SECONDS);
  }


}
