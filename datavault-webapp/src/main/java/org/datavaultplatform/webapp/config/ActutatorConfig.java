package org.datavaultplatform.webapp.config;

import java.time.Clock;

import org.datavaultplatform.common.actuator.ActuatorHealthSecurityAdvice;
import org.datavaultplatform.common.actuator.ActuatorInfoSecurityAdvice;
import org.datavaultplatform.common.actuator.ActuatorSecurityAdvice;
import org.datavaultplatform.webapp.actuator.CurrentTimeEndpoint;
import org.datavaultplatform.webapp.actuator.MemoryInfoEndpoint;
import org.datavaultplatform.webapp.actuator.RateLimitedEndpoint;
import org.datavaultplatform.webapp.config.ratelimited.RateLimitedProperties;
import org.springframework.boot.SpringBootVersion;
import org.springframework.boot.actuate.info.InfoContributor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ActutatorConfig {

  @Bean
  ActuatorInfoSecurityAdvice actuatorInfoSecurityAdvice() {
    return new ActuatorInfoSecurityAdvice();
  }

  @Bean
  ActuatorHealthSecurityAdvice actuatorHealthSecurityAdvice() {
    return new ActuatorHealthSecurityAdvice();
  }

  @Bean
  ActuatorSecurityAdvice actuatorSecurityAdvice() {
    return new ActuatorSecurityAdvice();
  }

  @Bean
  Clock clock() {
    return Clock.systemDefaultZone();
  }

  @Bean
  CurrentTimeEndpoint currentTime(Clock clock){
      return new CurrentTimeEndpoint(clock);
  }

  @Bean
  MemoryInfoEndpoint memoryInfo(Clock clock) {
    return new MemoryInfoEndpoint(clock);
  }

  @Bean
  RateLimitedEndpoint rateLimited(RateLimitedProperties rateLimitedProperties) {
    return new RateLimitedEndpoint(rateLimitedProperties);
  }

  @Bean
  public InfoContributor springBootVersionInfoContributor() {
    return builder -> builder.withDetail("spring-boot.version", SpringBootVersion.getVersion());
  }

}
