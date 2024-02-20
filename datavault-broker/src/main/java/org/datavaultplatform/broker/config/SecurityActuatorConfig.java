package org.datavaultplatform.broker.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@ConditionalOnExpression("${broker.security.enabled:true}")
@Configuration
@Slf4j
public class SecurityActuatorConfig {

  @Value("${broker.actuator.username:bactor}")
  private String username;

  @Value("${broker.actuator.password:bactorpass}")
  private String password;

  @Bean
  public UserDetailsService actuatorUsers() {
    UserDetails user = User.builder()
            .username(username).password("{noop}" + password).roles("ACTUATOR").build();

    return new InMemoryUserDetailsManager(user);
  }

  @Bean
  public DaoAuthenticationProvider actuatorAuthenticationProvider(@Qualifier("actuatorUsers") UserDetailsService uds) {
    DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
    provider.setUserDetailsService(uds);
    return provider;
  }

  @Bean
  @Order(1)
  public SecurityFilterChain actuatorSecurityFilterChain(HttpSecurity http,
                                                         @Qualifier("actuatorAuthenticationProvider") AuthenticationProvider authenticationProvider) throws Exception {
    http.securityMatcher("/actuator/**")
            .authenticationProvider( authenticationProvider )
            .csrf(csrf -> csrf.disable())
            .httpBasic(Customizer.withDefaults())
            .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests( authz -> {
                authz.requestMatchers("/actuator/info",
                        "/actuator/health",
                        "/actuator/metrics",
                        "/actuator/memoryinfo").permitAll();
                authz.anyRequest().fullyAuthenticated();
            });

    return http.build();
  }
}
