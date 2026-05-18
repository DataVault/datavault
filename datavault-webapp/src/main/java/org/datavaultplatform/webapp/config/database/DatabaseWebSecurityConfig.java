package org.datavaultplatform.webapp.config.database;

import lombok.extern.slf4j.Slf4j;
import org.datavaultplatform.webapp.authentication.AuthenticationSuccess;
import org.datavaultplatform.webapp.authentication.database.DatabaseAuthenticationProvider;
import org.datavaultplatform.webapp.config.HttpSecurityUtils;
import org.datavaultplatform.webapp.config.trace.TraceLoggingFilter;
import org.datavaultplatform.webapp.config.trace.MdcRequestFilter;
import org.datavaultplatform.webapp.controllers.auth.DataVaultAccessDeniedHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationEventPublisher;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;

import static org.springframework.security.config.Customizer.withDefaults;

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
  @Order(0)
  @Profile("database")
  public SecurityFilterChain traceApiFilterChain(HttpSecurity http, AuthenticationProvider actuatorAuthenticationProvider) throws Exception {
    return http
            .securityMatcher("/trace/**")
            .csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth.anyRequest().authenticated())
            .authenticationProvider(actuatorAuthenticationProvider)
            .httpBasic(withDefaults())
            .build();
  }

  @Bean
  AccessDeniedHandler accessDeniedHandler() {
    return new DataVaultAccessDeniedHandler();

  }
  
  @Bean
  @Order(2)
  public SecurityFilterChain filterChain(
          HttpSecurity http,
          AccessDeniedHandler accessDeniedHandler,
          TraceLoggingFilter traceLoggingFilter,
          MdcRequestFilter userMdcFilter) throws Exception {

    HttpSecurityUtils.formLogin(http, authenticationSuccess, accessDeniedHandler);

    HttpSecurityUtils.authorizeRequests(http, traceLoggingFilter, userMdcFilter);

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