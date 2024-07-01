package org.datavaultplatform.webapp.app.setup;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.datavaultplatform.webapp.test.ProfileStandalone;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RestController;

/*
 Test checks that the SpringBoot app starts up ok with 'standalone' profile
 This test will fail if there is a problem with the Spring Configuration for 'standalone' profile.
 */
@SpringBootTest
@ProfileStandalone
@Slf4j
public class ProfileStandaloneTest {

  @Autowired
  CountDownLatch latch;

  @Test
  void testContextIsCorrect(ApplicationContext ctx) throws InterruptedException {
    // No AuthenticationProvider for Standalone Profile
    Map<String,AuthenticationProvider> providers = ctx.getBeansOfType(AuthenticationProvider.class);
    log.info("providers = {}", providers);
    assertEquals(DaoAuthenticationProvider.class, providers.get("actuatorAuthenticationProvider").getClass());
    assertEquals(DaoAuthenticationProvider.class, providers.get("standaloneAuthenticationProvider").getClass());

    assertTrue(latch.await(3, TimeUnit.SECONDS));
  }

  @Test
  void testServiceBeans(ApplicationContext ctx){
    Set<String> serviceNames = Set.of(ctx.getBeanNamesForAnnotation(Service.class));
    assertEquals(Set.of("standaloneEvaluatorService",
        "standaloneNotifyLoginService",
        "standaloneNotifyLogoutService",
        "validateService"), serviceNames);
  }

  @Test
  void testControllerBeans(ApplicationContext ctx) {
    Set<String> names = Set.of(ctx.getBeanNamesForAnnotation(Controller.class));
    Set<String> restNames = Set.of(ctx.getBeanNamesForAnnotation(RestController.class));
    assertTrue(names.containsAll(restNames));
    assertEquals(Set.of("protectedTimeController", "timeController", "errorPageController", "simulateErrorController", "authController", "errorController", "fileUploadController", "helloController", "faviconController"), names);
  }

  @TestConfiguration
  static class TestConfig implements ApplicationListener<ApplicationStartedEvent>{

    @Bean
    CountDownLatch latch(){
      return new CountDownLatch(1);
    }

    @Override
    public void onApplicationEvent(ApplicationStartedEvent event) {
      latch().countDown();
    }
  }

}
