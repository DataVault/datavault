package org.datavaultplatform.broker.config;

import java.time.Clock;
import java.util.function.Function;
import org.datavaultplatform.broker.actuator.CurrentTimeEndpoint;
import org.datavaultplatform.broker.actuator.SftpFileStoreEndpoint;
import org.datavaultplatform.broker.services.FileStoreService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootVersion;
import org.springframework.boot.actuate.info.InfoContributor;
import org.springframework.context.annotation.Bean;

public class ActuatorConfig {

  @Bean
  Clock clock() {
    return Clock.systemDefaultZone();
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
  public Function<String, String> portAdjuster() {
    return port -> port;
  }

  @Bean
  public SftpFileStoreEndpoint sftpFileStoreEndpoint(@Autowired  FileStoreService fileStoreService, Function<String,String> portAdjuster) {
    return new SftpFileStoreEndpoint(fileStoreService, portAdjuster);
  }

}
