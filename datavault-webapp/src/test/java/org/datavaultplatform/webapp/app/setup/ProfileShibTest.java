package org.datavaultplatform.webapp.app.setup;

import static org.assertj.core.api.Assertions.assertThat;
import static org.datavaultplatform.webapp.test.TestUtils.toSet;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import javax.servlet.Filter;
import lombok.SneakyThrows;
import org.datavaultplatform.webapp.authentication.shib.ShibAuthenticationFilter;
import org.datavaultplatform.webapp.authentication.shib.ShibAuthenticationProvider;
import org.datavaultplatform.webapp.test.ProfileShib;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.web.FilterChainProxy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.logout.LogoutFilter;
import org.springframework.security.web.authentication.preauth.AbstractPreAuthenticatedProcessingFilter;
import org.springframework.security.web.session.ConcurrentSessionFilter;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RestController;

/*
 Test checks that the SpringBoot app starts up ok with 'shib' profile
 This test will fail if there is a problem with the Spring Configuration for 'shib' profile.
 */
@SpringBootTest
@ProfileShib
public class ProfileShibTest {

    @Autowired
    CountDownLatch latch;

    @Autowired
    ShibAuthenticationProvider shibAuthenticationProvider;

    @Autowired
    ShibAuthenticationFilter shibFilter;

    @Autowired
    FilterChainProxy proxy;

    @Test
    void testContextIsCorrect(ApplicationContext ctx) throws InterruptedException {
        assertTrue(latch.await(3, TimeUnit.SECONDS));
    }

    @Test
    void testServiceBeans(ApplicationContext ctx) {
      Set<String> serviceNames = toSet(ctx.getBeanNamesForAnnotation(Service.class));
      assertEquals(toSet("forceLogoutService", "restService", "permissionsService","userLookupService","validateService"), serviceNames);
    }

  @Test
  void testControllerBeans(ApplicationContext ctx) {
    Set<String> names = toSet(ctx.getBeanNamesForAnnotation(Controller.class));
    Set<String> restNames = toSet(ctx.getBeanNamesForAnnotation(RestController.class));
    assertTrue(names.containsAll(restNames));
    assertThat(names.size()).isEqualTo(24);
  }

  /**
     * Check that the ShibAuthenticationFilter is associated with the ShibAuthenticationProvider
     */
    @Test
    @SneakyThrows
    void testShibAuthFilterIsAssociatedWithShibAuthProvider() {
        Field f = AbstractPreAuthenticatedProcessingFilter.class.getDeclaredField("authenticationManager");
        f.setAccessible(true);
        ProviderManager providerManager = (ProviderManager) f.get(shibFilter);
        List<AuthenticationProvider> providers = providerManager.getProviders();
        assertThat(providers.size()).isOne();
        assertThat(providers.get(0)).isEqualTo(this.shibAuthenticationProvider);
    }

    @Test
    void testShibFilterIsIncludedInSecurityFilterChainAtCorrectPosition() {
        //Check that there are TWO filter chains ('anyRequest' and '/actuator/**'
        List<SecurityFilterChain> chains = proxy.getFilterChains();
        assertThat(chains.size()).isEqualTo(2);
        SecurityFilterChain anyRequestChain = chains.stream().filter(chain -> !chain.toString().contains("/actuator/**")).findFirst().get();

      Optional<Integer> optIdxLogout = findFilter(anyRequestChain, LogoutFilter.class);
      Optional<Integer> optIdxSession = findFilter(anyRequestChain, ConcurrentSessionFilter.class);
      Optional<Integer> optIdxShib = findFilter(anyRequestChain, ShibAuthenticationFilter.class);

      int idxLogout = optIdxLogout.get();
      int idxShib = optIdxShib.get();
      int idxSession = optIdxSession.get();

      //check that the filter chain contain LogoutFilter, then ShibFilter, then SessionFilter
      assertThat(idxShib).isEqualTo(idxLogout + 1);
      assertThat(idxShib).isEqualTo(idxSession -1);
    }

    Optional<Integer> findFilter(SecurityFilterChain chain, Class<? extends Filter> filter) {
        List<Filter> filters = chain.getFilters();
        for (int i = 0; i < filters.size(); i++) {
            Filter f = chain.getFilters().get(i);
            if (filter.isInstance(f)) {
                return Optional.of(i);
            }
        }
        return Optional.empty();
    }

  @TestConfiguration
  static class TestConfig implements ApplicationListener<ApplicationStartedEvent> {

    @Bean
    CountDownLatch latch() {
      return new CountDownLatch(1);
    }

    @Override
    public void onApplicationEvent(ApplicationStartedEvent event) {
      latch().countDown();
    }
  }

}