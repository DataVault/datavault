package org.datavaultplatform.webapp.config.standalone;

import lombok.extern.slf4j.Slf4j;
import org.datavaultplatform.webapp.authentication.AuthenticationSuccess;
import org.datavaultplatform.webapp.config.HttpSecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationEventPublisher;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Slf4j
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true, securedEnabled = true)
public class StandaloneWebSecurityConfig {

  @Value("${datavault.csrf.disabled:false}")
  boolean csrfDisabled;

  @Autowired
  SessionRegistry sessionRegistry;

  @Autowired
  AuthenticationSuccess authenticationSuccess;

  @Bean
  @Order(2)
  public SecurityFilterChain filterChain(HttpSecurity http,
                                         @Qualifier("standaloneAuthenticationProvider") AuthenticationProvider authenticationProvider) throws Exception {

    HttpSecurityUtils.formLogin(http, authenticationSuccess);

    HttpSecurityUtils.authorizeRequests(http, true);

    HttpSecurityUtils.sessionManagement(http, sessionRegistry);

    //When testing 'standalone' profile, we can optionally disable CSRF
    if (csrfDisabled) {
      //only for testing 'standalone' profile
      log.warn("CSRF PROTECTION DISABLED!!!!");
      http.csrf(AbstractHttpConfigurer::disable);
    }

    http.authenticationProvider(authenticationProvider);

    return http.build();
  }

  @Bean
  public UserDetailsService  standaloneUsers() {
    UserDetails user = User.builder()
            .username("user").password("{noop}" + "password").roles("USER").build();
    UserDetails admin = User.builder()
            .username("admin").password("{noop}" + "admin").roles("USER","ADMIN").build();
    return new InMemoryUserDetailsManager(user, admin);
  }

  @Bean
  @Qualifier("standaloneAuthenticationProvider")
  public DaoAuthenticationProvider standaloneAuthenticationProvider(@Qualifier("standaloneUsers") UserDetailsService uds) {
    DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
    provider.setUserDetailsService(uds);
    return provider;
  }

  @Bean
  public AuthenticationManager authenticationManager(
          AuthenticationEventPublisher eventPublisher,
          @Qualifier("actuatorAuthenticationProvider") AuthenticationProvider authenticationProvider1,
          @Qualifier("standaloneAuthenticationProvider") AuthenticationProvider authenticationProvider2
  ) {
    ProviderManager result = new ProviderManager(authenticationProvider1, authenticationProvider2);
    result.setAuthenticationEventPublisher(eventPublisher);
    result.afterPropertiesSet();
    return result;
  }
}