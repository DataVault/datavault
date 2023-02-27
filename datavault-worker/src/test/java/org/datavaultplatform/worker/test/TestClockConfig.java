package org.datavaultplatform.worker.test;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;

@TestConfiguration
public class TestClockConfig {

  @Bean
  @Primary
  public Clock testClock() {
    return Clock.fixed(
        Instant.parse("2022-03-29T13:15:16.101Z"),
        ZoneId.of("Europe/London"));
  }

}
