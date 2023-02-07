package org.datavaultplatform.webapp.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;

@ConditionalOnExpression("${broker.security.enabled:true}")
@Slf4j
@Order(1)
public class SecurityActuatorConfig extends WebSecurityConfigurerAdapter {

  @Value("${spring.security.debug:false}")
  boolean securityDebug;

  @Value("${webapp.actuator.username:wactor}")
  String username;

  @Value("${webapp.actuator.password:wactorpass}")
  String password;

  @Override
  public void configure(WebSecurity web) {
    web.debug(securityDebug);
  }

  @Override
  protected void configure(AuthenticationManagerBuilder auth) throws Exception {
    auth.inMemoryAuthentication()
        .withUser(username).password("{noop}"+password).roles("ACTUATOR")
        .and()
        .passwordEncoder(PasswordEncoderFactories.createDelegatingPasswordEncoder());
  }

  @Override
  protected void configure(HttpSecurity http) throws Exception {

    http.antMatcher("/actuator/**")
        .httpBasic().and()
        .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS).and()
        .authorizeRequests()
        .antMatchers(
            "/actuator",
            "/actuator/info",
            "/actuator/health",
            "/actuator/customtime",
            "/actuator/metrics", "/actuator/metrics/*", "/actuator/memoryinfo").permitAll()
        .anyRequest().authenticated();
  }



}
