package org.datavaultplatform.webapp.config.database;

import lombok.extern.slf4j.Slf4j;
import org.datavaultplatform.webapp.authentication.AuthenticationSuccess;
import org.datavaultplatform.webapp.authentication.database.DatabaseAuthenticationProvider;
import org.datavaultplatform.webapp.config.HttpSecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationEventPublisher;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.web.SecurityFilterChain;

@EnableWebSecurity
@Slf4j
@EnableMethodSecurity(prePostEnabled = true, securedEnabled = true)
@Configuration
public class DatabaseWebSecurityConfig {

  @Autowired
  SessionRegistry sessionRegistry;

  @Autowired
  AuthenticationSuccess authenticationSuccess;

  @Autowired
  DatabaseAuthenticationProvider databaseAuthenticationProvider;

  @Bean
  @Order(2)
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    HttpSecurityUtils.formLogin(http, authenticationSuccess);

    HttpSecurityUtils.authorizeRequests(http);

    HttpSecurityUtils.sessionManagement(http, sessionRegistry);

    http.authenticationProvider(this.databaseAuthenticationProvider);

    return http.build();
  }

  @Bean
  public AuthenticationManager authenticationManager(
          AuthenticationEventPublisher eventPublisher,
          @Qualifier("actuatorAuthenticationProvider") AuthenticationProvider authenticationProvider1
  ) {
    ProviderManager result =  new ProviderManager(authenticationProvider1, this.databaseAuthenticationProvider);
    result.setAuthenticationEventPublisher(eventPublisher);
    result.afterPropertiesSet();
    return result;
  }

}