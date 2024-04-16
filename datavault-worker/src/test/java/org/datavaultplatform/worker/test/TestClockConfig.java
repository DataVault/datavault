package org.datavaultplatform.worker.test;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;

@TestConfiguration
public class TestClockConfig {

  public static final Clock TEST_CLOCK;

  static {
    TEST_CLOCK =  Clock.fixed(
            //because of daylight saving 13:15 UTC will be 14:15 BST
            Instant.parse("2022-03-29T13:15:16.101Z"),
            ZoneId.of("Europe/London"));
  }

  @Bean
  @Primary
  public Clock testClock() {
    return TEST_CLOCK;
  }

}
