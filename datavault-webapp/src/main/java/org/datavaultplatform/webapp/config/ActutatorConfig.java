package org.datavaultplatform.webapp.config;

import java.time.Clock;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.datavaultplatform.common.actuator.ActuatorHealthSecurityAdvice;
import org.datavaultplatform.common.actuator.ActuatorInfoSecurityAdvice;
import org.datavaultplatform.common.actuator.ActuatorSecurityAdvice;
import org.datavaultplatform.webapp.actuator.CurrentTimeEndpoint;
import org.datavaultplatform.webapp.actuator.MemoryInfoEndpoint;
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
  public InfoContributor springBootVersionInfoContributor() {
    return builder -> builder.withDetail("spring-boot.version", SpringBootVersion.getVersion());
  }

  @Bean
  public OpenAPI openAPI() {
    return new OpenAPI().info(new Info().title("DataVault WebApp")
            .description("webapp application")
            .version("v0.0.1"));
  }  
}
