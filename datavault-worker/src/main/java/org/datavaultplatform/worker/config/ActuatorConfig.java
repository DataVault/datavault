package org.datavaultplatform.worker.config;

import java.time.Clock;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.datavaultplatform.worker.actuator.CurrentTimeEndpoint;
import org.datavaultplatform.worker.actuator.MemoryInfoEndpoint;
import org.springframework.boot.SpringBootVersion;
import org.springframework.boot.actuate.info.InfoContributor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ActuatorConfig {

  @Bean
  Clock clock() {
    return Clock.systemDefaultZone();
  }

  @Bean
  MemoryInfoEndpoint memoryInfoEndpoint(Clock clock) {
    return new MemoryInfoEndpoint(clock);
  }

  @Bean
  CurrentTimeEndpoint currentTime(Clock clock){
      return new CurrentTimeEndpoint(clock);
  }

  @Bean
  public InfoContributor springBootVersionInfoContributor() {
    return builder -> builder.withDetail("spring-boot.version", SpringBootVersion.getVersion());
  }

  @Bean
  public OpenAPI openAPI() {
    return new OpenAPI().info(new Info().title("DataVault Worker")
            .description("worker application")
            .version("v0.0.1"));
  }
}
