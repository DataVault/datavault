package org.datavaultplatform.webapp.test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import lombok.extern.slf4j.Slf4j;
import org.datavaultplatform.common.request.CreateClientEvent;
import org.datavaultplatform.webapp.services.NotifyLogoutService;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

@TestConfiguration
@Slf4j
public class WaitForLogoutNotificationConfig {

  @Bean
  CountDownLatch logoutLatch(){
    return new CountDownLatch(1);
  }

  @Bean
  List<CreateClientEvent> logoutEvents(){
    return new ArrayList<>();
  }

  @Bean
  @Primary
  NotifyLogoutService notifyLogoutService() {
    return clientEvent -> {
      logoutEvents().add(clientEvent);
      log.info("Got Logout Notification via {}",clientEvent);
      logoutLatch().countDown();
      return "NOTIFY";
    };
  }

}
