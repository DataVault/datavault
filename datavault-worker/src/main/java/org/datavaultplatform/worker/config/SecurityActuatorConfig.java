package org.datavaultplatform.worker.config;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;

@ConditionalOnExpression("${worker.security.enabled:true}")
@EnableWebSecurity
@Slf4j
@Order(1)
@Configuration
public class SecurityActuatorConfig {

  @Value("${spring.security.debug:false}")
  boolean securityDebug;

  @Value("${worker.actuator.username:wactu}")
  String username;

  @Value("${worker.actuator.password:wactupass}")
  String password;

  @Bean
  public WebSecurityCustomizer webSecurityCustomizer() {
    return web -> web.debug(securityDebug);
  }

  @Bean
  public UserDetailsService userDetailsService(){
    UserDetails user = User.builder()
            .username(username)
            .password("{noop}"+password)
            .roles("ACTUATOR")
            .build();
    return new InMemoryUserDetailsManager(user);
  }

  @Bean
  SecurityFilterChain springFilterChain(HttpSecurity http) throws Exception {
    http.securityMatcher("/actuator/**")
            .userDetailsService(userDetailsService())
            .httpBasic(Customizer.withDefaults())
            .sessionManagement((session) -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(authz -> {
              authz.requestMatchers(
                "/actuator/customtime",
                  "/actuator/health",
                      "/actuator/memoryinfo",
                      "/actuator/metrics",
                  "/actuator/info").permitAll();
              authz.anyRequest().authenticated();
            });
    return http.build();
  }

  @PostConstruct
  void init() {
    log.info("Security Actuator Config!!!");
  }
}
