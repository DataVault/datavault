package org.datavaultplatform.webapp.app.setup;

import static org.assertj.core.api.Assertions.assertThat;
import static org.datavaultplatform.webapp.test.TestUtils.toSet;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
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
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RestController;

/*
 Test checks that the SpringBoot app starts up ok with 'standalone' profile
 This test will fail if there is a problem with the Spring Configuration for 'standalone' profile.
 */
@SpringBootTest
@ProfileStandalone
public class ProfileStandaloneTest {

  @Autowired
  CountDownLatch latch;

  @Test
  void testContextIsCorrect(ApplicationContext ctx) throws InterruptedException {
      // No AuthenticationProvider for Standalone Profile
      assertThat(ctx.getBeansOfType(AuthenticationProvider.class)).isEmpty();

      assertTrue(latch.await(3, TimeUnit.SECONDS));
  }

  @Test
  void testServiceBeans(ApplicationContext ctx){
    Set<String> serviceNames = toSet(ctx.getBeanNamesForAnnotation(Service.class));
    assertEquals(toSet("standaloneEvaluatorService",
        "standaloneNotifyLoginService",
        "standaloneNotifyLogoutService",
        "validateService"), serviceNames);
  }

  @Test
  void testControllerBeans(ApplicationContext ctx) {
    Set<String> names = toSet(ctx.getBeanNamesForAnnotation(Controller.class));
    Set<String> restNames = toSet(ctx.getBeanNamesForAnnotation(RestController.class));
    assertTrue(names.containsAll(restNames));
    assertEquals(toSet("protectedTimeController", "timeController", "errorPageController", "simulateErrorController", "authController", "errorController", "fileUploadController", "helloController"), names);
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
